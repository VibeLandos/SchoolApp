import java.io.File
import org.gradle.api.Task
import org.gradle.api.file.FileCollection

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register("pinCursorGradleCache") {
    notCompatibleWithConfigurationCache("Recreates Cursor Temp cache every build")
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
                restoreMissingSandboxFiles(collectTaskFiles(this), realHome)
            }
        }
    }
}

fun collectTaskFiles(task: Task): Set<File> {
    val files = mutableSetOf<File>()
    files.addAll(task.inputs.files.files)
    task.javaClass.methods.filter { method ->
        method.parameterCount == 0 && FileCollection::class.java.isAssignableFrom(method.returnType)
    }.forEach { method ->
        runCatching {
            val collection = method.invoke(task) as? FileCollection
            if (collection != null) files.addAll(collection.files)
        }
    }
    return files
}

fun pinCursorSandboxGradleHome() {
    val realHome = File(System.getProperty("user.home"), ".gradle")
    if (!realHome.isDirectory) return
    sandboxGradleHomes().forEach { home ->
        seedSandboxTransforms(home, realHome)
    }
}

fun sandboxGradleHomes(): List<File> {
    val homes = mutableListOf<File>()
    val envHome = System.getenv("GRADLE_USER_HOME")
    if (!envHome.isNullOrBlank() && envHome.contains("cursor-sandbox-cache")) {
        homes += File(envHome)
    }
    val sandboxRoot = File(System.getProperty("java.io.tmpdir"), "cursor-sandbox-cache")
    if (sandboxRoot.isDirectory) {
        sandboxRoot.listFiles()?.forEach { hashDir ->
            if (hashDir.isDirectory) homes += File(hashDir, "gradle")
        }
    }
    return homes.distinctBy { it.absolutePath.lowercase() }
}

fun seedSandboxTransforms(sandboxGradle: File, realHome: File) {
    val srcRoot = File(realHome, "caches${File.separator}transforms-4")
    val destRoot = File(sandboxGradle, "caches${File.separator}transforms-4")
    if (!srcRoot.isDirectory) return
    destRoot.mkdirs()
    copyHashIfContains(srcRoot, destRoot, "navigation-common-ktx-2.7.7-runtime_dex")
    copyHashIfContains(srcRoot, destRoot, "m3_ref_palette_dynamic_neutral_variant6.xml")
    copyHashIfContains(srcRoot, destRoot, "material_ic_keyboard_arrow_right_black_24dp.xml")
    copyHashIfContains(srcRoot, destRoot, "design_bottom_sheet_slide_in.xml")
    val dexName = "navigation-common-ktx-2.7.7-runtime_dex"
    val sourceHash = srcRoot.listFiles().orEmpty().firstOrNull { hashDir ->
        hashDir.walk().any { it.name == dexName }
    }
    listOf("288ac6577544677e4efe5c46dcbefc95").forEach { hash ->
        if (sourceHash == null) return@forEach
        val dest = File(destRoot, hash)
        val expected = File(dest, "transformed${File.separator}navigation-common-ktx-2.7.7-runtime${File.separator}$dexName")
        if (!expected.exists()) {
            dest.mkdirs()
            sourceHash.copyRecursively(dest, overwrite = true)
        }
    }
}

fun copyHashIfContains(srcRoot: File, destRoot: File, markerName: String) {
    srcRoot.listFiles().orEmpty().forEach { hashDir ->
        if (hashDir.walk().any { it.name == markerName }) {
            val dest = File(destRoot, hashDir.name)
            if (!File(dest, "transformed").exists()) {
                hashDir.copyRecursively(dest, overwrite = true)
            }
        }
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
    if (!destHashDir.exists() || !missing.exists()) {
        sourceHashDir.copyRecursively(destHashDir, overwrite = true)
    }
}
