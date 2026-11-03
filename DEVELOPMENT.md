# Development Guide

This guide covers local development, testing, and publishing of Committle. For user documentation, see
[README.md](README.md). For AI coding-agent instructions, see [AGENTS.md](AGENTS.md).

## Prerequisites

| Requirement   | Version / Notes                                                       |
|---------------|-----------------------------------------------------------------------|
| JDK           | **21** for builds and unit tests; **25** for end-to-end UI tests      |
| Git           | Required to clone the repository and work with plugin functionality   |
| Node.js / npm | **Not required**; the repository has no `package.json` or Husky hooks |

The checked-in wrapper downloads **Gradle 9.8.2**. Java and Kotlin bytecode targets **JVM 17**.

The plugin compiles against **IntelliJ IDEA Community 2023.3.8** and currently supports platform builds **233–262.\***
(2023.3–2026.2).

## Getting Started

```bash
git clone git@github.com:eugenekozhun/committle.git
cd committle
./gradlew clean detekt test assemble
```

Make sure `JAVA_HOME` points to JDK 21 if your default Java runtime is different.

## Project Structure

| Path                                     | Purpose                                                                 |
|------------------------------------------|-------------------------------------------------------------------------|
| `src/main/kotlin/.../action`             | IDE actions and commit-message insertion                                |
| `src/main/kotlin/.../service`            | Formatting, replacements, caret, whitespace, settings, and Git services |
| `src/main/kotlin/.../storage`            | Persistent settings state                                               |
| `src/main/kotlin/.../ui`                 | Settings page, editor, and dialogs                                      |
| `src/main/kotlin/.../language`           | CMT language support, grammar, lexer, highlighting, and completion      |
| `src/main/gen`                           | Generated Grammar-Kit/JFlex sources                                     |
| `src/main/resources/META-INF/plugin.xml` | Plugin registration and dependencies                                    |
| `src/test/kotlin`                        | JUnit 5 and MockK tests                                                 |
| `src/integrationTest/kotlin`             | End-to-end tests running in a real IntelliJ IDEA instance               |
| `src/integrationTest/resources`          | Isolated Git project fixture used by the end-to-end tests               |

## Common Gradle Tasks

| Task                                         | Purpose                                            |
|----------------------------------------------|----------------------------------------------------|
| `./gradlew test`                             | Run unit tests                                     |
| `./gradlew integrationTest`                  | Run end-to-end tests in IntelliJ IDEA              |
| `./gradlew detekt`                           | Run static analysis                                |
| `./gradlew assemble`                         | Compile and assemble the plugin                    |
| `./gradlew buildPlugin`                      | Build the distributable plugin archive             |
| `./gradlew verifyPluginProjectConfiguration` | Validate plugin project configuration              |
| `./gradlew verifyPluginStructure`            | Validate the built plugin structure                |
| `./gradlew runIde`                           | Start a development IDE with the plugin installed  |
| `./gradlew test --rerun-tasks`               | Re-execute tests without reusing test task results |

Before submitting build configuration or plugin distribution changes, run:

```bash
./gradlew clean detekt test assemble buildPlugin verifyPluginProjectConfiguration verifyPluginStructure
```

## End-to-End UI Tests

Run the E2E suite with JDK 25 available to Gradle:

```bash
./gradlew integrationTest
```

The task builds the plugin ZIP, starts the latest supported IntelliJ IDEA with isolated configuration and system
directories, installs that ZIP, and drives the real Settings and Commit UI through the IntelliJ Driver remote API.
The scenarios cover plugin startup, settings persistence, regular and breaking-change commit messages, and the exact
caret position. Git is required because each project test creates its own temporary repository.

By default, Starter downloads the pinned IDE version. To use an existing installation instead, set `E2E_IDE_PATH` to
its installation directory (the `.app` directory on macOS):

```bash
E2E_IDE_PATH='/path/to/IntelliJ IDEA.app' ./gradlew integrationTest
```

