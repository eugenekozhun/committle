package com.kozhun.commitmessagetemplate.service.formatter.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageFormatter
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTaskIdReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTypeReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.FilePathScopeReplacer
import com.kozhun.commitmessagetemplate.service.whitespace.impl.WhitespaceServiceDefaultImpl
import com.kozhun.commitmessagetemplate.storage.SettingsStorage

@Service(Service.Level.PROJECT)
class CommitMessageFormatterDefaultImpl(
    project: Project
) : CommitMessageFormatter {
    private val projectStorage by lazy { SettingsStorage.getInstance(project) }
    private val whitespaceService by lazy { WhitespaceServiceDefaultImpl.getInstance(project) }

    private val replacers = listOf(
        BranchTypeReplacer.getInstance(project),
        BranchTaskIdReplacer.getInstance(project),
        FilePathScopeReplacer.getInstance(project)
    )

    override suspend fun getFormattedCommitMessage(anActionEvent: AnActionEvent): String {
        val pattern = projectStorage.state.pattern.orEmpty()
        val replacements = collectReplacements(pattern, anActionEvent)
        val replacedMessage = applyReplacements(pattern, replacements)

        return whitespaceService.format(replacedMessage)
    }

    private suspend fun collectReplacements(
        pattern: String,
        anActionEvent: AnActionEvent,
    ): Map<String, Replacement> {
        return replacers
            .filter { pattern.contains(it.anchor) }
            .associate { replacer -> replacer.anchor to replacer.getReplacement(anActionEvent) }
    }

    private fun applyReplacements(
        pattern: String,
        replacements: Map<String, Replacement>,
    ): String {
        return replacements.entries.fold(pattern) { acc, (anchor, replacement) ->
            val value = if (replacement.hasValue) replacement.value else ""
            acc.replace(anchor, value)
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CommitMessageFormatter = project.service<CommitMessageFormatterDefaultImpl>()
    }
}
