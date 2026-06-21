# Module Map

## Top-level project structure

- `build.gradle.kts`
  - Root plugin declarations only.
- `settings.gradle.kts`
  - Defines repositories, root project name, and includes `:app`.
- `gradle.properties`
  - Gradle and Android build flags.
- `app/build.gradle.kts`
  - Android app module configuration and dependencies.
- `app/src/main/AndroidManifest.xml`
  - App metadata, permissions, and launcher activity.

## Main Android source set

All primary Kotlin source currently lives under:

- `app/src/main/java/com/comstorss/toolbox`

Files in this package:

1. `MainActivity.kt`
2. `Models.kt`
3. `ToolboxViewModel.kt`
4. `Services.kt`
5. `ScreensMain.kt`
6. `ScreensLists.kt`
7. `ThemeAndComponents.kt`

## MainActivity

File: [MainActivity.kt](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/java/com/comstorss/toolbox/MainActivity.kt)

Role:

- Android launcher activity.
- Creates Compose content.
- Instantiates `ToolboxViewModel` using `ToolboxViewModel.factory(application)`.
- Hands the ViewModel to `ToolboxApp`.

Importance:

- This is the Android runtime entry point.
- It is intentionally very small and should stay small.

## Models

File: [Models.kt](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/java/com/comstorss/toolbox/Models.kt)

Role:

- Defines app-wide enums and data models.
- Defines module registry used by the home screen.

Main contents:

- `ThemeMode`
- `TaskStatus`
- `Route`
- `ConversionType`
- `PdfPageMode`
- `PdfOrientation`
- `PdfImageFit`
- `MainTab`
- `ToolModule`
- `ToolboxTask`
- `PdfOptions`
- `ConversionOutput`
- `HistoryRecord`
- `VideoInfo`
- `VideoDownloadOutput`
- `ModuleRegistry`

Why it matters:

- UI flow depends on `Route` and `MainTab`.
- Conversion behavior depends on `PdfOptions` and related enums.
- Persistence and history rendering depend on `HistoryRecord`.
- Home screen content depends on `ModuleRegistry.modules`.

## ToolboxViewModel

File: [ToolboxViewModel.kt](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/java/com/comstorss/toolbox/ToolboxViewModel.kt)

Role:

- Main state holder and orchestration layer for the app.
- Connects Compose UI events to services.

Owned state:

- theme
- tasks
- history
- current parsed video info
- global notice text

Key responsibilities:

- persist theme selection
- clear cache
- run image-to-PDF conversion
- run DOCX-to-PDF conversion
- parse video links
- download parsed videos
- open/share/delete history records
- manage task progress and completion state

Why it matters:

- This is the central coordination point of the app.
- UI side effects and service calls are routed through here.

## Services

File: [Services.kt](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/java/com/comstorss/toolbox/Services.kt)

Role:

- Primary business logic file.
- Contains most non-UI behavior in the entire project.

Contained classes and responsibilities:

- `OutputFileService`
  - Creates PDF/video files in Downloads.
- `ImagePdfService`
  - Converts selected images into PDF pages.
- `DocxPdfService`
  - Reads `.docx` zip content and generates a simplified PDF.
- `HistoryStore`
  - Saves/loads JSON-serialized history records from `SharedPreferences`.
- `FileActionService`
  - Open/share/delete output files via Android APIs.
- `VideoProvider`
  - Provider interface for video platform parsing.
- `DouyinProvider`
  - Douyin link matching and parsing.
- `BilibiliProvider`
  - Bilibili link matching and parsing.
- `VideoService`
  - Dispatches parsing to providers and downloads resolved videos.

Important implementation details:

- PDF output path:
  - `Download/MingyuToolBox/PDF`
- Video output path:
  - `Download/MingyuToolBox/Video`
- Uses `MediaStore` on Android Q+.
- Uses direct file output fallback for older Android versions.
- Uses `OkHttpClient` for provider parsing and video download.

Risk level:

- Highest-risk file in the project.
- Many unrelated user-visible features depend on this single file.

## ScreensMain

File: [ScreensMain.kt](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/java/com/comstorss/toolbox/ScreensMain.kt)

Role:

- Main Compose UI flow file.
- Contains top-level shell and core tool screens.

Main composables:

- `AppRoot`
  - switches from splash to main shell
- `SplashScreen`
  - animated startup screen
- `MainShell`
  - overall screen shell, route switching, tabs, notice placement
- `BottomTabs`
  - bottom tab navigation UI
- `HomeScreen`
  - module list entry screen
