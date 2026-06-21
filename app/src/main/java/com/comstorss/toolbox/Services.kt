package com.comstorss.toolbox

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Xml
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

private const val PDF_RELATIVE_DIR = "Download/MingyuToolBox/PDF"
private const val VIDEO_RELATIVE_DIR = "Download/MingyuToolBox/Video"
private const val DESKTOP_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36"
private const val MOBILE_UA = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36"

class OutputFileService(private val context: Context) {
    fun createPdf(displayName: String, type: ConversionType, write: (OutputStream) -> Unit): Result<ConversionOutput> = runCatching {
        val now = System.currentTimeMillis()
        val safeName = if (displayName.endsWith(".pdf", true)) displayName else "$displayName.pdf"
        val uri = createInDownloads(safeName, "application/pdf", PDF_RELATIVE_DIR, write)
        ConversionOutput(safeName, uri.toString(), "$PDF_RELATIVE_DIR/$safeName", now, type)
    }

    fun createVideo(displayName: String, mimeType: String, platform: String, write: (OutputStream) -> Unit): Result<VideoDownloadOutput> = runCatching {
        val now = System.currentTimeMillis()
        val safeName = if (displayName.endsWith(".mp4", true)) displayName else "$displayName.mp4"
        val uri = createInDownloads(safeName, mimeType, VIDEO_RELATIVE_DIR, write)
        VideoDownloadOutput(safeName, uri.toString(), "$VIDEO_RELATIVE_DIR/$safeName", now, platform, mimeType)
    }

    private fun createInDownloads(displayName: String, mimeType: String, relativeDir: String, write: (OutputStream) -> Unit): Uri {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("\u65e0\u6cd5\u521b\u5efa\u8f93\u51fa\u6587\u4ef6")
            try {
                resolver.openOutputStream(uri)?.use(write) ?: error("\u65e0\u6cd5\u5199\u5165\u8f93\u51fa\u6587\u4ef6")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (error: Throwable) {
                resolver.delete(uri, null, null)
                throw error
            }
            return uri
        }

        val base = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), relativeDir.removePrefix("Download/"))
        base.mkdirs()
        val file = File(base, displayName)
        FileOutputStream(file).use(write)
        return Uri.fromFile(file)
    }

    fun displayName(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) return cursor.getString(nameIndex)
        }
        return uri.lastPathSegment ?: "document"
    }
}

class ImagePdfService(private val context: Context) {
    private val outputFiles = OutputFileService(context)

    fun createPdf(uris: List<Uri>, options: PdfOptions): Result<ConversionOutput> {
        if (uris.isEmpty()) return Result.failure(IllegalArgumentException("\u8bf7\u9009\u62e9\u81f3\u5c11\u4e00\u5f20\u56fe\u7247"))
        val name = "images-${timestamp()}.pdf"
        return outputFiles.createPdf(name, ConversionType.ImagesToPdf) { stream ->
            val document = PdfDocument()
            try {
                uris.forEachIndexed { index, uri ->
                    val bitmap = decodeBitmap(uri) ?: error("\u7b2c ${index + 1} \u5f20\u56fe\u7247\u65e0\u6cd5\u8bfb\u53d6")
                    try {
                        val pageSize = pageSizeFor(bitmap, options)
                        val page = document.startPage(PdfDocument.PageInfo.Builder(pageSize.first, pageSize.second, index + 1).create())
                        drawBitmap(page.canvas, bitmap, pageSize.first, pageSize.second, options.imageFit)
                        document.finishPage(page)
                    } finally {
                        bitmap.recycle()
                    }
                }
                document.writeTo(stream)
            } finally {
                document.close()
            }
        }
    }

    private fun decodeBitmap(uri: Uri): Bitmap? {
        return context.contentResolver.openInputStream(uri).use { input -> BitmapFactory.decodeStream(input) }
    }

    private fun pageSizeFor(bitmap: Bitmap, options: PdfOptions): Pair<Int, Int> {
        if (options.pageMode == PdfPageMode.Original) {
            val maxSide = 1600f
            val scale = minOf(1f, maxSide / maxOf(bitmap.width, bitmap.height).toFloat())
            return maxOf(1, (bitmap.width * scale).toInt()) to maxOf(1, (bitmap.height * scale).toInt())
        }
        return if (options.orientation == PdfOrientation.Landscape) 842 to 595 else 595 to 842
    }

