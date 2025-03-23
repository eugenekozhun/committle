package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_TYPE_REGEX
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.util.branches
import com.kozhun.commitmessagetemplate.util.storage
import com.kozhun.commitmessagetemplate.util.toCase
import com.kozhun.commitmessagetemplate.util.toNotBlankRegex

/**
 * A class that replaces a specific substring in a given message based on the current branch type and settings.
 *
 * @property project The IntelliJ IDEA project.
 */
@Service(Service.Level.PROJECT)
class BranchTypeReplacer(
    private val project: Project
) : Replacer {
    override fun replace(message: String, anActionEvent: AnActionEvent): String {
        return changeCase(replaceWithSynonym(getTypeFromCurrentBranch(anActionEvent)))
            .let { message.replace(ANCHOR, it) }
    }

    private fun getTypeFromCurrentBranch(anActionEvent: AnActionEvent): String {
        return project.branches().getCurrentBranch(anActionEvent).name
            .let { getTypeRegex().find(it)?.value }
            ?: getDefaultTypeValue()
    }

    private fun replaceWithSynonym(type: String): String {
        return project.storage().state.typeSynonyms[type] ?: type
    }

    private fun getTypeRegex(): Regex {
        return project.storage().state.typeRegex?.toNotBlankRegex() ?: DEFAULT_TYPE_REGEX
    }

    private fun changeCase(value: String): String {
        return project.storage().state.typePostprocessor
            ?.let { StringCase.labelValueOf(it) }
            ?.let { value.toCase(it) }
            ?: value
    }

    private fun getDefaultTypeValue(): String {
        return project.storage().state.typeDefault.orEmpty()
    }

    companion object {
        const val ANCHOR = "\$TYPE"

        @JvmStatic
        fun getInstance(project: Project): Replacer = project.service<BranchTypeReplacer>()
    }
}
