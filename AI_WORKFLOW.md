# AI Workflow

## Required startup sequence for every new Codex window

Before proposing or making any code changes, read these five files first:

1. `PROJECT_STATE.md`
2. `MODULE_MAP.md`
3. `PROJECT_LOG.md`
4. `CODING_RULES.md`
5. `AI_WORKFLOW.md`

## Required behavior before editing code

For any future development task:

1. Read the five project memory files.
2. Read the relevant source files for the requested task.
3. Give the user a concise change plan.
4. Wait for user approval.
5. Only then edit code.

## What the plan should include

Before code edits, the AI should state:

- which files it plans to change
- why those files are the minimum necessary set
- whether business logic will change
- whether `Services.kt` will be touched
- whether Gradle or Manifest would need changes

## Approval rule

- No code edits before user approval.
- If the task would require changing architecture, Gradle, Manifest, persistence format, or `Services.kt` broadly, call that out explicitly before editing.

## Scope discipline

- Keep edits minimal and task-focused.
- Do not refactor unrelated code.
- Do not "improve" nearby code unless the user asks.
- Do not modify generated/build output.

## Mandatory post-edit step

After any future code edit task:

1. Update `PROJECT_LOG.md`.
2. Record what changed and what was intentionally left untouched.

## Special caution areas

These areas require extra care and should be mentioned in the plan when touched:

- `Services.kt`
- `ToolboxViewModel.kt`
- `Models.kt`
- `AndroidManifest.xml`
- `build.gradle.kts`
- `app/build.gradle.kts`

## Current handoff status

This workflow file was created during an initial documentation-only handoff.

In that handoff:

- source code was read
- project memory files were created
- no business code was modified