- `ModuleCard`
  - module card UI
- `ConvertScreen`
  - image/Word conversion flow UI
- `ConvertPanel`
  - shared conversion panel container
- `OptionRow`
  - option chip row for PDF settings
- `VideoScreen`
  - video parse/download flow UI
- `VideoCard`
  - parsed video result card

Behavior notes:

- Navigation is manual state switching, not `NavHost`.
- Conversion screen includes both image and Word tabs.
- Video screen is tightly coupled to `ToolboxViewModel.video` and task state.

## ScreensLists

File: [ScreensLists.kt](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/java/com/comstorss/toolbox/ScreensLists.kt)

Role:

- Secondary screens and list-centric UI.

Main composables:

- `TaskScreen`
- `HistoryScreen`
- `TaskCard`
- `HistoryCard`
- `SettingsScreen`
- `ThemeRow`
- `ListOrEmpty`
- `Feature`

Behavior notes:

- Tasks screen is read-only rendering of in-memory task state.
- History screen exposes open/share/delete actions back through the ViewModel.
- Settings currently contains theme, cache cleanup, and app/about info.

## ThemeAndComponents

File: [ThemeAndComponents.kt](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/java/com/comstorss/toolbox/ThemeAndComponents.kt)

Role:

- Theme definition and reusable Compose building blocks.

Main contents:

- `NoticeTone`
- `ToolboxPalette`
- `toolboxPalette()`
- `ToolboxApp`
- `ComstTheme`
- `GlassCard`
- `SectionHeader`
- `PrimaryActionButton`
- `SecondaryActionButton`
- `SmoothButton`
- `Pressable`
- `OptionChip`
- `InfoToast`
- `Notice`
- `tintedFieldColors`
- `statusIcon`

Why it matters:

- Defines the visual identity of the app.
- Also contains app root theming entry via `ToolboxApp`.
- UI polish and shared components are centralized here.

## AndroidManifest

File: [AndroidManifest.xml](/D:/Mingyu%20ToolBox/ToolBox/app/src/main/AndroidManifest.xml)

Role:

- Declares app-level Android metadata.

Current notable contents:

- permission: `INTERNET`
- permission: `WRITE_EXTERNAL_STORAGE` with `maxSdkVersion="28"`
- app label: `Mingyu的Toolbox`
- app icon: `@drawable/ic_launcher`
- launcher activity: `.MainActivity`
- theme: `@style/Theme.ComstToolbox`

Why it matters:

- Determines installation/runtime identity and launch behavior.
- Permission edits here can affect file and network behavior.

## Gradle files

### Root build file

File: [build.gradle.kts](/D:/Mingyu%20ToolBox/ToolBox/build.gradle.kts)

Role:

- Declares plugin versions for:
  - Android application plugin
  - Kotlin Android plugin
  - Kotlin Compose plugin

### Settings file

File: [settings.gradle.kts](/D:/Mingyu%20ToolBox/ToolBox/settings.gradle.kts)

Role:

- Controls plugin/dependency repositories.
- Sets `rootProject.name = "ComstToolbox"`.
- Includes the `:app` module.

### Gradle properties

File: [gradle.properties](/D:/Mingyu%20ToolBox/ToolBox/gradle.properties)

Role:

- JVM memory settings
- AndroidX enablement
- non-transitive R class setting
- compile SDK suppression flag
- UTF-8 file encoding

### App module build file

File: [app/build.gradle.kts](/D:/Mingyu%20ToolBox/ToolBox/app/build.gradle.kts)

Role:

- Android module identity and build configuration.

Current key settings:

- namespace: `com.comstorss.toolbox`
- applicationId: `com.comstorss.toolbox`
- compileSdk: 36
- minSdk: 23
- targetSdk: 35
- versionCode: 1
- versionName: `1.0.0`
- Java target: 17
- Compose enabled

Current key dependencies:

- Compose BOM
- Activity Compose
- Material 3
- Compose animation/foundation/runtime/ui
- material icons extended
- AndroidX core-ktx
- lifecycle runtime/viewmodel compose
- coroutines Android
- OkHttp

Dependencies present but not obviously used from current source:

- DataStore Preferences
- Navigation Compose

## Resources

Relevant resource files:

- `app/src/main/res/drawable/ic_launcher.jpg`
- `app/src/main/res/drawable-nodpi/splash_avatar_rounded.png`
- `app/src/main/res/values/styles.xml`

Notes:

- `styles.xml` defines `Theme.ComstToolbox` as a no-action-bar platform theme.
- Compose handles most of the actual runtime UI styling.
