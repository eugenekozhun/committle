<h1 align="center">
    <img src="src/main/resources/META-INF/pluginIcon.svg" width="84" height="84" alt="logo"/>
    <br/>
    Committle
</h1>

<p align="center">
    <a href="https://plugins.jetbrains.com/plugin/23100-commit-message-template"><img alt="plugin's version" src="https://img.shields.io/jetbrains/plugin/v/23100-commit-message-template?style=flat-square&logo=jetbrains"/></a>
    <a href="https://plugins.jetbrains.com/plugin/23100-commit-message-template"><img alt="plugin's downloads" src="https://img.shields.io/jetbrains/plugin/d/23100-commit-message-template?style=flat-square"/></a>
    <a href="https://plugins.jetbrains.com/plugin/23100-commit-message-template"><img alt="plugin's rating" src="https://img.shields.io/jetbrains/plugin/r/stars/23100-commit-message-template?style=flat-square"/></a>
    <a href="https://github.com/EugeneKozhun/commit-message-template/actions/workflows/publish-workflow.yml"><img alt="deploy" src="https://img.shields.io/github/actions/workflow/status/EugeneKozhun/commit-message-template/publish-workflow.yml?label=deploy&style=flat-square&logo=github"/></a>
    <a href="https://github.com/EugeneKozhun/commit-message-template/blob/main/LICENSE.md"><img alt="plugin's license" src="https://img.shields.io/github/license/EugeneKozhun/commit-message-template?style=flat-square"/></a>
    <a href="https://github.com/EugeneKozhun/commit-message-template/commits/main"><img alt="last commit" src="https://img.shields.io/github/last-commit/EugeneKozhun/commit-message-template?style=flat-square"/></a>
    <a href="https://boosty.to/eugenekozhun"><img alt="Boosty" src="https://img.shields.io/badge/Support%20me-Boosty-ff5f5f?style=flat-square&logo=boosty&logoColor=white"/></a>
</p>

✨ **Committle** helps you write clear, consistent commit messages in JetBrains IDEs. Keep your history clean and follow your team’s conventions with minimal effort.

_Compatible with [Conventional Commits](https://www.conventionalcommits.org/)_

## 🚀 Key Features

- Default commit message template to speed up routine commits.
- Customizable variables with optional defaults and autocompletion in the template editor:
  - Task id (Jira, Asana, etc.)
  - Scope (feature/module)
  - Type (feat, fix, etc.)
- Whitespace and caret controls for precise formatting and cursor placement.
- Quick action to apply the template via menu or shortcut.

## 📦 Installation (JetBrains Marketplace)

1. Open **Settings/Preferences → Plugins**.
2. Search for **Committle** in the Marketplace tab.
3. Click **Install** and restart the IDE.

## 💡 Quick Start

1. Configure a template in **File → Settings → Version Control → Committle**.
2. Stage your changes and open the commit dialog.
3. Click **Insert Pattern Message** or press **Ctrl+Shift+T** to insert the template.
4. Fill in variables (if any), adjust the text if needed, and commit.

## ⚙️ Configuration

In **File → Settings → Version Control → Committle** you can:

1. Define the commit message template.
2. Configure variables and default values.
3. Tune whitespace and caret placement rules.

### Template example

```
$TYPE($SCOPE): $CARET_POSITION

Closes $TASK_ID
```

Tips:

- Keep the first line under ~72 characters; put details in the body.
- Align with your team’s Convention (e.g., Conventional Commits) for better tooling.

## 🔑 Shortcuts

- Apply template: **Ctrl+Shift+T** (can also be triggered via the commit dialog button).

## 🧩 Compatibility

- Works with Conventional Commits formatting.
- Designed for JetBrains IDEs with the Git tool window and commit dialog.

## 🛠 Development

See [DEVELOPMENT.md](DEVELOPMENT.md) for commit guidelines and build instructions.

## 🐞 Bug Reporting

Found a bug or have an idea? Open an issue: [GitHub Issues](https://github.com/EugeneKozhun/commit-message-template/issues).

## ☕ Support

If this plugin helps you, consider supporting: [Boosty](https://boosty.to/eugenekozhun).

--- 
© 2025 Eugene Kozhun
