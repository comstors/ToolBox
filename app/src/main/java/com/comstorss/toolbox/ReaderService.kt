package com.comstorss.toolbox

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.UUID

class ReaderService(private val context: Context) {
    private val prefs = context.getSharedPreferences("reader", Context.MODE_PRIVATE)

    fun loadBooks(): List<ReaderBook> {
        val raw = prefs.getString(KEY_BOOKS, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        ReaderBook(
                            id = item.optString("id", UUID.randomUUID().toString()),
                            title = item.optString("title", "TXT"),
                            uri = item.optString("uri"),
                            totalChars = item.optInt("totalChars", 0),
                            currentParagraph = item.optInt("currentParagraph", 0),
                            scrollOffset = item.optInt("scrollOffset", 0),
                            progress = item.optDouble("progress", 0.0).toFloat().coerceIn(0f, 1f),
                            lastReadAt = item.optLong("lastReadAt", System.currentTimeMillis())
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveBooks(books: List<ReaderBook>) {
        val array = JSONArray()
        books.forEach { book ->
            array.put(
                JSONObject()
                    .put("id", book.id)
                    .put("title", book.title)
                    .put("uri", book.uri)
                    .put("totalChars", book.totalChars)
                    .put("currentParagraph", book.currentParagraph)
                    .put("scrollOffset", book.scrollOffset)
                    .put("progress", book.progress.toDouble())
                    .put("lastReadAt", book.lastReadAt)
            )
        }
        prefs.edit().putString(KEY_BOOKS, array.toString()).apply()
    }

    fun importTxt(uri: Uri, existing: List<ReaderBook>): Result<Pair<ReaderBook, List<String>>> = runCatching {
        takeReadPermission(uri)
        val text = readText(uri)
        require(text.isNotBlank()) { "这个 TXT 没有读到正文内容" }
        val paragraphs = splitParagraphs(text)
        val title = displayName(uri).ifBlank { "本地 TXT" }
        val existed = existing.firstOrNull { it.uri == uri.toString() }
        val book = (existed ?: ReaderBook(title = title, uri = uri.toString())).copy(
            title = title,
            totalChars = text.length,
            lastReadAt = System.currentTimeMillis()
        )
        book to paragraphs
    }

    fun readBook(book: ReaderBook): Result<List<String>> = runCatching {
        val text = readText(Uri.parse(book.uri))
        require(text.isNotBlank()) { "这个 TXT 没有读到正文内容" }
        splitParagraphs(text)
    }

    private fun takeReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun readText(uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法打开这个 TXT 文件")
        return decodeText(bytes).trim()
    }

    private fun decodeText(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return bytes.copyOfRange(3, bytes.size).toString(Charsets.UTF_8)
        }
        return try {
            val decoder = Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) {
            bytes.toString(Charset.forName("GBK"))
        }
    }

    private fun splitParagraphs(text: String): List<String> {
        val normalized = text.replace("\r\n", "\n").replace('\r', '\n')
        val rough = normalized.lines().map { it.trim() }.filter { it.isNotBlank() }
        val source = if (rough.isEmpty()) listOf(normalized.trim()) else rough
        return source.flatMap { paragraph ->
            if (paragraph.length <= CHUNK_SIZE) listOf(paragraph) else paragraph.chunked(CHUNK_SIZE)
        }
    }

    private fun displayName(uri: Uri): String {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0).orEmpty() else ""
            }.orEmpty()
        }.getOrDefault(uri.lastPathSegment.orEmpty()).removeSuffix(".txt")
    }

    companion object {
        private const val KEY_BOOKS = "books"
        private const val CHUNK_SIZE = 1200
    }
}