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
        val branchName = getBranchService.getCurrentBranch(anActionEvent).name

        return extractTaskId(branchName)
            ?.let { applyPostProcessing(it) }
            ?: settingsStorage.state.taskIdDefault.orEmpty()
    }

    private fun extractTaskId(branchName: String): String? {
        val regex = settingsStorage.state.taskIdRegex?.toNotBlankRegex() ?: DEFAULT_TASK_ID_REGEX
        return regex.find(branchName)?.value
    }

    private fun applyPostProcessing(value: String): String {
        val postProcessor = settingsStorage.state.taskIdPostProcessor ?: return value

        return StringCase.labelValueOf(postProcessor)
            ?.let { value.toCase(it) }
            ?: value
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): BranchTaskIdReplacer = project.service()
    }
}