    private fun drawBitmap(canvas: Canvas, bitmap: Bitmap, pageWidth: Int, pageHeight: Int, fit: PdfImageFit) {
        val widthScale = pageWidth.toFloat() / bitmap.width
        val heightScale = pageHeight.toFloat() / bitmap.height
        val scale = if (fit == PdfImageFit.Fill) maxOf(widthScale, heightScale) else minOf(widthScale, heightScale)
        val w = bitmap.width * scale
        val h = bitmap.height * scale
        val rect = RectF((pageWidth - w) / 2f, (pageHeight - h) / 2f, (pageWidth + w) / 2f, (pageHeight + h) / 2f)
        canvas.drawBitmap(bitmap, null, rect, null)
    }
}

class DocxPdfService(private val context: Context) {
    private val outputFiles = OutputFileService(context)

    fun createPdf(uri: Uri): Result<ConversionOutput> = runCatching {
        val fileName = outputFiles.displayName(uri)
        require(fileName.endsWith(".docx", true)) { "\u5f53\u524d\u4ec5\u652f\u6301 .docx \u6587\u4ef6" }
        val content = readDocx(uri)
        require(content.paragraphs.isNotEmpty() || content.images.isNotEmpty()) { "\u6ca1\u6709\u8bfb\u53d6\u5230\u53ef\u8f6c\u6362\u7684\u6587\u6863\u5185\u5bb9" }
        outputFiles.createPdf("docx-${timestamp()}.pdf", ConversionType.DocxToPdf) { stream ->
            writeDocxPdf(content, stream)
        }.getOrThrow()
    }

    private fun readDocx(uri: Uri): DocxContent {
        var documentXml: String? = null
        val images = mutableListOf<ByteArray>()
        context.contentResolver.openInputStream(uri).use { input ->
            ZipInputStream(input ?: error("\u65e0\u6cd5\u6253\u5f00 DOCX \u6587\u4ef6")).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory && name == "word/document.xml") {
                        documentXml = zip.readBytes().toString(Charsets.UTF_8)
                    } else if (!entry.isDirectory && name.startsWith("word/media/")) {
                        images += zip.readBytes()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
        return DocxContent(parseDocumentText(documentXml.orEmpty()), images)
    }

    private fun parseDocumentText(xml: String): List<String> {
        if (xml.isBlank()) return emptyList()
        val parser = Xml.newPullParser()
        parser.setInput(xml.reader())
        val lines = mutableListOf<String>()
        val current = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name?.substringAfter(':')
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (tag) {
                        "t" -> current.append(parser.nextText())
                        "tab" -> current.append("    ")
                        "br" -> current.append('\n')
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tag) {
                        "p" -> {
                            val text = current.toString().trim()
                            if (text.isNotEmpty()) lines += text
                            current.clear()
                        }
                        "tr" -> current.append('\n')
                        "tc" -> current.append("  ")
                    }
                }
            }
            event = parser.next()
        }
        val rest = current.toString().trim()
        if (rest.isNotEmpty()) lines += rest
        return lines
    }

    private fun writeDocxPdf(content: DocxContent, stream: OutputStream) {
        val document = PdfDocument()
        val writer = PdfPageWriter(document)
        content.paragraphs.forEach { writer.drawParagraph(it) }
        content.images.forEach { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let { bitmap ->
                try { writer.drawImage(bitmap) } finally { bitmap.recycle() }
            }
        }
        writer.finish()
        try {
            document.writeTo(stream)
        } finally {
            document.close()
        }
    }
}

class HistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)

    fun load(): List<HistoryRecord> {
        val raw = prefs.getString("records", "[]").orEmpty()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                HistoryRecord(
                    id = item.optString("id", UUID.randomUUID().toString()),
                    title = item.optString("title"),
                    detail = item.optString("detail"),
                    path = item.optString("path").ifBlank { null },
                    status = runCatching { TaskStatus.valueOf(item.optString("status")) }.getOrDefault(TaskStatus.Success),
                    uri = item.optString("uri").ifBlank { null },
                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                    errorMessage = item.optString("errorMessage").ifBlank { null },
                    mimeType = item.optString("mimeType", "application/pdf")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun save(records: List<HistoryRecord>) {
        val array = JSONArray()
        records.forEach { record ->
            array.put(JSONObject().apply {
                put("id", record.id)
                put("title", record.title)
                put("detail", record.detail)
                put("path", record.path ?: "")
                put("status", record.status.name)
                put("uri", record.uri ?: "")
                put("createdAt", record.createdAt)
                put("errorMessage", record.errorMessage ?: "")
                put("mimeType", record.mimeType)
            })
        }
        prefs.edit().putString("records", array.toString()).apply()
    }
}

class FileActionService(private val context: Context) {
    fun open(record: HistoryRecord): Result<Unit> = runCatching {
        val uri = record.uri?.let(Uri::parse) ?: error("\u6ca1\u6709\u53ef\u6253\u5f00\u7684\u6587\u4ef6")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, record.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "\u6253\u5f00\u6587\u4ef6").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun share(record: HistoryRecord): Result<Unit> = runCatching {
        val uri = record.uri?.let(Uri::parse) ?: error("\u6ca1\u6709\u53ef\u5206\u4eab\u7684\u6587\u4ef6")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = record.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "\u5206\u4eab\u6587\u4ef6").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun deleteFile(record: HistoryRecord): Result<Unit> = runCatching {
        val uri = record.uri?.let(Uri::parse) ?: return@runCatching
        context.contentResolver.delete(uri, null, null)
    }
}

interface VideoProvider {
    val platform: String
    fun match(url: String): Boolean
    fun parse(url: String, client: OkHttpClient): Result<VideoInfo>
}

class DouyinProvider : VideoProvider {
    override val platform = "\u6296\u97f3"
    override fun match(url: String) = url.contains("douyin.com", true) || url.contains("iesdouyin.com", true)

    override fun parse(url: String, client: OkHttpClient): Result<VideoInfo> = runCatching {
        val pageUrl = client.resolveUrl(url, MOBILE_UA)
        val html = client.getText(pageUrl, MOBILE_UA, pageUrl)
        val title = html.firstGroup("\\\"desc\\\"\\s*:\\s*\\\"(.*?)\\\"")
            ?: html.firstGroup("<title>(.*?)</title>")
            ?: "\u6296\u97f3\u89c6\u9891"
        val author = html.firstGroup("\\\"nickname\\\"\\s*:\\s*\\\"(.*?)\\\"") ?: "\u672a\u77e5\u4f5c\u8005"
        val cover = findUrlFromJsonList(html, "cover") ?: findUrlFromJsonList(html, "origin_cover")
        val direct = findUrlFromJsonList(html, "play_addr")
            ?.replace("playwm", "play")
            ?.replace("watermark=1", "watermark=0")
            ?: html.firstGroup("https?://[^\\\"'<>\\s]+?video[^\\\"'<>\\s]+")?.decodeJsonText()
        require(!direct.isNullOrBlank()) { "\u8fd9\u4e2a\u6296\u97f3\u94fe\u63a5\u6682\u65f6\u6ca1\u89e3\u6790\u5230\u53ef\u4e0b\u8f7d\u89c6\u9891" }
        VideoInfo(
            platform = platform,
            title = title.decodeJsonText().cleanText().ifBlank { "\u6296\u97f3\u89c6\u9891" },
            author = author.decodeJsonText().cleanText().ifBlank { "\u672a\u77e5\u4f5c\u8005" },
            duration = "\u672a\u77e5",
            url = pageUrl,
            directUrl = direct.decodeJsonText(),
            coverUrl = cover?.decodeJsonText(),
            fileName = safeFileName("douyin-${title.decodeJsonText()}.mp4"),
            warning = "\u5df2\u89e3\u6790\u5230\u53ef\u4e0b\u8f7d\u5730\u5740"
        )
    }

    private fun findUrlFromJsonList(html: String, key: String): String? {
        val block = html.firstGroup("\\\"$key\\\"\\s*:\\s*\\{(.{0,1800}?)\\}") ?: return null
        val list = block.firstGroup("\\\"url_list\\\"\\s*:\\s*\\[(.*?)\\]") ?: return null
        return Regex("\\\"(https?:.*?)(?<!\\\\)\\\"").find(list)?.groupValues?.get(1)
    }
}

class BilibiliProvider : VideoProvider {
    override val platform = "Bilibili"
    override fun match(url: String) = url.contains("bilibili.com", true) || url.contains("b23.tv", true)

    override fun parse(url: String, client: OkHttpClient): Result<VideoInfo> = runCatching {
        val pageUrl = client.resolveUrl(url, DESKTOP_UA)
        val bvid = Regex("BV[0-9A-Za-z]+", RegexOption.IGNORE_CASE).find(pageUrl)?.value
        val avid = Regex("(?:av|aid=)(\\d+)", RegexOption.IGNORE_CASE).find(pageUrl)?.groupValues?.get(1)
        require(!bvid.isNullOrBlank() || !avid.isNullOrBlank()) { "\u6ca1\u6709\u8bc6\u522b\u5230 Bilibili \u89c6\u9891\u53f7" }

        val viewUrl = if (!bvid.isNullOrBlank()) {
            "https://api.bilibili.com/x/web-interface/view?bvid=$bvid"
        } else {
            "https://api.bilibili.com/x/web-interface/view?aid=$avid"
        }
        val view = JSONObject(client.getText(viewUrl, DESKTOP_UA, pageUrl))
        require(view.optInt("code") == 0) { view.optString("message", "Bilibili \u89c6\u9891\u4fe1\u606f\u89e3\u6790\u5931\u8d25") }
        val data = view.getJSONObject("data")
        val cid = data.getLong("cid")
        val actualBvid = data.optString("bvid", bvid.orEmpty())
        val title = data.optString("title", "Bilibili \u89c6\u9891")
        val author = data.optJSONObject("owner")?.optString("name") ?: "UP \u4e3b"
        val cover = data.optString("pic").ifBlank { null }
        val duration = data.optLong("duration", 0L).formatDuration()
        val playUrl = "https://api.bilibili.com/x/player/playurl?bvid=$actualBvid&cid=$cid&qn=16&otype=json&fnval=0&fourk=0"
        val play = JSONObject(client.getText(playUrl, DESKTOP_UA, pageUrl))
        require(play.optInt("code") == 0) { play.optString("message", "Bilibili \u64ad\u653e\u5730\u5740\u89e3\u6790\u5931\u8d25") }
        val durl = play.getJSONObject("data").optJSONArray("durl")
        val direct = durl?.optJSONObject(0)?.optString("url")?.takeIf { it.isNotBlank() }
        require(!direct.isNullOrBlank()) { "\u8be5 Bilibili \u89c6\u9891\u8fd4\u56de\u7684\u662f\u5206\u7247\u8d44\u6e90\uff0c\u5f53\u524d\u7248\u672c\u6682\u4e0d\u5408\u5e76\u97f3\u89c6\u9891" }
        VideoInfo(
            platform = platform,
            title = title.cleanText(),
            author = author.cleanText(),
            duration = duration,
            url = pageUrl,
            directUrl = direct,
            coverUrl = cover,
            fileName = safeFileName("bilibili-${title}.mp4"),
            warning = "\u57fa\u7840\u6e05\u6670\u5ea6\u53ef\u4e0b\u8f7d"
        )
    }
}

class VideoService(private val context: Context) {
    private val outputFiles = OutputFileService(context)
    private val providers = listOf(DouyinProvider(), BilibiliProvider())
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(18, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    fun parse(input: String): Result<VideoInfo> {
        val url = extractFirstUrl(input) ?: return Result.failure(IllegalArgumentException("\u8bf7\u5148\u7c98\u8d34\u89c6\u9891\u94fe\u63a5"))
        val provider = providers.firstOrNull { it.match(url) }
            ?: return Result.failure(IllegalArgumentException("\u6682\u53ea\u652f\u6301\u6296\u97f3\u548c Bilibili \u94fe\u63a5"))
        return provider.parse(url, client)
    }

    fun download(info: VideoInfo, onProgress: (Float) -> Unit): Result<VideoDownloadOutput> = runCatching {
        val direct = info.directUrl?.decodeJsonText() ?: throw UnsupportedOperationException("\u8bf7\u5148\u89e3\u6790\u5230\u53ef\u4e0b\u8f7d\u7684\u89c6\u9891")
        outputFiles.createVideo(info.fileName, info.mimeType, info.platform) { stream ->
            val request = Request.Builder()
                .url(direct)
                .header("User-Agent", if (info.platform == "\u6296\u97f3") MOBILE_UA else DESKTOP_UA)
                .header("Referer", info.url)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("\u4e0b\u8f7d\u5931\u8d25\uff1a${response.code}")
                val body = response.body ?: error("\u4e0b\u8f7d\u5185\u5bb9\u4e3a\u7a7a")
                val total = body.contentLength().takeIf { it > 0L }
                body.byteStream().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var read: Int
                    var done = 0L
                    while (true) {
                        read = input.read(buffer)
                        if (read <= 0) break
                        stream.write(buffer, 0, read)
                        done += read
                        if (total != null) onProgress((done.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
        }.getOrThrow()
    }
}

private data class DocxContent(val paragraphs: List<String>, val images: List<ByteArray>)

private class PdfPageWriter(private val document: PdfDocument) {
    private val pageWidth = 595
    private val pageHeight = 842
    private val margin = 48f
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 15f
        color = android.graphics.Color.BLACK
    }
    private var pageNumber = 0
    private var page = newPage()
    private var canvas = page.canvas
    private var y = margin

    fun drawParagraph(text: String) {
        val lines = wrap(text, textPaint, pageWidth - margin * 2)
        lines.forEach { line ->
            ensureSpace(24f)
            canvas.drawText(line, margin, y, textPaint)
            y += 22f
        }
        y += 8f
    }

    fun drawImage(bitmap: Bitmap) {
        val maxWidth = pageWidth - margin * 2
        val maxHeight = 360f
        val scale = minOf(maxWidth / bitmap.width, maxHeight / bitmap.height)
        val width = bitmap.width * scale
        val height = bitmap.height * scale
        ensureSpace(height + 18f)
        val left = margin + (maxWidth - width) / 2f
        canvas.drawBitmap(bitmap, null, RectF(left, y, left + width, y + height), null)
        y += height + 18f
    }

    fun finish() {
        document.finishPage(page)
    }

    private fun ensureSpace(required: Float) {
        if (y + required <= pageHeight - margin) return
        document.finishPage(page)
        page = newPage()
        canvas = page.canvas
        y = margin
    }

    private fun newPage(): PdfDocument.Page {
        pageNumber += 1
        return document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
    }

    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        var remaining = text
        while (remaining.isNotEmpty()) {
            val count = paint.breakText(remaining, true, maxWidth, null)
            if (count <= 0) break
            result += remaining.take(count)
            remaining = remaining.drop(count).trimStart()
        }
        return result.ifEmpty { listOf(text) }
    }
}

private fun OkHttpClient.resolveUrl(url: String, userAgent: String): String {
    val request = Request.Builder().url(url).header("User-Agent", userAgent).build()
    return newCall(request).execute().use { response -> response.request.url.toString() }
}

private fun OkHttpClient.getText(url: String, userAgent: String, referer: String? = null): String {
    val builder = Request.Builder().url(url).header("User-Agent", userAgent)
    if (referer != null) builder.header("Referer", referer)
    return newCall(builder.build()).execute().use { response ->
        if (!response.isSuccessful) error("\u7f51\u7edc\u8bf7\u6c42\u5931\u8d25\uff1a${response.code}")
        response.body?.string() ?: error("\u8fd4\u56de\u5185\u5bb9\u4e3a\u7a7a")
    }
}

private fun extractFirstUrl(input: String): String? {
    return Regex("https?://[^\\s\\u4e00-\\u9fa5]+", RegexOption.IGNORE_CASE)
        .find(input)
        ?.value
        ?.trim('"', '\'', ',', '\u3002', '\uff0c')
}

private fun String.firstGroup(pattern: String): String? = Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)).find(this)?.groupValues?.getOrNull(1)

private fun String.decodeJsonText(): String {
    var unescaped = this
    repeat(3) {
        unescaped = unescaped
            .replace("\\\\u002F", "/")
            .replace("\\\\u002f", "/")
            .replace("\\u002F", "/")
            .replace("\\u002f", "/")
            .replace("\\\\/", "/")
            .replace("\\/", "/")
            .replace("&amp;", "&")
            .replace("\\\\u0026", "&")
            .replace("\\u0026", "&")
            .replace("\\\\u003A", ":")
            .replace("\\\\u003a", ":")
            .replace("\\u003A", ":")
            .replace("\\u003a", ":")
            .replace("\\\\u003D", "=")
            .replace("\\\\u003d", "=")
            .replace("\\u003D", "=")
            .replace("\\u003d", "=")
            .replace("\\\\u003F", "?")
            .replace("\\\\u003f", "?")
            .replace("\\u003F", "?")
            .replace("\\u003f", "?")
        unescaped = Regex("\\\\u([0-9a-fA-F]{4})").replace(unescaped) { match ->
            match.groupValues[1].toInt(16).toChar().toString()
        }
    }
    return runCatching { URLDecoder.decode(unescaped, "UTF-8") }.getOrDefault(unescaped)
}

private fun String.cleanText(): String = replace(Regex("\\s+"), " ").trim()

private fun Long.formatDuration(): String {
    if (this <= 0L) return "\u672a\u77e5"
    val minute = this / 60
    val second = this % 60
    return "%d:%02d".format(Locale.US, minute, second)
}

private fun safeFileName(name: String): String {
    val cleaned = name.cleanText().replace(Regex("[\\\\/:*?\"<>|]"), "_").take(80).trim('_', ' ')
    return if (cleaned.isBlank()) "video-${timestamp()}.mp4" else cleaned
}

private fun ZipInputStream.readBytes(): ByteArray {
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val read = read(buffer)
        if (read <= 0) break
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}

private fun timestamp(): String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())