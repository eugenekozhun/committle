package com.kozhun.commitmessagetemplate.service.formatter.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageFormatter
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTaskIdReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTypeReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.FilePathScopeReplacer
import com.kozhun.commitmessagetemplate.service.whitespace.impl.WhitespaceServiceDefaultImpl
import com.kozhun.commitmessagetemplate.storage.SettingsStorage

@Service(Service.Level.PROJECT)
class CommitMessageFormatterDefaultImpl(
    project: Project
) : CommitMessageFormatter {
    private val projectStorage = SettingsStorage.getInstance(project)
    private val replacers = listOf(
        BranchTypeReplacer.getInstance(project),
        BranchTaskIdReplacer.getInstance(project),
        FilePathScopeReplacer.getInstance(project)
    )

    private val whitespaceService = WhitespaceServiceDefaultImpl.getInstance(project)

    override suspend fun getFormattedCommitMessage(anActionEvent: AnActionEvent): String {
        val pattern = projectStorage.state.pattern.orEmpty()
        return replacers
            .fold(pattern) { result, replacer -> replaceIfNeeded(replacer, result, anActionEvent) }
            .let { whitespaceService.format(it) }
    }

    private suspend fun replaceIfNeeded(
        replacer: Replacer,
        message: String,
        anActionEvent: AnActionEvent
    ): String = when {
        message.contains(replacer.anchor) -> replacer.replace(message, anActionEvent)
        else -> message
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CommitMessageFormatter = project.service<CommitMessageFormatterDefaultImpl>()
    }
}
