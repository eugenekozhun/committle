package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageGenerationContext
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import com.kozhun.commitmessagetemplate.service.replacer.Replacer

@Service(Service.Level.PROJECT)
class BreakingChangeReplacer : Replacer {
    override val anchor = "\$BREAKING_CHANGE"

    override suspend fun getReplacement(context: CommitMessageGenerationContext): Replacement {
        return if (context.isBreakingChange) {
            Replacement("!", true)
        } else {
            Replacement("", false)
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): Replacer = project.service<BreakingChangeReplacer>()
    }
}
