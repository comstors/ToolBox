# Project Log

## Initial AI Project Handoff

Date: 2026-06-21

Summary:

- Read the existing Android project source and project configuration.
- Created project memory and handoff documents for future AI development.
- Did not modify any Kotlin source files.
- Did not modify any Gradle files.
- Did not modify `AndroidManifest.xml`.
- Did not add features, fix bugs, refactor code, or change UI behavior.

Documents created in this handoff:

1. `PROJECT_STATE.md`
2. `MODULE_MAP.md`
3. `PROJECT_LOG.md`
4. `CODING_RULES.md`
5. `AI_WORKFLOW.md`

Observed project shape during handoff:

- Single Android app module: `:app`
- Single package for main source: `com.comstorss.toolbox`
- Main functional areas:
  - PDF conversion
  - video parsing/download
  - tasks/history/settings UI

Key caution recorded for future AI sessions:

- `Services.kt` is the highest-risk file and should not be rewritten casually.
- `ToolboxViewModel.kt` is the central state and orchestration file.
- Future AI changes should start with a plan and wait for user approval before code edits.

## 2026-06-21 Video Screen UX Pass

Summary:

- Added a dedicated clipboard paste button on the video parsing screen.
- Added explicit parsing feedback so users can see when link parsing is in progress.
- Kept parsing and download feedback visually consistent while giving them different loading labels.
- Slightly relaxed the shell header spacing to reduce the cramped top-text feel on the video page.

Files changed:

- `app/src/main/java/com/comstorss/toolbox/ScreensMain.kt`
- `app/src/main/java/com/comstorss/toolbox/ThemeAndComponents.kt`
- `PROJECT_LOG.md`

Business logic changed:

- No service-layer business logic was changed.
- UI interaction behavior changed on the video screen only.

Gradle or Manifest changed:

- No.

## 2026-06-21 Header Polish Follow-up

Summary:

- Reverted the heavier header sizing change from the video screen UX pass.
- Kept the original header height and spacing style, then moved the header content down slightly with a small offset.
- Replaced the back-to-home label with Unicode escapes to avoid Windows encoding corruption.

Files changed:

- `app/src/main/java/com/comstorss/toolbox/ScreensMain.kt`
- `PROJECT_LOG.md`

Business logic changed:

- No.

Gradle or Manifest changed:

- No.

## 2026-06-21 Network Speed Test Lite

Summary:

- Added a lightweight download-only network speed test module.
- Added a home module card and route for network speed testing.
- Added a speed test screen with a gauge, live current speed, stop/retry action, and result summary.
- Added a small OkHttp-based speed test service that uses candidate public test sources and discards downloaded data.
- Added network type detection through Android connectivity APIs.

Files changed:

- `app/src/main/java/com/comstorss/toolbox/Models.kt`
- `app/src/main/java/com/comstorss/toolbox/ToolboxViewModel.kt`
- `app/src/main/java/com/comstorss/toolbox/ScreensMain.kt`
- `app/src/main/java/com/comstorss/toolbox/SpeedTestScreen.kt`
- `app/src/main/java/com/comstorss/toolbox/NetworkSpeedTestService.kt`
- `app/src/main/AndroidManifest.xml`
- `PROJECT_LOG.md`

Business logic changed:

- Added download-only network speed test behavior.
- Existing conversion and video download logic was not changed.

Gradle or Manifest changed:

- Gradle: No.
- Manifest: Added `ACCESS_NETWORK_STATE` for network type detection.
## 2026-06-21 Speed Test Startup Fix

Summary:

- Changed the speed test flow so it starts sampling immediately instead of blocking on a preflight source probe.
- Let parallel workers try candidate test sources directly and rotate to the next source on failure.
- Shortened network timeouts so unavailable public test sources do not make the UI feel stuck.

Files changed:

- `app/src/main/java/com/comstorss/toolbox/NetworkSpeedTestService.kt`
- `PROJECT_LOG.md`

Business logic changed:

- Improved startup behavior and source fallback for network speed testing.

Gradle or Manifest changed:

- No.
## 2026-06-21 Speed Test Runtime Stabilization

- Fixed Network Speed Test Lite startup/runtime behavior after emulator testing showed the page could enter testing but public source handling was too fragile.
- Removed the blocking preflight strategy in favor of direct source rotation, added a hard deadline canceller for active OkHttp calls, shortened network timeouts, and avoided cache-busting query parameters on static test files.
- Adjusted speed formatting so very low speeds show useful decimal values instead of looking like `0.0`.
- Verified `assembleDebug` succeeds and installed the debug APK on the emulator. Emulator run confirmed the speed test starts, completes in about 10 seconds, and shows max/average speed, elapsed time, network type, and rating.
- Note: the emulator measured very low speed against the current public test sources. Accuracy can be improved later by replacing or expanding the source list with faster, region-appropriate download endpoints.
## 2026-06-21 China Speed Test Sources

