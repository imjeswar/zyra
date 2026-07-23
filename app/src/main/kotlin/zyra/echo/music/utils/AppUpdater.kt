package zyra.echo.music.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import zyra.echo.music.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.Duration

data class ReleaseInfo(
    val versionName: String,
    val releaseNotes: String,
    val downloadUrl: String,
)

object AppUpdater {
    private const val GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/imjeswar/zyra/releases/latest"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(15))
        .readTimeout(Duration.ofSeconds(30))
        .followRedirects(true)
        .build()

    suspend fun checkForUpdate(): Result<ReleaseInfo?> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(GITHUB_LATEST_RELEASE_URL)
                .header("User-Agent", "Zyra-App")
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.tag("AppUpdater").w("GitHub releases API returned code ${response.code}")
                    return@runCatching null
                }

                val bodyStr = response.body?.string() ?: return@runCatching null
                val rootJson = json.parseToJsonElement(bodyStr).jsonObject

                val tagName = rootJson["tag_name"]?.jsonPrimitive?.content ?: ""
                val releaseNotes = rootJson["body"]?.jsonPrimitive?.content ?: ""
                val assets = rootJson["assets"]?.jsonArray ?: emptyList()

                var apkDownloadUrl = ""
                for (asset in assets) {
                    val assetObj = asset.jsonObject
                    val name = assetObj["name"]?.jsonPrimitive?.content ?: ""
                    val downloadUrl = assetObj["browser_download_url"]?.jsonPrimitive?.content ?: ""
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = downloadUrl
                        break
                    }
                }

                if (apkDownloadUrl.isEmpty() || tagName.isEmpty()) {
                    return@runCatching null
                }

                val currentVersion = BuildConfig.VERSION_NAME
                if (isNewerVersion(tagName, currentVersion)) {
                    ReleaseInfo(
                        versionName = tagName,
                        releaseNotes = releaseNotes,
                        downloadUrl = apkDownloadUrl,
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
        val cleanRemote = remoteTag.trimStart('v', 'V').trim()
        val cleanCurrent = currentVersion.trimStart('v', 'V').trim()

        if (cleanRemote == cleanCurrent) return false

        val remoteParts = cleanRemote.split(".").mapNotNull { it.takeWhile { char -> char.isDigit() }.toIntOrNull() }
        val currentParts = cleanCurrent.split(".").mapNotNull { it.takeWhile { char -> char.isDigit() }.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val remoteVal = remoteParts.getOrElse(i) { 0 }
            val currentVal = currentParts.getOrElse(i) { 0 }
            if (remoteVal > currentVal) return true
            if (remoteVal < currentVal) return false
        }

        return false
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit = {},
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cacheDir = context.externalCacheDir ?: context.cacheDir
            val apkFile = File(cacheDir, "update.apk")
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "Zyra-App")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Failed to download APK: HTTP ${response.code}")
                }

                val responseBody = response.body ?: throw IllegalStateException("Empty response body")
                val contentLength = responseBody.contentLength()

                responseBody.byteStream().use { inputStream ->
                    FileOutputStream(apkFile).use { outputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = ((totalBytesRead * 100) / contentLength).toInt()
                                withContext(Dispatchers.Main) {
                                    onProgress(progress.coerceIn(0, 100))
                                }
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                installApk(context, apkFile)
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val manageIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(manageIntent)
                    Toast.makeText(
                        context,
                        "Please allow 'Install unknown apps' and tap Update again",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Timber.tag("AppUpdater").e(e, "Failed to launch package installer")
            Toast.makeText(context, "Failed to launch installer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
