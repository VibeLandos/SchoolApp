import java.io.File

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register("pinCursorGradleCache") {
    notCompatibleWithConfigurationCache("Recreates Cursor Temp junctions every build")
    doNotTrackState("Sandbox Temp is wiped between Studio runs")
    doLast { pinCursorSandboxGradleHome() }
}

gradle.projectsEvaluated {
    val pin = tasks.named("pinCursorGradleCache")
    subprojects.forEach { project ->
        project.tasks.matching { task ->
            task.name.startsWith("mergeExtDex") ||
                task.name.startsWith("process") && task.name.endsWith("Resources")
        }.configureEach {
            dependsOn(pin)
        }
    }
}

fun pinCursorSandboxGradleHome() {
    val realHome = File(System.getProperty("user.home"), ".gradle")
    if (!realHome.isDirectory) return
    val sandboxRoot = File(System.getProperty("java.io.tmpdir"), "cursor-sandbox-cache")
    if (sandboxRoot.isDirectory) {
        sandboxRoot.listFiles()?.forEach { hashDir ->
            if (hashDir.isDirectory) {
                junctionGradleHome(File(hashDir, "gradle"), realHome)
            }
        }
    }
    val envHome = System.getenv("GRADLE_USER_HOME")
    if (!envHome.isNullOrBlank() && envHome.contains("cursor-sandbox-cache")) {
        junctionGradleHome(File(envHome), realHome)
    }
}

fun junctionGradleHome(link: File, realHome: File) {
    if (realHome.canonicalPath.equals(link.canonicalPath, ignoreCase = true)) return
    if (link.exists() && File(link, "caches").isDirectory && File(link, "wrapper").isDirectory) {
        return
    }
    link.parentFile.mkdirs()
    if (link.exists()) {
        link.deleteRecursively()
    }
    val result = ProcessBuilder(
        "cmd.exe", "/c", "mklink", "/J", link.absolutePath, realHome.absolutePath,
    ).redirectErrorStream(true).start()
    result.waitFor()
}
