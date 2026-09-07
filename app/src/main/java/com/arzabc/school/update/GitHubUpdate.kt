package com.arzabc.school.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.arzabc.school.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

data class RemoteRelease(
    val versionName: String,
    val versionCode: Int?,
    val apkUrl: String,
)

object GitHubUpdate {
    const val OWNER = "VibeLandos"
    const val REPO = "SchoolApp"
    private const val LATEST =
        "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
    private val userAgent = "SchoolDiary/${BuildConfig.VERSION_NAME}"

    fun isNewer(remote: RemoteRelease): Boolean {
        val remoteCode = remote.versionCode
        if (remoteCode != null) return remoteCode > BuildConfig.VERSION_CODE
        return compareVersionName(remote.versionName, BuildConfig.VERSION_NAME) > 0
    }

    fun fetchLatest(): RemoteRelease? {
        val conn = request(LATEST, "application/vnd.github+json")
        try {
            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_NOT_FOUND) return null
            if (code !in 200..299) error("http $code")
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val tag = json.getString("tag_name").trim().removePrefix("v")
            val body = json.optString("body")
            val versionCode = Regex("""versionCode\s*[=:]\s*(\d+)""")
                .find(body)
                ?.groupValues
                ?.get(1)
                ?.toInt()
            val assets = json.getJSONArray("assets")
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.getString("name").endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.getString("browser_download_url")
                    break
                }
            }
            val url = apkUrl ?: return null
            return RemoteRelease(versionName = tag, versionCode = versionCode, apkUrl = url)
        } finally {
            conn.disconnect()
        }
    }

    fun downloadApk(context: Context, url: String): File {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val file = File(dir, "update.apk")
        val conn = request(url, "*/*")
        try {
            if (conn.responseCode !in 200..299) error("http ${conn.responseCode}")
            conn.inputStream.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        } finally {
            conn.disconnect()
        }
        return file
    }

    fun canInstall(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true
        return context.packageManager.canRequestPackageInstalls()
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun installApk(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }

    fun compareVersionName(left: String, right: String): Int {
        val a = parts(left)
        val b = parts(right)
        val n = maxOf(a.size, b.size)
        for (i in 0 until n) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    private fun parts(value: String): List<Int> =
        value.split('.', '-', '_').mapNotNull { chunk ->
            chunk.filter { it.isDigit() }.toIntOrNull()
        }

    private fun request(urlString: String, accept: String): HttpURLConnection {
        var current = urlString
        repeat(8) {
            val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 15_000
                readTimeout = 60_000
                setRequestProperty("User-Agent", userAgent)
                setRequestProperty("Accept", accept)
            }
            val code = conn.responseCode
            if (code in 300..399) {
                val next = conn.getHeaderField("Location") ?: error("redirect")
                conn.disconnect()
                current = if (next.startsWith("http")) next else URL(URL(current), next).toString()
            } else {
                return conn
            }
        }
        error("redirect")
    }
}
