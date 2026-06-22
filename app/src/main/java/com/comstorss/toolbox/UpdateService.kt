package com.comstorss.toolbox

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class UpdateService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun currentVersionCode(): Long {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return PackageInfoCompat.getLongVersionCode(info)
    }

    fun currentVersionName(): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    }

    fun canInstallPackages(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()
    }

    fun fetchLatest(): Result<UpdateInfo> = runCatching {
        val request = Request.Builder()
            .url(VERSION_JSON_URL)
            .header("Cache-Control", "no-cache")
            .build()
        client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "检查更新失败：${response.code}" }
            val body = response.body?.string().orEmpty()
            require(body.isNotBlank()) { "更新配置为空" }
            parseUpdateInfo(body)
        }
    }

    fun downloadApk(info: UpdateInfo, onProgress: (Float) -> Unit): Result<File> = runCatching {
        val request = Request.Builder().url(info.apkUrl).build()
        client.newCall(request).execute().use { response ->
            require(response.isSuccessful) { "下载失败：${response.code}" }
            val body = response.body ?: error("下载内容为空")
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val output = File(dir, APK_FILE_NAME)
            val total = body.contentLength().takeIf { it > 0L } ?: -1L
            var readTotal = 0L
            body.byteStream().use { input ->
                output.outputStream().use { fileOut ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        fileOut.write(buffer, 0, read)
                        readTotal += read
                        if (total > 0L) onProgress((readTotal.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
            require(output.length() > 0L) { "APK 下载失败" }
            onProgress(1f)
            output
        }
    }

    fun installApk(file: File) {
        if (!canInstallPackages()) {
            openInstallPermissionSettings()
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    private fun parseUpdateInfo(raw: String): UpdateInfo {
        val json = JSONObject(raw)
        val versionCode = json.getLong("versionCode")
        val versionName = json.getString("versionName")
        val changelogArray = json.optJSONArray("changelog")
        val changelog = buildList {
            if (changelogArray != null) {
                for (index in 0 until changelogArray.length()) add(changelogArray.optString(index))
            }
        }.filter { it.isNotBlank() }
        val apkUrl = json.optString("apkUrl").takeIf { it.isNotBlank() }
            ?: "https://github.com/comstors/ToolBox/releases/download/v${versionName}/MingyuToolBox.apk"
        return UpdateInfo(
            versionCode = versionCode,
            versionName = versionName,
            changelog = changelog,
            forceUpdate = json.optBoolean("forceUpdate", false),
            apkUrl = apkUrl
        )
    }

    companion object {
        const val VERSION_JSON_URL = "https://raw.githubusercontent.com/comstors/ToolBox/main/update/version.json"
        const val APK_FILE_NAME = "MingyuToolBox.apk"
    }
}