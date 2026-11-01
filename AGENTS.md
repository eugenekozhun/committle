# AGENTS.md

Guidance for AI coding agents working in this repository.

## Scope

These instructions apply to the entire repository. A more specific `AGENTS.md` in a subdirectory takes precedence for
files under that directory.

## Project Overview

Committle is a JetBrains IDE plugin written in Kotlin. It generates commit messages from configurable templates and
variables such as `$TASK_ID`, `$TYPE`, `$SCOPE`, and `$CARET_POSITION`.

Key project areas:

- `src/main/kotlin/com/kozhun/commitmessagetemplate/action`: IDE actions and commit-message insertion.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/service`: formatting, replacement, caret, whitespace, and Git logic.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/storage`: persistent settings state and storage.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/ui`: settings UI and editor components.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/language`: CMT language, lexer/parser, highlighting, and completion.
- `src/main/resources/META-INF/plugin.xml`: plugin dependencies, extensions, actions, and registration.
- `src/main/gen`: generated Grammar-Kit/JFlex sources.
- `src/test/kotlin`: JUnit 5 and MockK tests.

## Toolchain and Compatibility

- Use the checked-in Gradle wrapper, currently Gradle `9.8.2`.
- Run Gradle and CI with JDK 21.
- Compile Java/Kotlin bytecode for JVM 17.
- Kotlin plugin `2.3.0` uses Kotlin API `1.9` for compatibility with the minimum IDE.
- Compile against IntelliJ IDEA Community `2023.3.8`.
- Supported platform range: `sinceBuild=233`, `untilBuild=262.*`.
- Manage versions in `build.gradle.kts` and `gradle/wrapper/gradle-wrapper.properties`; do not duplicate version updates
  in documentation without checking those sources.

## Common Commands

Run the narrowest useful verification for the change:

```bash
./gradlew test
./gradlew detekt
./gradlew assemble
```

For build configuration or plugin distribution changes, run:

```bash
./gradlew clean detekt test assemble buildPlugin verifyPluginProjectConfiguration verifyPluginStructure
```

Other useful tasks:

```bash
./gradlew runIde
./gradlew generateParser generateLexer
./gradlew test --rerun-tasks
```

## Generated Sources

- Treat `src/main/gen` as generated output; do not manually edit generated parser, lexer, PSI, or visitor files.
- Edit `CMT.bnf` or `CMT.flex` for language changes, then run `./gradlew generateParser generateLexer`.
- Include generated changes when behavior or generated APIs change.

## Implementation Rules

- Follow the existing Kotlin style, package structure, and service patterns; keep lines within the detekt limit of 160
  characters.
- Keep business logic in services and UI classes focused on composition and event wiring.
- Prefer IntelliJ Platform APIs already used by the project. Avoid new dependencies unless they clearly reduce
  complexity and fit the plugin runtime.
- Register project services with `@Service(Service.Level.PROJECT)` and use the existing `project.service<...>()` pattern.
- Do not retain or asynchronously query `AnActionEvent` after `actionPerformed`; capture required data first. Update
  editors and Swing components on EDT.
- Keep `plugin.xml` synchronized with actions, configurables, file types, language extensions, and notification groups.
- Preserve public plugin IDs, action IDs, extension IDs, shortcut registrations, and settings storage names unless a
  breaking migration is explicitly requested.
- Do not remove the `com.intellij.modules.platform` or `Git4Idea` dependencies without verifying all Git functionality.
- Consider `SettingsMigrator` and existing user state when changing persistent settings.
- Avoid broad refactors while implementing focused changes.

## Tests

- Add or update tests for behavioral changes in services, replacement logic, whitespace formatting, caret handling, and
  branch parsing.
- Use JUnit 5 assertions and MockK, following existing test conventions.
- Prefer focused unit tests unless the behavior depends on the IntelliJ runtime.
- Test both value-present and value-missing cases for formatting and replacements.
- For generated language changes, regenerate sources and run at least `./gradlew test`.

## Documentation, Commits, and Release

- Write public documentation, issues, and PR descriptions in English.
- Keep `README.md` user-focused, `DEVELOPMENT.md` contributor-focused, and this file focused on agent instructions.
  Update documentation when behavior, compatibility, commands, or workflows change.
- Use Conventional Commits; `.commitlintrc.yml` extends `@commitlint/config-conventional`.
- The repository has no `package.json` or Husky hooks. Do not assume npm commands are available.
- `CHANGELOG.md` is managed through the Gradle changelog plugin and release workflow. Do not edit it for routine
  changes unless requested.

## Agent Workflow

- Read relevant files and check `git status --short` before editing. Do not overwrite unrelated user changes.
- Keep edits scoped to the request and review the final diff.
- Do not run destructive Git commands or broad file deletion without explicit user authorization.
- Run meaningful verification on the final deliverable. If a command cannot run, report the exact command and reason.
- Do not commit generated build output, IDE sandbox data, credentials, or session files.
- In the final response, summarize changed files, verification, and any remaining limitations.
