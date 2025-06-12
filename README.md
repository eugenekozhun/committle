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
    <a href="https://ko-fi.com/eugenekozhun"><img alt="Buy Me a Coffee" src="https://img.shields.io/badge/Support%20me-Ko--fi-ff5f5f?style=flat-square&logo=ko-fi&logoColor=white"/></a>
</p>

✨ **Committle** (ex. Commit Message Template)
is a JetBrains IDE plugin that simplifies writing consistent and meaningful commit messages.
Forget messy commits—stick to your project’s conventions with ease!

_Compatible with [Conventional Commits](https://www.conventionalcommits.org/)_

## 🚀 Key Features

- Default commit message template: Save time by using a predefined structure.
- Customizable variables: Dynamically set values like:
    - Task id (Jira, Asana, etc.)
    - Scope (e.g., feature area)
    - Type (feat, fix, etc.)
    - Default values for variables
    - Autocompletion for variables in the template editor
- Whitespace and caret formatting: Fine-tune alignment and position.
- Shortcut for applying the template: Fast and efficient workflow.

## 📦 Installation

### From JetBrains Marketplace

1. Open **Settings/Preferences → Plugins** in your JetBrains IDE.
2. Search for **Committle** in the Marketplace tab.
3. Click **Install** and restart the IDE.

## 💡 How to Use

Follow these steps to insert a commit message from your template:

1. Set up your template in **Settings → Version Control → Commit Template**.
2. Stage files and open the commit dialog.
3. Click **Insert Pattern Message** or press **Ctrl+Shift+T** to apply the template.
4. Edit the generated text if needed and complete the commit.

## ⚙️ Customization

Customize the plugin to your needs:

1. Go to `File > Settings > Version Control > Committle`.
2. Enter your desired commit message template.
3. Set whitespace rules.
4. (Optional) Configure your own rules for the variables.

### Basic Template Example

```
$TYPE ($SCOPE): Commit message 

Description of changes

Closes $TASK_ID
```

## 🛠 Development

See [DEVELOPMENT.md](DEVELOPMENT.md) for commit guidelines and build instructions.

## 🐞 Bug Reporting

Found a bug? Have an idea for improvement? Feel free to create an issue in the
repository: [GitHub Issues](https://github.com/EugeneKozhun/commit-message-template/issues).

## ☕ Support My Work

If you find this project helpful, consider [buying me a coffee](https://ko-fi.com/eugenekozhun) —
your support helps me keep going!

--- 
© 2025 Eugene Kozhun
