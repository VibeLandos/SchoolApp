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
    val realHome = File(System.getProperty("user.home"), ".gradle")
    subprojects.forEach { project ->
        project.tasks.matching { task ->
            task.name.startsWith("mergeExtDex") ||
                (task.name.startsWith("process") && task.name.endsWith("Resources"))
        }.configureEach {
            dependsOn(pin)
            doFirst {
                restoreMissingSandboxFiles(inputs.files.files, realHome)
            }
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

fun restoreMissingSandboxFiles(files: Iterable<File>, realHome: File) {
    files.forEach { file ->
        if (file.exists()) return@forEach
        if (!file.absolutePath.contains("cursor-sandbox-cache")) return@forEach
        restoreMissingTransform(file, realHome)
    }
}

fun restoreMissingTransform(missing: File, realHome: File) {
    val needle = "${File.separator}transforms-4${File.separator}"
    val raw = missing.absolutePath
    val at = raw.indexOf(needle, ignoreCase = true)
    if (at < 0) return
    val after = raw.substring(at + needle.length)
    val slash = after.indexOf(File.separatorChar)
    if (slash < 0) return
    val destHashDir = File(raw.substring(0, at + needle.length), after.substring(0, slash))
    val rest = after.substring(slash + 1)
    val realRoot = File(realHome, "caches${File.separator}transforms-4")
    if (!realRoot.isDirectory) return
    val sourceHashDir = realRoot.listFiles().orEmpty().firstOrNull { hashDir ->
        File(hashDir, rest).exists()
    } ?: return
    destHashDir.parentFile.mkdirs()
    if (!destHashDir.exists()) {
        sourceHashDir.copyRecursively(destHashDir)
    }
}

fun junctionGradleHome(link: File, realHome: File) {
    if (realHome.canonicalFile == link.canonicalFile) return
    val alreadyLinked = link.exists() &&
        runCatching { link.canonicalFile == realHome.canonicalFile }.getOrDefault(false)
    if (alreadyLinked) return
    val hasDex = File(link, "caches${File.separator}transforms-4")
        .takeIf { it.isDirectory }
        ?.walk()
        ?.any { it.name.endsWith("_dex") }
        ?: false
    if (hasDex && File(link, "wrapper").isDirectory) return
    link.parentFile.mkdirs()
    if (link.exists()) {
        link.deleteRecursively()
    }
    ProcessBuilder("cmd.exe", "/c", "mklink", "/J", link.absolutePath, realHome.absolutePath)
        .redirectErrorStream(true)
        .start()
        .waitFor()
}
