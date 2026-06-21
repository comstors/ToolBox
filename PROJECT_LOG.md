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