- Replaced overseas speed test sources with China-based mirror download sources after user testing showed results around `0.44 Mbps` while other speed test sites were near `100 Mbps`.
- Current source list uses TUNA, BFSU, Huawei Cloud, and HIT Ubuntu mirror files. The test still discards downloaded bytes and does not save files.
- Increased download parallelism from 2 to 4 connections and enlarged the read buffer to better saturate normal broadband/mobile connections.
- Added a browser-like User-Agent for mirror compatibility.
- Verified `assembleDebug` succeeds. Emulator test with China sources completed in about 10 seconds and reported substantially higher throughput than the previous overseas-source build.
## 2026-06-21 Personalization Settings Draft

- Added a first-pass appearance personalization system inside Settings.
- Users can choose an accent color preset, background style, custom background image, image overlay tone, and reset appearance to defaults.
- Personalization is persisted with the existing settings `SharedPreferences`; no DataStore migration or new dependency was introduced.
- Theme rendering now applies the selected accent color globally and can render a selected background image behind the app shell.
- No conversion, video download, history, or speed test business logic was changed.
- Gradle and AndroidManifest were not changed.
- Verified `assembleDebug` succeeds. Emulator install succeeded, but automated coordinate tapping did not reliably switch to the Settings tab, so settings UI interaction still needs a quick manual check in Android Studio/emulator.
## 2026-06-21 Personalization Theme Safety Fix

- Corrected the first personalization pass after user feedback that it changed the original light/dark theme too aggressively and caused settings instability.
- Restored the original light and dark theme baseline colors, including text colors, card colors, background colors, and Material primary colors for the default preset.
- Changed the default personalization background style back to the original clean baseline so old light/dark themes are not visually altered by default.
- Kept extra color presets as opt-in accent presets only;正文文字颜色 remains controlled by the original light/dark theme palette.
- `assembleDebug` succeeds after the fix. Local logcat did not reproduce a Settings-tab fatal exception, but the risky theme-palette pollution was removed.
## 2026-06-21 Settings Crash Fix

- Fixed the Settings-tab crash introduced by the personalization draft.
- Root cause: accent preset colors were stored as `ULong` and passed into Compose `Color(ULong)`, which expects Compose's packed internal color format rather than normal ARGB. This caused `ArrayIndexOutOfBoundsException` during Compose drawing when the Settings page rendered color dots/backgrounds.
- Changed accent preset color values to normal ARGB `Long` values so Compose color creation follows the same pattern as the rest of the project.
- Reproduced the crash with adb by launching the app and tapping Settings, then verified the same path enters Settings without a crash after the fix.
- Verified `assembleDebug` succeeds.
## 2026-06-21 Settings Folding Layout

- Reduced Settings page visual weight by keeping Theme visible and moving optional sections into collapsible cards.
- Appearance personalization is now collapsed by default and shows a short summary of the current accent/background choice.
- Cache actions are collapsed by default.
- About information was changed from a full card into a lightweight footer-style text block.
- This change only affects Settings layout; theme logic, personalization persistence, business logic, Gradle, Manifest, and Services were not changed.
- Verified `assembleDebug` succeeds. Emulator UI re-check was blocked by adb moving to `authorizing` after server restart.
## 2026-06-21 Settings Fold Click Feedback Fix

- Removed the default rectangular click highlight/ripple from collapsible Settings card headers.
- Kept the existing fold/unfold behavior and arrow rotation.
- This change only affects Settings click feedback; no theme, personalization persistence, business logic, Gradle, Manifest, or Services changes were made.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Background Image Photo Picker

- Changed the appearance personalization background image selector from document/file picking to Android's visual media photo picker.
- The Settings page now opens a gallery-style image picker for custom background images instead of a folder-oriented document picker where supported by the system.
- Kept existing personalization state, theme behavior, background rendering, Gradle, Manifest, and service logic unchanged.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Local TXT Reader Lite

- Added a first-pass local TXT novel reader module.
- Added a home entry and route for the reader without changing the existing navigation architecture.
- Added a lightweight reader shelf with TXT import, book list, continue-reading entry, progress display, and record removal that does not delete the original file.
- Added a reading page with paragraph-based rendering, automatic progress saving, font size controls, line spacing controls, and reader-only background choices.
- Added `ReaderService.kt` for TXT reading, UTF-8/GBK decoding, URI read permission handling, and reader shelf persistence through SharedPreferences.
- Added reader state and events in `ToolboxViewModel.kt` for import, open, close, progress save, appearance controls, and shelf removal.
- Did not modify Gradle, AndroidManifest, existing `Services.kt`, PDF conversion, video download, speed test logic, or global light/dark theme behavior.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Reader Immersive Controls Pass

