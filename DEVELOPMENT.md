# Development Guide

This guide covers local development, testing, and publishing of Committle. For user documentation, see
[README.md](README.md). For AI coding-agent instructions, see [AGENTS.md](AGENTS.md).

## Prerequisites

| Requirement   | Version / Notes                                                       |
|---------------|-----------------------------------------------------------------------|
| JDK           | **21**, matching the Gradle toolchain and CI                          |
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

## Common Gradle Tasks

| Task                                         | Purpose                                            |
|----------------------------------------------|----------------------------------------------------|
| `./gradlew test`                             | Run unit tests                                     |
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

The **Build** workflow runs on pull requests and pushes to `main`. It uses JDK 21 and executes:

```bash
./gradlew clean detekt test assemble --info
```

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
