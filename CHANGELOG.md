# Changelog

## Unreleased

### Added

- Added the `$BREAKING_CHANGE` template variable and a dedicated insertion action for marking individual Conventional
  Commits with `!`, with the default shortcut **Ctrl+Shift+B**.

### Changed

- Updated IntelliJ Platform Gradle Plugin to **2.19.0** and Gradle wrapper to **9.8.2**.
- Raised the minimum supported IDE version to **2023.3** (build **233**) and aligned Kotlin API compatibility to **1.9**.

### Deprecated

### Removed

### Fixed

- Hardened commit-message generation by capturing VCS context before background execution, reporting failures, and using
  configured defaults when branch metadata is unavailable.
- Settings import no longer persists values until Apply, and regular-expression settings are validated on Apply.
- Settings are now written to disk on Apply instead of being lost after an IDE restart.

### Security

## 2.6.0 - 2026-07-21

### Changed

- Update default type regex to follow Conventional Commits format.

## 2.5.0 - 2026-02-08

### Changed

- Migrated to the new IntelliJ Platform Gradle Plugin (v2.10.5).
- Updated Gradle wrapper to **9.2.1**.
- Updated Kotlin to **2.3.0**.

## 2.4.0 - 2026-01-10

### Added

- **Enhanced Template Editor**: Introduced a new dedicated edit field for templates, providing a more intuitive and streamlined configuration experience.

### Changed

- **Performance Optimization**: Deeply optimized the CMT (Committle) language tree, resulting in faster parsing and reduced memory footprint when handling complex templates.
- **Under-the-hood Refactoring**: Improved core architectural components to ensure better maintainability.

### Removed

- **Plugin Slimming**: Stripped out unused dependencies and legacy code to reduce the overall plugin size and improve startup performance.

### Fixed

- **Smart Autocomplete**: Significantly improved the suggestion engine in the template field. Autocomplete is now more context-aware, accurately offering available variables and keywords.
