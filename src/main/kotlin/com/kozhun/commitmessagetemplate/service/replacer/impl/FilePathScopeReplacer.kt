package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_SCOPE_SEPARATOR
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageGenerationContext
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.util.toCase
import com.kozhun.commitmessagetemplate.util.toNotBlankRegex

@Service(Service.Level.PROJECT)
class FilePathScopeReplacer(
    project: Project
) : Replacer {
    private val settingsStorage = SettingsStorage.getInstance(project)

    override val anchor = "\$SCOPE"

    override suspend fun getReplacement(context: CommitMessageGenerationContext): Replacement {
        val scope = extractScope(context)

        return Replacement(scope, scope.isNotBlank())
    }

    private fun extractScope(context: CommitMessageGenerationContext): String {
        return getAffectedPaths(context)
            .map { getPathScope(it) }
            .filter { !it.isNullOrEmpty() }
            .distinct()
            .joinToString(getSeparator())
            .takeIf { it.isNotEmpty() }
            ?.let { changeCase(it) }
            .orDefaultScope()
    }

    private fun getAffectedPaths(context: CommitMessageGenerationContext): Sequence<String> {
        return context.changes
            .asSequence()
            .mapNotNull(::getChangePath)
            .ifEmpty { context.affectedPaths.asSequence() }
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
