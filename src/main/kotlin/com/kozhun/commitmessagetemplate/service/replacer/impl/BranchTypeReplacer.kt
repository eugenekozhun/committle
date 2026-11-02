package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_TYPE_REGEX
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageGenerationContext
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.util.toCase
import com.kozhun.commitmessagetemplate.util.toNotBlankRegex

@Service(Service.Level.PROJECT)
class BranchTypeReplacer(
    project: Project
) : Replacer {
    private val settingsStorage = SettingsStorage.getInstance(project)

    override val anchor = "\$TYPE"

    override suspend fun getReplacement(context: CommitMessageGenerationContext): Replacement {
        val type = changeCase(replaceWithSynonym(getTypeFromCurrentBranch(context)))

        return Replacement(type, type.isNotBlank())
    }

    private fun getTypeFromCurrentBranch(context: CommitMessageGenerationContext): String {
        return context.currentBranchName
            ?.let { getTypeRegex().find(it)?.value }
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
