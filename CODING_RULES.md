# Coding Rules

## Non-negotiable rules

1. Do not change architecture casually.
2. Do not do opportunistic refactors.
3. Do not modify unrelated files while working on a focused task.
4. Do not change Gradle files without explicit user approval.
5. Do not change `AndroidManifest.xml` without explicit user approval.
6. Do not rewrite `Services.kt` without explicit user approval and a clear scoped reason.
7. After every code change task, update `PROJECT_LOG.md`.

## Architecture rules

- Preserve the current single-activity Compose structure unless the user explicitly asks for an architectural change.
- Preserve `ToolboxViewModel` as the central state/orchestration point unless the user explicitly asks for a redesign.
- Preserve current route/tab flow based on `Route` and `MainTab` unless navigation changes are explicitly requested.
- Do not replace `SharedPreferences` with DataStore just because DataStore already exists in dependencies.
- Do not replace manual screen switching with Navigation Compose just because the dependency already exists.

## Scope control rules

- Only change files required for the requested task.
- Do not "clean up nearby code" unless the user asked for it.
- Do not rename enums, models, routes, tabs, or history schema fields without explicit approval.
- Do not edit generated/build output directories as part of normal feature work.
- Do not touch release artifacts or APK output files.

## Service-layer safety rules

- Treat `Services.kt` as a high-risk integration file.
- Do not rewrite the full file for small changes.
- Prefer minimal, local edits when service behavior must change.
- Be especially careful around:
  - `OutputFileService`
  - `ImagePdfService`
  - `DocxPdfService`
  - `HistoryStore`
  - `FileActionService`
  - `VideoService`
  - `DouyinProvider`
  - `BilibiliProvider`

## Gradle and manifest rules

- Do not upgrade plugin versions unless the user explicitly requests it.
- Do not add or remove dependencies without a task-specific reason.
- Do not change SDK levels casually.
- Do not modify app permissions casually.
- Do not modify application ID, app label, icon, or launcher wiring unless explicitly requested.

## UI rules

- Do not "optimize UI" unless the user explicitly asks for UI changes.
- Do not restyle `ThemeAndComponents.kt` during unrelated work.
- Do not alter splash behavior, tabs, or screen copy during non-UI tasks.

## Logging rule

Every future AI code-edit task must append or update `PROJECT_LOG.md` with:

- date
- task summary
- files changed
- whether business logic was changed
- whether Gradle/Manifest was changed

## Documentation-first rule for risky work

If a future task involves `Services.kt`, persistence shape, output paths, permissions, or architecture:

1. Read the five project memory docs first.
2. Explain planned changes before editing code.
3. Wait for user approval.
