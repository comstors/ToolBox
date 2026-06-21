# Comst Toolbox

纯本地运行的安卓模块化工具箱 MVP。

## 已实现

- Kotlin + Jetpack Compose Android 项目骨架
- 启动动画页：包含 `作者微信 comstorss`、`开源软件，仅供自己使用`
- 首页、任务、历史、设置、格式转换、视频下载页面
- 浅色 / 深色 / 跟随系统主题选择，并本地保存
- 所有主要页面切换、按钮点击、Tab、任务进度、提示条均带动画
- 模块注册结构：格式转换、视频下载
- 图片转 PDF：使用 Android `PdfDocument` 本地生成 A4 PDF
- 视频 Provider 架构：抖音、Bilibili 独立封装

## 当前边界

- Word 转 PDF 已预留入口和任务流程，DOCX 排版引擎后续接入。
- 抖音 / Bilibili 模块已完成本地 Provider 架构和链接识别；平台未提供直链时不会强行绕过下载。
- 当前环境没有 `java`、`gradle`、`adb`，所以本机未执行编译验证。

## 打开方式

用 Android Studio 打开本目录：

```text
D:\Mingyu ToolBox\ToolBox
```

等待 Gradle 同步后运行 `app` 模块。
