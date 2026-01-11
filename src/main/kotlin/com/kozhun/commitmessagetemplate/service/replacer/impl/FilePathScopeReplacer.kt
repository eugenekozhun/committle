package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.ui.Refreshable
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_SCOPE_SEPARATOR
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
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

    override val anchor = $$"$SCOPE"

    override suspend fun getReplacement(anActionEvent: AnActionEvent): Replacement {
        val scope = extractScope(anActionEvent)

        return Replacement(scope, scope.isNotBlank())
    }

    private fun extractScope(anActionEvent: AnActionEvent): String {
        return getAffectedPaths(anActionEvent)
            .map { getPathScope(it) }
            .filter { !it.isNullOrEmpty() }
            .distinct()
            .joinToString(getSeparator())
            .takeIf { it.isNotEmpty() }
            ?.let { changeCase(it) }
            .orDefaultScope()
    }

    private fun getAffectedPaths(anActionEvent: AnActionEvent): Sequence<String> {
        return getSelectedChangePaths(anActionEvent)
            .ifEmpty {
                changeListManager
                    .affectedPaths
                    .asSequence()
                    .mapNotNull { it.path }
            }
    }

    private fun getSelectedChangePaths(anActionEvent: AnActionEvent): Sequence<String> {
        val panel = anActionEvent.getData(Refreshable.PANEL_KEY) as? CheckinProjectPanel
        if (panel != null) {
            return panel.selectedChanges.asSequence().mapNotNull(::getChangePath)
        }

        return anActionEvent.getData(VcsDataKeys.CHANGES)
            ?.asSequence()
            ?.mapNotNull(::getChangePath)
            ?: emptySequence()
    }

    private fun getChangePath(change: Change): String? {
        return change.afterRevision?.file?.path
            ?: change.virtualFile?.path
            ?: change.beforeRevision?.file?.path
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
        @JvmStatic
        fun getInstance(project: Project): Replacer = project.service<FilePathScopeReplacer>()
    }
}
