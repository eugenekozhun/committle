package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_TYPE_REGEX
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.git.branch.impl.GitBranchServiceImpl
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.util.toCase
import com.kozhun.commitmessagetemplate.util.toNotBlankRegex

@Service(Service.Level.PROJECT)
class BranchTypeReplacer(
    project: Project
) : Replacer {
    private val settingsStorage = SettingsStorage.getInstance(project)
    private val getBranchService = GitBranchServiceImpl.getInstance(project)

    override val anchor = "\$TYPE"

    override suspend fun replace(message: String, anActionEvent: AnActionEvent): String {
        return changeCase(replaceWithSynonym(getTypeFromCurrentBranch(anActionEvent)))
            .let { message.replace(anchor, it) }
    }

    private suspend fun getTypeFromCurrentBranch(anActionEvent: AnActionEvent): String {
        return getBranchService.getCurrentBranch(anActionEvent).name
            .let { getTypeRegex().find(it)?.value }
            ?: getDefaultTypeValue()
    }

    private fun replaceWithSynonym(type: String): String {
        return settingsStorage.state.typeSynonyms[type] ?: type
    }

    private fun getTypeRegex(): Regex {
        return settingsStorage.state.typeRegex?.toNotBlankRegex() ?: DEFAULT_TYPE_REGEX
    }

    private fun changeCase(value: String): String {
        return settingsStorage.state.typePostprocessor
            ?.let { StringCase.labelValueOf(it) }
            ?.let { value.toCase(it) }
            ?: value
    }

    private fun getDefaultTypeValue(): String {
        return settingsStorage.state.typeDefault.orEmpty()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): Replacer = project.service<BranchTypeReplacer>()
    }
}
