package com.comstorss.toolbox

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

enum class ThemeMode(val label: String) { System("\u8ddf\u968f\u7cfb\u7edf"), Light("\u6d45\u8272"), Dark("\u6df1\u8272") }
enum class TaskStatus(val label: String) { Running("\u8fdb\u884c\u4e2d"), Success("\u5df2\u5b8c\u6210"), Failed("\u5931\u8d25") }
enum class Route { Home, Convert, Video }
enum class ConversionType(val label: String) { ImagesToPdf("\u56fe\u7247\u8f6c PDF"), DocxToPdf("Word \u8f6c PDF") }
enum class PdfPageMode(val label: String) { A4("A4"), Original("\u539f\u56fe\u6bd4\u4f8b") }
enum class PdfOrientation(val label: String) { Portrait("\u7ad6\u5411"), Landscape("\u6a2a\u5411") }
enum class PdfImageFit(val label: String) { Fit("\u7b49\u6bd4\u9002\u5e94"), Fill("\u94fa\u6ee1\u88c1\u5207") }
enum class MainTab(val label: String, val icon: ImageVector) {
    Home("\u9996\u9875", Icons.Rounded.Home), Tasks("\u4efb\u52a1", Icons.Rounded.TaskAlt), History("\u5386\u53f2", Icons.Rounded.History), Settings("\u8bbe\u7f6e", Icons.Rounded.Settings)
}

data class ToolModule(val id: String, val name: String, val desc: String, val icon: ImageVector, val network: Boolean, val route: Route)
data class ToolboxTask(val id: String = UUID.randomUUID().toString(), val title: String, val detail: String, val progress: Float, val status: TaskStatus)
data class PdfOptions(
    val pageMode: PdfPageMode = PdfPageMode.A4,
    val orientation: PdfOrientation = PdfOrientation.Portrait,
    val imageFit: PdfImageFit = PdfImageFit.Fit
)
data class ConversionOutput(
    val displayName: String,
    val uri: String,
    val displayPath: String,
    val createdAt: Long,
    val type: ConversionType
)
data class HistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val detail: String,
    val path: String?,
    val status: TaskStatus,
    val uri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val mimeType: String = "application/pdf"
)
data class VideoInfo(
    val platform: String,
    val title: String,
    val author: String,
    val duration: String,
    val url: String,
    val directUrl: String?,
    val coverUrl: String? = null,
    val fileName: String = "video.mp4",
    val mimeType: String = "video/mp4",
    val warning: String? = null
)
data class VideoDownloadOutput(
    val displayName: String,
    val uri: String,
    val displayPath: String,
    val createdAt: Long,
    val platform: String,
    val mimeType: String
)

object ModuleRegistry {
    val modules = listOf(
        ToolModule("format", "\u683c\u5f0f\u8f6c\u6362", "\u56fe\u7247\u8f6c PDF\u3001Word \u8f6c PDF\uff0c\u672c\u5730\u5904\u7406\u66f4\u5b89\u5fc3\u3002", Icons.Rounded.Article, false, Route.Convert),
        ToolModule("video", "\u89c6\u9891\u4e0b\u8f7d", "\u6296\u97f3\u4e0e Bilibili \u94fe\u63a5\u89e3\u6790\u4e0b\u8f7d\u3002", Icons.Rounded.Movie, true, Route.Video)
    )
}