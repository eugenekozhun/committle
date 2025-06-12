package com.kozhun.commitmessagetemplate.language.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
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
                    listOf(
                        LookupElementBuilder.create("\$TASK_ID").withTypeText("Task identifier extracted from branch name"),
                        LookupElementBuilder.create("\$TYPE").withTypeText("Type of commit extracted from branch name (feature, bugfix, etc.)"),
                        LookupElementBuilder.create("\$SCOPE").withTypeText("Scope of changes extracted from affected file paths"),
                        LookupElementBuilder.create("\$CARET_POSITION").withTypeText("Position where cursor will be placed after template insertion")
                    ).forEach {
                        result.addElement(it)
                    }
                }
            }
        )
    }
}
