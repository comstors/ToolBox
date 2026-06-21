package com.comstorss.toolbox

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToolboxViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val imagePdf = ImagePdfService(app)
    private val docxPdf = DocxPdfService(app)
    private val videoService = VideoService(app)
    private val historyStore = HistoryStore(app)
    private val fileActions = FileActionService(app)

    val modules = ModuleRegistry.modules

    private val _theme = MutableStateFlow(ThemeMode.valueOf(prefs.getString("theme", ThemeMode.System.name) ?: ThemeMode.System.name))
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    private val _tasks = MutableStateFlow<List<ToolboxTask>>(emptyList())
    val tasks: StateFlow<List<ToolboxTask>> = _tasks.asStateFlow()

    private val _history = MutableStateFlow(historyStore.load())
    val history: StateFlow<List<HistoryRecord>> = _history.asStateFlow()

    private val _video = MutableStateFlow<VideoInfo?>(null)
    val video: StateFlow<VideoInfo?> = _video.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        prefs.edit().putString("theme", mode.name).apply()
        _theme.value = mode
    }

    fun clearNotice() { _notice.value = null }

    fun clearCache() {
        getApplication<Application>().cacheDir.deleteRecursively()
        _notice.value = "\u7f13\u5b58\u5df2\u6e05\u7406"
    }

    fun imagesToPdf(uris: List<Uri>, options: PdfOptions) = viewModelScope.launch {
        val task = addTask("\u56fe\u7247\u8f6c PDF", "\u6b63\u5728\u751f\u6210 ${uris.size} \u5f20\u56fe\u7247\u7684 PDF")
        progress(task.id, .15f, .78f)
        val result = withContext(Dispatchers.IO) { imagePdf.createPdf(uris, options) }
        result.onSuccess { output ->
            finish(task.id, TaskStatus.Success, "PDF \u5df2\u751f\u6210")
            addHistory(output.toHistory("\u56fe\u7247\u8f6c PDF", "${uris.size} \u5f20\u56fe\u7247 \u00b7 ${options.pageMode.label} \u00b7 ${options.orientation.label}"))
            _notice.value = "\u751f\u6210\u6210\u529f\uff1a${output.displayName}"
        }.onFailure { error ->
            val message = error.message ?: "\u751f\u6210\u5931\u8d25"
            finish(task.id, TaskStatus.Failed, message)
            addHistory(HistoryRecord(title = "\u56fe\u7247\u8f6c PDF", detail = message, path = null, status = TaskStatus.Failed, errorMessage = message))
            _notice.value = message
        }
    }

    fun wordToPdf(uri: Uri) = viewModelScope.launch {
        val task = addTask("Word \u8f6c PDF", "\u6b63\u5728\u8fdb\u884c\u7b80\u5316\u8f6c\u6362")
        progress(task.id, .1f, .55f)
        val result = withContext(Dispatchers.IO) { docxPdf.createPdf(uri) }
        result.onSuccess { output ->
            finish(task.id, TaskStatus.Success, "PDF \u5df2\u751f\u6210")
            addHistory(output.toHistory("Word \u8f6c PDF", "DOCX \u7b80\u5316\u8f6c\u6362 \u00b7 \u590d\u6742\u6392\u7248\u53ef\u80fd\u4e0e\u539f\u6587\u6863\u4e0d\u540c"))
            _notice.value = "\u751f\u6210\u6210\u529f\uff1a${output.displayName}"
        }.onFailure { error ->
            val message = error.message ?: "Word \u8f6c PDF \u5931\u8d25"
            finish(task.id, TaskStatus.Failed, message)
            addHistory(HistoryRecord(title = "Word \u8f6c PDF", detail = message, path = null, status = TaskStatus.Failed, errorMessage = message))
            _notice.value = message
        }
    }

    fun openHistory(record: HistoryRecord) {
        fileActions.open(record).onFailure { _notice.value = it.message ?: "\u6253\u5f00\u5931\u8d25" }
    }

    fun shareHistory(record: HistoryRecord) {
        fileActions.share(record).onFailure { _notice.value = it.message ?: "\u5206\u4eab\u5931\u8d25" }
    }

    fun deleteHistory(record: HistoryRecord) {
        _history.value = _history.value.filterNot { it.id == record.id }
        persistHistory()
        _notice.value = "\u5386\u53f2\u8bb0\u5f55\u5df2\u5220\u9664"
    }

    fun parseVideo(input: String) = viewModelScope.launch {
        if (input.isBlank()) {
            _notice.value = "\u8bf7\u5148\u7c98\u8d34\u94fe\u63a5"
            return@launch
        }
        val task = addTask("\u89c6\u9891\u89e3\u6790", "\u6b63\u5728\u8bc6\u522b\u94fe\u63a5")
        progress(task.id, .1f, .45f)
        val result = withContext(Dispatchers.IO) { videoService.parse(input) }
        result.onSuccess {
            _video.value = it
            finish(task.id, TaskStatus.Success, "${it.platform} \u89c6\u9891\u5df2\u89e3\u6790")
            _notice.value = "\u89e3\u6790\u6210\u529f"
        }.onFailure {
            val message = it.message ?: "\u89e3\u6790\u5931\u8d25"
            finish(task.id, TaskStatus.Failed, message)
            _notice.value = message
        }
    }

    fun downloadVideo() = viewModelScope.launch {
        val info = _video.value ?: run {
            _notice.value = "\u8bf7\u5148\u89e3\u6790\u94fe\u63a5"
            return@launch
        }
        if (info.directUrl.isNullOrBlank()) {
            _notice.value = "\u8fd9\u4e2a\u89c6\u9891\u6682\u65f6\u6ca1\u6709\u53ef\u4e0b\u8f7d\u5730\u5740"
            return@launch
        }
        val task = addTask("${info.platform} \u4e0b\u8f7d", "\u6b63\u5728\u4fdd\u5b58\u89c6\u9891")
        setProgress(task.id, .08f)
        val result = withContext(Dispatchers.IO) {
            videoService.download(info) { progress ->
                setProgress(task.id, (.1f + progress * .88f).coerceIn(.1f, .98f))
            }
        }
        result.onSuccess { output ->
            finish(task.id, TaskStatus.Success, "\u89c6\u9891\u5df2\u4fdd\u5b58")
            addHistory(output.toHistory("${info.platform} \u4e0b\u8f7d", info.title))
            _notice.value = "\u4e0b\u8f7d\u6210\u529f\uff1a${output.displayName}"
        }.onFailure {
            val message = it.message ?: "\u4e0b\u8f7d\u5931\u8d25"
            finish(task.id, TaskStatus.Failed, message)
            addHistory(HistoryRecord(title = "${info.platform} \u4e0b\u8f7d", detail = message, path = null, status = TaskStatus.Failed, errorMessage = message, mimeType = info.mimeType))
            _notice.value = message
        }
    }

    private fun addTask(title: String, detail: String): ToolboxTask {
        val task = ToolboxTask(title = title, detail = detail, progress = 0f, status = TaskStatus.Running)
        _tasks.value = listOf(task) + _tasks.value
        return task
    }

    private suspend fun progress(id: String, from: Float, to: Float) {
        repeat(8) { i ->
            val p = from + (to - from) * ((i + 1) / 8f)
            setProgress(id, p)
            delay(70)
        }
    }

    private fun setProgress(id: String, progress: Float) {
        _tasks.value = _tasks.value.map { if (it.id == id) it.copy(progress = progress.coerceIn(0f, 1f)) else it }
    }

    private fun finish(id: String, status: TaskStatus, detail: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == id) it.copy(status = status, progress = if (status == TaskStatus.Success) 1f else it.progress, detail = detail) else it
        }
    }

    private fun addHistory(record: HistoryRecord) {
        _history.value = listOf(record) + _history.value
        persistHistory()
    }

    private fun persistHistory() {
        historyStore.save(_history.value)
    }

    private fun ConversionOutput.toHistory(title: String, detail: String): HistoryRecord {
        return HistoryRecord(
            title = title,
            detail = detail,
            path = displayPath,
            status = TaskStatus.Success,
            uri = uri,
            createdAt = createdAt,
            mimeType = "application/pdf"
        )
    }

    private fun VideoDownloadOutput.toHistory(title: String, detail: String): HistoryRecord {
        return HistoryRecord(
            title = title,
            detail = detail,
            path = displayPath,
            status = TaskStatus.Success,
            uri = uri,
            createdAt = createdAt,
            mimeType = mimeType
        )
    }

    companion object {
        fun factory(app: Application) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ToolboxViewModel(app) as T
        }
    }
}