# Mingyu Toolbox

一个使用 Kotlin 和 Jetpack Compose 开发的 Android 本地工具箱。

项目定位是轻量、自用、开源的移动端工具集合，优先保证本地处理、界面顺手、功能稳定。

## 当前功能

- 图片转 PDF：选择多张图片，本地生成 PDF。
- Word 转 PDF：支持 `.docx` 简化转换为 PDF。
- 视频解析下载：支持抖音和 Bilibili 分享链接解析与下载。
- 网络测速 Lite：下载测速、实时速度、最大速度、平均速度、网络类型和评级。
- 本地 TXT 小说阅读器：导入 TXT、书架管理、自动保存阅读进度、目录识别、上下滚动/左右翻页、字号/行距/背景设置。
- 任务和历史：查看运行任务，打开、分享、删除历史记录。
- 设置与个性化：深色/浅色/跟随系统主题，预设配色，自定义背景图，缓存清理，折叠式设置布局。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX ViewModel / Lifecycle
- Kotlin Coroutines
- OkHttp
- Android `PdfDocument`
- SharedPreferences
- MediaStore / Android 文件选择器

## 项目结构

主要源码位于：

```text
app/src/main/java/com/comstorss/toolbox
```

核心文件：

- `MainActivity.kt`：Android 入口。
- `Models.kt`：路由、模块、任务、历史、阅读器等数据模型。
- `ToolboxViewModel.kt`：主要状态管理和功能调度。
- `Services.kt`：PDF、DOCX、视频、历史、文件动作等服务逻辑。
- `ScreensMain.kt`：主界面、首页、格式转换、视频下载等核心 Compose 页面。
- `ScreensLists.kt`：任务、历史、设置页面。
- `ThemeAndComponents.kt`：主题、卡片、按钮、提示等复用组件。
- `ReaderScreen.kt`：本地 TXT 阅读器界面。
- `ReaderService.kt`：TXT 导入、解码、书架和进度保存。
- `SpeedTestScreen.kt` / `NetworkSpeedTestService.kt`：网络测速 Lite。

## 构建

使用 Android Studio 打开项目目录：

```text
D:\Mingyu ToolBox\ToolBox
```

或者在本机 Android 环境配置完成后执行：

```powershell
.\gradlew.bat assembleDebug --no-daemon
```

## AI 开发约定

这个项目已经建立 AI 项目记忆文档。后续新 Codex 窗口应先阅读：

- `PROJECT_STATE.md`
- `MODULE_MAP.md`
- `PROJECT_LOG.md`
- `CODING_RULES.md`
- `AI_WORKFLOW.md`

开发流程固定为：

```text
限定计划 -> 用户批准 -> 执行 -> 编译验证 -> 更新 PROJECT_LOG.md
```

## 注意

- 不要随意重构当前架构。
- 不要无理由修改 Gradle 或 AndroidManifest。
- `Services.kt` 是高风险文件，不要重写。
- 本项目仍是个人工具箱，不是完整商业平台。