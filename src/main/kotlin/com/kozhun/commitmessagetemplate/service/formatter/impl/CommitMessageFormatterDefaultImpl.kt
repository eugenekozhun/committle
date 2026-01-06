package com.kozhun.commitmessagetemplate.service.formatter.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageFormatter
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
        val activeReplacers = replacers.filter { pattern.contains(it.anchor) }

        if (activeReplacers.isEmpty()) {
            return whitespaceService.format(pattern)
        }

        val replacements = activeReplacers.associate { it.anchor to it.getReplacement(anActionEvent) }
        val anchorRegex = replacements.keys
            .joinToString("|") { Regex.escape(it) }
            .toRegex()

        val replacedMessage = anchorRegex.replace(pattern) { matchResult ->
            val replacement = replacements[matchResult.value]
            if (replacement?.hasValue == true) replacement.value else ""
        }

        return whitespaceService.format(replacedMessage)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CommitMessageFormatter = project.service<CommitMessageFormatterDefaultImpl>()
    }
}
