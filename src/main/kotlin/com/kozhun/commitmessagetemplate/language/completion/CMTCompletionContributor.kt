package com.kozhun.commitmessagetemplate.language.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.kozhun.commitmessagetemplate.language.CMTLanguage

class CMTCompletionContributor : CompletionContributor(), DumbAware {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(CMTLanguage.INSTANCE),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val lookupElements = listOf(
                        createVariableLookup($$"$TASK_ID", "Task identifier from branch name"),
                        createVariableLookup($$"$TYPE", "Commit type from branch name"),
                        createVariableLookup($$"$SCOPE", "Scope from file paths"),
                        createVariableLookup($$"$CARET_POSITION", "Cursor placement after insertion")
                    )

                    result.addAllElements(lookupElements)
                }
            }
        )
    }

    private fun createVariableLookup(name: String, description: String) =
        LookupElementBuilder.create(name)
            .withIcon(AllIcons.Nodes.Variable)
            .withBoldness(true)
            .withTypeText(description, true)
}
