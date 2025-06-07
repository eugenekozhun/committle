package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_TASK_ID_REGEX
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.util.branches
import com.kozhun.commitmessagetemplate.util.storage
import com.kozhun.commitmessagetemplate.util.toCase
import com.kozhun.commitmessagetemplate.util.toNotBlankRegex

/**
 * Replaces a predefined anchor in a given message with the task ID of the current Git branch.
 *
 * @param project The IntelliJ IDEA project.
 */
@Service(Service.Level.PROJECT)
class BranchTaskIdReplacer(
    private val project: Project
) : Replacer {

    /**
     * Replaces the occurrence of TASK_ID_ANCHOR in the given message with the task ID from the current branch.
     *
     * @param message the original message that may contain TASK_ID_ANCHOR.
     * @return the message with TASK_ID_ANCHOR replaced by the task ID from the current branch.
     */
    override suspend fun replace(message: String, anActionEvent: AnActionEvent): String {
        return message.replace(ANCHOR, getTaskIdFromCurrentBranch(anActionEvent))
    }

    private suspend fun getTaskIdFromCurrentBranch(anActionEvent: AnActionEvent): String {
        return project.branches().getCurrentBranch(anActionEvent).name
            .let { getTaskIdRegex().find(it)?.value }
            ?.let { changeCase(it) }
            ?: getDefaultTaskIdValue()
    }

    private fun getTaskIdRegex(): Regex {
        return project.storage().state.taskIdRegex?.toNotBlankRegex() ?: DEFAULT_TASK_ID_REGEX
    }

    private fun getDefaultTaskIdValue(): String {
        return project.storage().state.taskIdDefault.orEmpty()
    }

    private fun changeCase(value: String): String {
        return project.storage().state.taskIdPostProcessor
            ?.let { StringCase.labelValueOf(it) }
            ?.let { value.toCase(it) }
            ?: value
    }

    companion object {
        const val ANCHOR = "\$TASK_ID"

        @JvmStatic
        fun getInstance(project: Project): Replacer = project.service<BranchTaskIdReplacer>()
    }
}
