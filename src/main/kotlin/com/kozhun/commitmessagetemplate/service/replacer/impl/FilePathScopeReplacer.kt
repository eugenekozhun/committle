package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_SCOPE_SEPARATOR
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.util.toCase
import com.kozhun.commitmessagetemplate.util.toNotBlankRegex

@Service(Service.Level.PROJECT)
class FilePathScopeReplacer(
    project: Project
) : Replacer {
    private val changeListManager = ChangeListManager.getInstance(project)
    private val settingsStorage = SettingsStorage.getInstance(project)

    override suspend fun replace(message: String, anActionEvent: AnActionEvent): String {
        return message.replace(ANCHOR, extractScope())
    }

    private fun extractScope(): String {
        return changeListManager
            .affectedPaths
            .asSequence()
            .mapNotNull { it.path }
            .map { getPathScope(it) }
            .filter { !it.isNullOrEmpty() }
            .distinct()
            .joinToString(getSeparator())
            .takeIf { it.isNotEmpty() }
            ?.let { changeCase(it) }
            .orDefaultScope()
    }

    private fun getPathScope(it: String): String? {
        return getRegex()?.find(it)?.value
    }

    private fun changeCase(value: String): String {
        return settingsStorage.state.scopePostprocessor
            ?.let { StringCase.labelValueOf(it) }
            ?.let { value.toCase(it) }
            ?: value
    }

    private fun getSeparator(): String {
        return settingsStorage.state.scopeSeparator
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_SCOPE_SEPARATOR
    }

    private fun getRegex(): Regex? {
        return settingsStorage.state.scopeRegex?.toNotBlankRegex()
    }

    private fun String?.orDefaultScope(): String {
        if (!this.isNullOrEmpty()) {
            return this
        }
        return settingsStorage.state.scopeDefault.orEmpty()
    }

    companion object {
        private const val ANCHOR = "\$SCOPE"

        @JvmStatic
        fun getInstance(project: Project): Replacer = project.service<FilePathScopeReplacer>()
    }
}
