package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_TASK_ID_REGEX
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.git.branch.impl.GitBranchServiceImpl
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.util.toCase
import com.kozhun.commitmessagetemplate.util.toNotBlankRegex

@Service(Service.Level.PROJECT)
class BranchTaskIdReplacer(
    project: Project
) : Replacer {
    private val settingsStorage = SettingsStorage.getInstance(project)
    private val getBranchService = GitBranchServiceImpl.getInstance(project)

    override val anchor = "\$TASK_ID"

    override suspend fun getReplacement(anActionEvent: AnActionEvent): Replacement {
        val taskId = getTaskIdFromCurrentBranch(anActionEvent)
        return Replacement(taskId, taskId.isNotBlank())
    }

    private suspend fun getTaskIdFromCurrentBranch(anActionEvent: AnActionEvent): String {
        return getBranchService.getCurrentBranch(anActionEvent).name
            .let { getTaskIdRegex().find(it)?.value }
            ?.let { changeCase(it) }
            ?: getDefaultTaskIdValue()
    }

    private fun getTaskIdRegex(): Regex {
        return settingsStorage.state.taskIdRegex?.toNotBlankRegex() ?: DEFAULT_TASK_ID_REGEX
    }

    private fun getDefaultTaskIdValue(): String {
        return settingsStorage.state.taskIdDefault.orEmpty()
    }

    private fun changeCase(value: String): String {
        return settingsStorage.state.taskIdPostProcessor
            ?.let { StringCase.labelValueOf(it) }
            ?.let { value.toCase(it) }
            ?: value
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): Replacer = project.service<BranchTaskIdReplacer>()
    }
}