On Windows, use the equivalent environment-variable syntax for your shell and `gradlew.bat` instead of `./gradlew`.
The tests contain no OS-specific branches or native mouse/keyboard automation. Windows and macOS require an interactive
graphical session; on Linux, Starter manages Xvfb when no display is available.

JUnit reports are written to `build/test-results/integrationTest/` and `build/reports/tests/integrationTest/`.
IDE logs and failure screenshots are under `out/ide-tests/tests/**/log/`; test performance reports are in the adjacent
`reports/` directories. CI uploads only these diagnostic directories, not the IDE distribution and system caches.

## Working with the CMT Language

The parser, lexer, PSI, and visitor files under `src/main/gen` are generated. Do not edit them directly.

For language changes:

1. Edit `src/main/kotlin/com/kozhun/commitmessagetemplate/language/grammar/CMT.bnf` or
   `src/main/kotlin/com/kozhun/commitmessagetemplate/language/lexer/CMT.flex`.
2. Regenerate the sources:

   ```bash
   ./gradlew generateParser generateLexer
   ```

3. Review both the source grammar and generated diff.
4. Run at least `./gradlew test`.

## Testing Guidelines

- Use **JUnit 5** assertions and **MockK**, following the existing tests.
- Prefer focused unit tests for services and replacement behavior.
- Add IDE integration tests when behavior depends on the IntelliJ runtime, action context, or VCS UI.
- Cover both present and missing values for template replacements.
- Include multiline input when changing whitespace or caret formatting.

## Code Style and Architecture

- Follow the existing Kotlin style and package structure.
- Keep lines within the detekt limit of **160 characters**.
- Keep business logic in services and UI code focused on composition and event wiring.
- Keep editor and Swing updates on EDT; do not use `AnActionEvent` as an asynchronous data snapshot.
- Keep `plugin.xml` synchronized with registered implementation classes and extension points.
- Avoid new dependencies unless they clearly reduce complexity and fit the plugin runtime.

## Commits and Pull Requests

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/). Examples:

```text
feat: add a new template variable
fix: preserve multiline commit messages
build: update the IntelliJ Platform toolchain
docs: improve contributor documentation
```

The repository contains `.commitlintrc.yml`, but no npm project or installed Git hooks. Do not use the old
`npm install` or `npm run changelog` setup instructions.

Pull requests should include:

- a focused description of the change;
- relevant compatibility or migration notes;
- the verification commands and their results;
- documentation updates when behavior or workflows change.

## Continuous Integration

The **Build** workflow runs on pull requests and pushes to `main`. The `build` job uses JDK 21 and executes:

```bash
./gradlew clean detekt test assemble --info
```

A separate `e2e` job uses JDK 25 on Ubuntu and runs `./gradlew integrationTest --info`. Its JUnit reports, IDE logs, and
screenshots are uploaded as the `e2e-test-artifacts` artifact after both successful and failed runs.

For same-repository pull requests and pushes to `main`, the `e2e-report` job publishes the HTML report and a screenshot
gallery to [GitHub Pages](https://eugenekozhun.github.io/committle/). The link is added to the workflow summary and pull
request. Each deployment replaces the previous report, so the URL always shows the latest published run. IDE logs remain
available only in the downloadable artifact. Fork pull requests skip Pages deployment because they do not receive the
required write permissions.

SonarCloud checks also run for pull requests. Build-distribution and plugin-structure verification should be run locally
when relevant, or added to CI as a separate infrastructure improvement.

## Changelog and Release

`CHANGELOG.md` is managed with the Gradle changelog plugin and the **Release** workflow. Follow the current maintainer
process before editing it manually for routine changes.

To publish a new version:

1. Set `pluginVersion` in `gradle.properties`.
2. Prepare the corresponding changelog entry and merge the release changes into `main`.
3. Open **Actions → Release** on GitHub.
4. Select the `main` branch and click **Run workflow**.

The release workflow runs `patchChangelog`, commits the updated changelog to `main`, and then builds, signs, and
publishes the plugin using the configured repository secrets.
