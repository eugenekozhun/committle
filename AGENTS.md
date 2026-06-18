# AGENTS.md

Guidance for Codex and other coding agents working in this repository.

## Scope

These instructions apply to the whole repository. If a more specific `AGENTS.md` is added in a subdirectory, that file
takes precedence for files under that subdirectory.

## Project Overview

Committle is a JetBrains IDE plugin written in Kotlin. It helps generate commit messages from configurable templates and
custom variables.

Important project areas:

- `src/main/kotlin/com/kozhun/commitmessagetemplate/action`: IDE actions, including commit-message insertion.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/service`: business logic for formatting, caret handling, whitespace
  handling, branch parsing, and replacement values.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/storage`: persistent settings state and storage.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/ui`: settings UI and editor components.
- `src/main/kotlin/com/kozhun/commitmessagetemplate/language`: custom CMT language support, lexer/parser integration,
  highlighting, completion, and file type.
- `src/main/resources/META-INF/plugin.xml`: IntelliJ plugin registration, actions, extensions, dependencies, and
  compatibility.
- `src/main/gen`: generated Grammar-Kit/JFlex Java sources used by the CMT language implementation.
- `src/test/kotlin`: JUnit 5 and MockK tests, mostly mirroring service packages.

## Toolchain

- Use the checked-in Gradle wrapper: `./gradlew`.
- Use JDK 21 to match CI and the Gradle Java toolchain.
- The project compiles Java/Kotlin bytecode for JVM 17.
- Kotlin plugin version is managed in `build.gradle.kts`.
- IntelliJ Platform Gradle plugin targets IntelliJ IDEA Community `2023.1.5` with plugin compatibility configured in
  `plugin.xml`/`build.gradle.kts`.

## Common Commands

Run the closest useful verification for the change:

```bash
./gradlew test
```

```bash
./gradlew detekt
```

```bash
./gradlew assemble
```

Full CI-equivalent local check:

```bash
./gradlew clean detekt test assemble --info
```

Run the plugin in a development IDE:

```bash
./gradlew runIde
```

Regenerate language sources after changing the grammar or lexer:

```bash
./gradlew generateParser generateLexer
```

## Generated Sources

- Treat `src/main/gen` as generated output.
- Do not manually edit generated parser, lexer, PSI, or visitor files unless the user explicitly asks for an emergency
  patch.
- For language changes, edit:
    - `src/main/kotlin/com/kozhun/commitmessagetemplate/language/grammar/CMT.bnf`
    - `src/main/kotlin/com/kozhun/commitmessagetemplate/language/lexer/CMT.flex`
- Then run `./gradlew generateParser generateLexer` and include the generated changes if behavior or APIs changed.

## Coding Standards

- Follow the existing Kotlin style and package structure.
- Keep code compatible with JVM 17.
- Prefer IntelliJ Platform APIs and services already used in the project.
- Register project-level services with `@Service(Service.Level.PROJECT)` and expose them through
  `project.service<...>()` companion helpers when matching existing patterns.
- Keep business logic in services; keep UI classes focused on UI composition and event wiring.
- Avoid broad refactors while fixing focused issues.
- Avoid introducing new dependencies unless they clearly reduce complexity and fit the plugin runtime.
- Keep line length within the detekt limit of 160 characters.
- Use meaningful names for template anchors, replacements, and settings fields; this project relies on clear variable
  semantics.
- Preserve existing public plugin IDs, action IDs, extension IDs, and settings storage names unless the user explicitly
  requests a breaking migration.

## IntelliJ Plugin Notes

- `plugin.xml` is part of the runtime contract. Keep it in sync with implementation classes when renaming or moving
  actions, configurables, file types, language extensions, or notification groups.
- The plugin depends on `com.intellij.modules.platform` and `Git4Idea`; do not remove those dependencies without
  verifying all Git-related services.
- The default shortcut for inserting a pattern message is registered in `plugin.xml`; avoid changing it unless
  requested.
- Changes affecting persistent settings should consider migration through `SettingsMigrator` and compatibility with
  existing user state.

## Tests

- Add or update tests for behavioral changes in services, replacement logic, whitespace formatting, caret handling, and
  branch parsing.
- Use JUnit 5 assertions and MockK, following the existing tests.
- Prefer focused unit tests over full IDE integration tests unless the change depends on IntelliJ runtime behavior.
- When changing formatting or replacement behavior, test both value-present and value-missing cases.
- When changing generated language behavior, run parser/lexer generation and at least `./gradlew test`.

## Commit, Changelog, and Release

- Commit messages must follow Conventional Commits.
- `.commitlintrc.yml` extends `@commitlint/config-conventional`.
- `DEVELOPMENT.md` mentions Husky and npm setup for commit hooks, but this repository snapshot does not currently
  contain `package.json` or `.husky`; do not assume npm commands are available unless those files are added.
- The changelog is managed by Gradle/GitHub workflows. Do not manually edit `CHANGELOG.md` for routine code changes
  unless the user asks.

## Agent Workflow

- Read relevant files before editing; prefer `rg` and `rg --files` for discovery.
- Check `git status --short` before making edits and do not overwrite unrelated user changes.
- Keep edits scoped to the requested behavior.
- Use `apply_patch` for manual file edits.
- Do not run destructive commands such as `git reset --hard`, `git checkout --`, or broad file deletion unless the user
  explicitly requests them.
- After changes, run the narrowest meaningful Gradle task. If a verification command cannot run, report the exact
  command and why.
- In final responses, summarize changed files and verification performed.

