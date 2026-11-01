<div align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" width="96" height="96" alt="Committle logo"/>

  <h1>Committle</h1>

  <p><strong>Consistent commit messages, generated your way.</strong></p>
  <p>A JetBrains IDE plugin for reusable commit templates, branch-based variables, and precise formatting.</p>

  <p>
    <a href="https://plugins.jetbrains.com/plugin/23100-committle">
      <img alt="Marketplace version" src="https://img.shields.io/jetbrains/plugin/v/23100-committle?style=flat-square&logo=jetbrains&label=Marketplace"/>
    </a>
    <a href="https://plugins.jetbrains.com/plugin/23100-committle">
      <img alt="Downloads" src="https://img.shields.io/jetbrains/plugin/d/23100-committle?style=flat-square&logo=jetbrains"/>
    </a>
    <a href="https://github.com/eugenekozhun/committle/actions/workflows/build.yml">
      <img alt="Build status" src="https://img.shields.io/github/actions/workflow/status/eugenekozhun/committle/build.yml?branch=main&style=flat-square&logo=github&label=build"/>
    </a>
    <a href="https://github.com/eugenekozhun/committle/blob/main/LICENSE.md">
      <img alt="License" src="https://img.shields.io/github/license/eugenekozhun/committle?style=flat-square"/>
    </a>
  </p>

  <p>
    <a href="#-features">Features</a> ·
    <a href="#-installation">Installation</a> ·
    <a href="#-quick-start">Quick Start</a> ·
    <a href="#-configuration">Configuration</a> ·
    <a href="#-development">Development</a>
  </p>
</div>

---

## ✨ Features

| Feature | Description |
| --- | --- |
| **Reusable templates** | Generate commit messages from a project-specific template instead of writing the same structure repeatedly. |
| **Branch-based variables** | Extract a task ID and commit type from the current Git branch using configurable regular expressions. |
| **File-based scopes** | Build scopes from paths of the changes selected for commit. |
| **Defaults and postprocessing** | Configure fallback values, separators, type synonyms, and case transformations. |
| **Whitespace and caret control** | Clean up formatting and place the cursor exactly where the next part of the message should be entered. |
| **Template completion** | Use completion and syntax highlighting in the dedicated template editor. |
| **Settings import/export** | Back up or share project settings as JSON. |

## 📦 Installation

Install Committle directly from [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/23100-committle), or open
your IDE and:

1. Go to **Settings/Preferences → Plugins → Marketplace**.
2. Search for **Committle**.
3. Click **Install** and restart the IDE if prompted.

## 🚀 Quick Start

1. Open **Settings/Preferences → Version Control → Committle**.
2. Define a commit message template.
3. Open the commit dialog and click **Insert Pattern Message**, or use the default shortcut **Ctrl+Shift+T**.
4. Complete the generated message and commit your changes.

### Template

```text
$TYPE($SCOPE): $CARET_POSITION

Closes $TASK_ID
```

### Example result

```text
feat(ui): Improve login flow

Closes CMT-123
```

## 🧩 Template Variables

| Variable | Value source | Example |
| --- | --- | --- |
| `$TASK_ID` | A match extracted from the current branch name | `CMT-123` |
| `$TYPE` | A commit type extracted from the current branch name | `feat`, `fix`, `docs` |
| `$SCOPE` | Matches extracted from paths of changes selected for commit | `ui`, `api`, `core` |
| `$CARET_POSITION` | The desired cursor position after the message is inserted | Not included in the final text |

Each extracted value can have a configurable regular expression, default fallback, and case postprocessing. Type
synonyms can normalize custom branch prefixes into your team's conventional commit types.

## ⚙️ Configuration

### Task ID and type

Configure extraction regexes for branch names, fallback values, and case transformations. For the commit type, you can
also maintain a table of synonyms.

### Scope

Configure a path-matching regex, a fallback scope, case transformation, and a separator for multiple distinct scopes.
Committle uses the changes selected in the commit dialog when they are available.

### Whitespace and caret

Choose whether to trim leading or trailing whitespace and whether to consolidate repeated whitespace. Insert
`$CARET_POSITION` where you want to continue typing after applying the template.

### Import and export

Use the **Import Settings** and **Export Settings** buttons on the Committle settings page to read or save a JSON
configuration file.

## ✅ Compatibility

| Requirement | Supported version |
| --- | --- |
| Minimum IntelliJ Platform | **2023.3**, build `233` |
| Maximum declared IntelliJ Platform | **2026.2**, build `262.*` |
| Required plugin dependency | **Git4Idea**, bundled with supported JetBrains IDEs that include Git integration |

Committle is designed for IntelliJ-based JetBrains IDEs with Git support.

## 🛠 Development

The project uses **JDK 21**, **Gradle 9.8.2**, **Kotlin 2.3.0**, and **JVM 17** bytecode.

- [Development Guide](DEVELOPMENT.md) — setup, Gradle tasks, testing, generated sources, CI, and release process.
- [AGENTS.md](AGENTS.md) — project instructions for AI coding agents.
- [Changelog](CHANGELOG.md) — release history and notable changes.

Bug reports and feature requests are welcome in [GitHub Issues](https://github.com/eugenekozhun/committle/issues).

## 💛 Support

If Committle makes your workflow easier, consider starring the repository or supporting development through
[Boosty](https://boosty.to/eugenekozhun).

## 📄 License

Committle is distributed under the terms of the [LICENSE.md](LICENSE.md) file.

---

<div align="center">
  <sub>© 2026 Eugene Kozhun · Built for clean and consistent commit histories.</sub>
</div>
