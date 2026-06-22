package com.comstorss.toolbox

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.UUID

enum class ThemeMode(val label: String) { System("\u8ddf\u968f\u7cfb\u7edf"), Light("\u6d45\u8272"), Dark("\u6df1\u8272") }
enum class AccentPreset(val label: String, val color: Long) {
    Ocean("\u9ed8\u8ba4\u84dd", 0xFF0284C7),
    Mint("\u8584\u8377\u7eff", 0xFF10B981),
    Orange("\u6696\u6a59\u8272", 0xFFF97316),
    Rose("\u73ab\u7470\u7ea2", 0xFFE11D48),
    Slate("\u66dc\u77f3\u7070", 0xFF64748B)
}
enum class BackgroundStyle(val label: String) {
    Solid("\u5e72\u51c0\u5e95\u8272"),
    Gradient("\u67d4\u5149\u6e10\u53d8"),
    Image("\u81ea\u5b9a\u4e49\u80cc\u666f\u56fe")
}
enum class BackgroundImageTone(val label: String, val overlayAlpha: Float) {
    Clear("\u6e05\u6670", 0.18f),
    Soft("\u67d4\u548c", 0.34f),
    Dark("\u6df1\u8272\u906e\u7f69", 0.52f)
}
data class PersonalizationState(
    val accentPreset: AccentPreset = AccentPreset.Ocean,
    val backgroundStyle: BackgroundStyle = BackgroundStyle.Solid,
    val backgroundImageUri: String? = null,
    val backgroundImageTone: BackgroundImageTone = BackgroundImageTone.Soft
)
enum class TaskStatus(val label: String) { Running("\u8fdb\u884c\u4e2d"), Success("\u5df2\u5b8c\u6210"), Failed("\u5931\u8d25") }
enum class Route { Home, Convert, Video, SpeedTest, Reader }
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

enum class NetworkKind(val label: String) { Wifi("Wi-Fi"), Mobile("\u79fb\u52a8\u6570\u636e"), None("\u65e0\u7f51\u7edc"), Unknown("\u672a\u77e5\u7f51\u7edc") }
enum class SpeedRating(val label: String) { Excellent("\u4f18\u79c0"), Good("\u826f\u597d"), Normal("\u4e00\u822c"), Slow("\u8f83\u6162"), Unknown("--") }
enum class SpeedTestPhase { Idle, Testing, Finished, Failed, Stopped }
data class SpeedTestSample(
    val currentMbps: Double,
    val maxMbps: Double,
    val averageMbps: Double,
    val elapsedMillis: Long,
    val network: NetworkKind
)
data class SpeedTestResult(
    val maxMbps: Double,
    val averageMbps: Double,
    val elapsedMillis: Long,
    val network: NetworkKind,
    val rating: SpeedRating
)
data class SpeedTestUiState(
    val phase: SpeedTestPhase = SpeedTestPhase.Idle,
    val currentMbps: Double = 0.0,
    val maxMbps: Double = 0.0,
    val averageMbps: Double = 0.0,
    val elapsedMillis: Long = 0L,
    val network: NetworkKind = NetworkKind.Unknown,
    val rating: SpeedRating = SpeedRating.Unknown,
    val errorMessage: String? = null
)


data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val changelog: List<String> = emptyList(),
    val forceUpdate: Boolean = false,
    val apkUrl: String = "https://github.com/comstors/ToolBox/releases/download/v${versionName}/MingyuToolBox.apk"
)

enum class UpdatePhase { Idle, Checking, UpToDate, Available, Downloading, ReadyToInstall, Failed }

data class UpdateUiState(
    val phase: UpdatePhase = UpdatePhase.Idle,
    val info: UpdateInfo? = null,
    val dialogVisible: Boolean = false,
    val manualCheck: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedApkPath: String? = null,
    val errorMessage: String? = null
)
enum class ReaderBackground(val label: String) {
    Paper("\u7eb8\u5f20"), Warm("\u6696\u9ec4"), Dark("\u591c\u8bfb")
}
enum class ReaderPageMode(val label: String) {
    Scroll("\u4e0a\u4e0b\u6eda\u52a8"), Horizontal("\u5de6\u53f3\u7ffb\u9875")
}
data class ReaderBook(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val uri: String,
    val totalChars: Int = 0,
    val currentParagraph: Int = 0,
    val scrollOffset: Int = 0,
    val progress: Float = 0f,
    val lastReadAt: Long = System.currentTimeMillis()
)
data class ReaderUiState(
    val books: List<ReaderBook> = emptyList(),
    val activeBook: ReaderBook? = null,
    val paragraphs: List<String> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val fontSizeSp: Float = 18f,
    val lineSpacing: Float = 1.55f,
    val background: ReaderBackground = ReaderBackground.Warm,
    val pageMode: ReaderPageMode = ReaderPageMode.Scroll
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
        ToolModule("video", "\u89c6\u9891\u4e0b\u8f7d", "\u6296\u97f3\u4e0e Bilibili \u94fe\u63a5\u89e3\u6790\u4e0b\u8f7d\u3002", Icons.Rounded.Movie, true, Route.Video),
        ToolModule("speed", "\u7f51\u7edc\u6d4b\u901f", "\u8f7b\u91cf\u4e0b\u8f7d\u6d4b\u901f\uff0c\u5b9e\u65f6\u67e5\u770b\u7f51\u7edc\u8868\u73b0\u3002", Icons.Rounded.Speed, true, Route.SpeedTest),
        ToolModule("reader", "\u5c0f\u8bf4\u9605\u8bfb\u5668", "\u672c\u5730 TXT \u9605\u8bfb\uff0c\u81ea\u52a8\u8bb0\u4f4f\u4e0a\u6b21\u770b\u5230\u7684\u4f4d\u7f6e\u3002", Icons.Rounded.MenuBook, false, Route.Reader)
    )
}