- Reworked the TXT reader page into an immersive reading layout where the text is the default focus.
- Added tap-to-show/tap-to-hide bottom controls with smooth fade, slide, and scale transitions.
- Added a small side-mounted gear affordance for reader settings so typography and background controls do not cover the main reading area by default.
- Added a bottom reader control card with shelf, table of contents, night mode, and settings entry points.
- Added a lightweight table-of-contents panel using simple TXT chapter heading detection.
- Moved font size, line spacing, background, and manual night mode controls into the reader settings panel.
- Cleaned up reader UI/ViewModel Chinese copy using escaped strings to avoid Windows encoding corruption.
- Did not modify Gradle, AndroidManifest, existing `Services.kt`, global light/dark theme behavior, PDF conversion, video download, or speed test logic.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Reader Header And Page Mode

- Changed the reader route header so active reading shows the current book title as the main title and the nearest detected chapter title as the subtitle.
- Removed the fixed `Local TXT Reading` style header copy while reading; the shelf state now uses a lighter reader/shelf fallback.
- Added a reader page mode preference with `Scroll` and `Horizontal` options.
- Added a smooth horizontal pager reading mode that approximates side-to-side page turning without adding dependencies.
- Persisted reader typography, background, and page mode preferences through the existing settings SharedPreferences.
- Kept scroll mode as the default and preserved paragraph-based progress saving for both modes.
- Did not modify Gradle, AndroidManifest, existing `Services.kt`, global theme behavior, PDF conversion, video download, or speed test logic.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Reader Title And Paging Fix

- Adjusted the reader route header so active reading no longer shows a chapter subtitle under the book title.
- Added a compact reader-title sizing rule: normal titles stay prominent, longer book names shrink before falling back to ellipsis.
- Reworked horizontal reader pagination to estimate page capacity from actual screen width, screen height, font size, and line spacing instead of using one fixed character count.
- Split oversized paragraphs during horizontal pagination so a single long paragraph is less likely to overflow and get clipped at the bottom of the page.
- Did not modify Gradle, AndroidManifest, existing `Services.kt`, import/storage behavior, or global theme behavior.
## 2026-06-22 Reader Chapter Header And Page Progress Fix

- Restored the current chapter subtitle under the reader book title.
- Tightened the reader title font sizing so the book title and chapter subtitle can share the header without pushing the reading card down.
- Fixed horizontal page mode progress so it advances by current page count instead of staying tied only to paragraph index.
- Kept scroll reading progress behavior unchanged.
- Did not modify Gradle, AndroidManifest, existing `Services.kt`, import/storage behavior, or global theme behavior.
- Verified `assembleDebug` succeeds.
## 2026-06-22 In-App Update Flow

- Added an in-app update system that checks `update/version.json` on GitHub raw and compares the remote `versionCode` with the installed app version.
- Added startup auto-check behavior and a manual Settings entry for checking updates on demand.
- Added update dialog UI with version information, changelog, download progress, retry/error messaging, and install action.
- Added `UpdateService.kt` to fetch update metadata, download the GitHub Release APK, and open Android's system package installer through FileProvider.
- Added FileProvider paths and `REQUEST_INSTALL_PACKAGES` permission required for user-confirmed APK installation.
- Added `update/version.json` as the remote update metadata template. Future releases use tag `v{versionName}` and APK name `MingyuToolBox.apk`.
- Did not modify Gradle, existing `Services.kt`, conversion/video/speed-test/reader business logic, or global theme behavior.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Settings Update Entry Polish

- Moved the manual update check entry out of its own collapsible Settings card.
- Added a compact update button beside the Settings theme header so users can check updates without making Settings feel heavier.
- Kept startup auto-check, update dialog, APK download, and installer flow unchanged.
- Did not modify Gradle, AndroidManifest, `UpdateService.kt`, existing `Services.kt`, or unrelated app features.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Update Button State Labels

- Adjusted the compact Settings update button copy so it defaults to `Check Update` instead of implying an update is already available.
- Added an `UpToDate` update phase for manual checks with no newer version.
- The Settings update button now shows clear states: checking, up to date, update available, downloading, and install.
- Kept startup auto-check, update dialog, APK download, and installer flow unchanged.
- Did not modify Gradle, AndroidManifest, `UpdateService.kt`, existing `Services.kt`, or unrelated app features.
- Verified `assembleDebug` succeeds.
## 2026-06-22 Update Download Task Progress

- Connected in-app update APK downloads to the existing Tasks screen.
- Starting an update download now creates an app update download task.
- Download progress updates both the update dialog and the Tasks progress bar.
- Successful downloads mark the task as complete; failed downloads mark it as failed with the error message.
- Did not modify Gradle, AndroidManifest, `UpdateService.kt`, existing `Services.kt`, or unrelated app features.
- Verified `assembleDebug` succeeds.
## 2026-06-22 v1.0 Version Metadata

- Set app `versionName` to `1.0` for the first GitHub release tag `v1.0`.
- Updated `update/version.json` to point the in-app update metadata at version `1.0` with current release notes.
- Kept `versionCode` at `1` for the initial release.