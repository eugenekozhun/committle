package com.kozhun.commitmessagetemplate.storage

import com.intellij.openapi.components.BaseState
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_SCOPE_SEPARATOR
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.settings.ExportableSettings

/**
 * Represents the persistable state of the commit message template settings.
 * Extends [BaseState] to provide automatic state persistence functionality.
 */
class SettingsState : BaseState() {
    /**
     * The template pattern used for generating commit messages.
     */
    var pattern by string()

    /**
     * Controls whether leading whitespace should be removed from the generated messоage.
     */
    var trimWhitespacesStart by property(false)

    /**
     * Controls whether trailing whitespace should be removed from the generated message.
     */
    var trimWhitespacesEnd by property(false)

    /**
     * Controls whether duplicate whitespace characters should be consolidated.
     */
    var unnecessaryWhitespaces by property(false)

    /**
     * Regular expression pattern used to extract task ID from the current branch name.
     * When null or empty, the default pattern will be used.
     */
    var taskIdRegex by string()

    /**
     * Default value to use for task ID when no match is found in the branch name.
     */
    var taskIdDefault by string()

    /**
     * String case transformation to apply to the extracted task ID.
     * Default value is [StringCase.NONE].
     */
    var taskIdPostProcessor by string(StringCase.NONE.label)

    /**
     * Regular expression pattern used to extract type from the current branch name.
     * When null or empty, the default pattern will be used.
     */
    var typeRegex by string()

    /**
     * Default value to use for type when no match is found in the branch name.
     */
    var typeDefault by string()

    /**
     * Map of type synonyms for normalizing branch types.
     * Keys are the source values, values are the target replacements.
     */
    var typeSynonyms by map<String, String>()

    /**
     * String case transformation to apply to the extracted type.
     * Default value is [StringCase.NONE].
     */
    var typePostprocessor by string(StringCase.NONE.label)

    /**
     * Regular expression pattern used to extract scope from the file path.
     * When null or empty, the default pattern will be used.
     */
    var scopeRegex by string()

    /**
     * Default value to use for scope when no match is found in the file path.
     */
    var scopeDefault by string()

    /**
     * Character or string used to separate multiple scope values.
     * When null or empty, [DEFAULT_SCOPE_SEPARATOR] will be used.
     */
    var scopeSeparator by string()

    /**
     * String case transformation to apply to the extracted scope.
     * Default value is [StringCase.NONE].
     */
    var scopePostprocessor by string(StringCase.NONE.label)

    /**
     * Checks if task-related fields are in their default state.
     *
     * @return `true` if all task-related fields have default values, `false` otherwise.
     */
    fun isDefaultTaskFields(): Boolean {
        return taskIdRegex.isNullOrEmpty() &&
                taskIdDefault.isNullOrEmpty() &&
                taskIdPostProcessor == StringCase.NONE.label
    }

    /**
     * Checks if type-related fields are in their default state.
     *
     * @return `true` if all type-related fields have default values, `false` otherwise.
     */
    fun isDefaultTypeFields(): Boolean {
        return typeRegex.isNullOrEmpty() &&
                typeDefault.isNullOrEmpty() &&
                typePostprocessor == StringCase.NONE.label
    }

    /**
     * Checks if scope-related fields are in their default state.
     *
     * @return `true` if all scope-related fields have default values, `false` otherwise.
     */
    fun isDefaultScopeFields(): Boolean {
        return scopeRegex.isNullOrEmpty() &&
                scopeDefault.isNullOrEmpty() &&
                (scopeSeparator.isNullOrEmpty() || scopeSeparator == DEFAULT_SCOPE_SEPARATOR) &&
                (scopePostprocessor == StringCase.NONE.label)
    }
}

/**
 * Converts the current [SettingsState] to [ExportableSettings].
 * This is used when exporting settings to a portable format.
 *
 * @return An [ExportableSettings] instance containing the current settings.
 */
fun SettingsState.toExportableSettings(): ExportableSettings {
    return ExportableSettings(
        pattern = pattern.orEmpty(),
        trimWhitespacesStart = trimWhitespacesStart,
        trimWhitespacesEnd = trimWhitespacesEnd,
        unnecessaryWhitespaces = unnecessaryWhitespaces,
        taskIdRegex = taskIdRegex,
        taskIdDefault = taskIdDefault,
        taskIdPostProcessor = taskIdPostProcessor,
        typeRegex = typeRegex,
        typeDefault = typeDefault,
        typeSynonyms = typeSynonyms,
        typePostprocessor = typePostprocessor,
        scopeRegex = scopeRegex,
        scopeDefault = scopeDefault,
        scopeSeparator = scopeSeparator,
        scopePostprocessor = scopePostprocessor
    )
}
