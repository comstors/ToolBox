# Project State

## App purpose

This project is an Android app named `Mingyu的Toolbox` (`com.comstorss.toolbox`).

Its current product定位 is a local personal toolbox with two real tool modules:

1. Image to PDF
2. Word (`.docx`) to PDF
3. Video parsing and download for Douyin and Bilibili links

The app is explicitly presented in UI copy as open source software intended for the author's own use.

## Current implemented features

### 1. App shell and navigation

- Single-activity app with `MainActivity` as the Android entry point.
- Compose-based UI only.
- Manual in-memory route switching via `Route` and `MainTab`.
- Bottom tabs:
  - Home
  - Tasks
  - History
  - Settings
- Splash screen with custom animation before entering the main shell.

### 2. Image to PDF

- User can pick multiple images via system document picker.
- Supported image MIME types currently launched from picker:
  - `image/jpeg`
  - `image/png`
  - `image/webp`
- PDF options currently implemented:
  - Page mode: `A4` or `Original`
  - Orientation: portrait or landscape
  - Image fit: fit or fill
- Output is written to:
  - `Download/MingyuToolBox/PDF`

### 3. Word to PDF

- User can pick a `.docx` file.
- Conversion is a simplified local conversion.
- Current implementation reads:
  - `word/document.xml`
  - embedded images under `word/media/`
- Current renderer supports plain paragraph extraction, simple line/table text flattening, and image insertion into generated PDF.
- Complex DOCX layout fidelity is not guaranteed.

### 4. Video parsing and download

- Input is free text containing a URL; first URL is extracted.
- Supported providers currently implemented:
  - Douyin
  - Bilibili
- Flow:
  - parse shared link
  - inspect whether downloadable direct URL exists
  - download to local file if available
- Output is written to:
  - `Download/MingyuToolBox/Video`
- Network access is required only for video parsing/downloading.

### 5. Task, history, and file actions

- Running tasks are tracked in memory as `ToolboxTask`.
- History records are persisted in `SharedPreferences` via `HistoryStore`.
- History supports:
  - open
  - share
  - delete record
- For successful records with stored URI, file open/share uses Android intents.

### 6. Theme and settings

- Theme modes:
  - System
  - Light
  - Dark
- Theme choice is stored in `SharedPreferences`.
- Cache cleanup is exposed in settings and deletes `cacheDir` recursively.

## Tech stack actually in use

- Kotlin
- Android SDK
- Jetpack Compose
- Material 3
- AndroidX lifecycle ViewModel
- Coroutines
- OkHttp
- `PdfDocument` for PDF generation
- `SharedPreferences` for theme and history persistence
- `MediaStore` / Downloads output writing

## Dependencies present but not clearly used in current source

The following dependencies exist in `app/build.gradle.kts` but are not evident in the current Kotlin source files:

- `androidx.datastore:datastore-preferences`
- `androidx.navigation:navigation-compose`

Current source uses manual route state and `SharedPreferences`, not Compose Navigation or DataStore.

## Current architecture

The app is small and file-centered rather than layered into many packages.

High-level structure:

1. `MainActivity` boots the app.
2. `ToolboxApp` applies theme and enters Compose root.
3. `ToolboxViewModel` owns app state and orchestrates business actions.
4. `Services.kt` contains almost all non-UI business/service logic:
   - PDF output
   - DOCX parsing
   - history persistence
   - file actions
   - video provider parsing/downloading
5. `ScreensMain.kt` contains splash, shell, home, convert, and video screens.
6. `ScreensLists.kt` contains tasks, history, settings, and reusable list rendering.
7. `ThemeAndComponents.kt` contains theme system and reusable Compose components.

This is effectively a compact MVVM-style Compose app with a heavy service file and no repository/domain module split.

## Things that should not be changed casually

### `Services.kt`

This is the most sensitive file in the project because it concentrates:

- file output behavior
- DOCX parsing behavior
- PDF generation behavior
- persistent history format
- external video provider parsing
- network download behavior

Small edits here can silently break multiple features at once.

### `ToolboxViewModel.kt`

This file is the app's state coordinator. It ties UI actions to services, history updates, notices, and task progress.

### `Models.kt`

Enums and data classes here define the app's UI flow, persistence expectations, and module registry. Renaming or reshaping them can ripple through the whole app.

### `AndroidManifest.xml`

Current app launch behavior, app label/icon, and permissions are defined here. Do not modify casually.

### Gradle files

Plugin versions, SDK levels, Java/Kotlin target versions, and dependency set are stable project assumptions. Do not change without explicit user approval.

## Practical constraints from current source

- The app is a single-module Android application: `:app`.
- `MainActivity` is intentionally thin.
- Navigation is state-driven inside Compose, not nav-graph driven.
- Theme persistence currently depends on `SharedPreferences`.
- History persistence currently depends on a JSON string stored in `SharedPreferences`.
- Video support is intentionally limited to Douyin and Bilibili.
- DOCX conversion is intentionally simplified and should not be described as full Word rendering support.
