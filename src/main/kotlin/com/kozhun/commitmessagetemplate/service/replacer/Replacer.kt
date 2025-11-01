package com.kozhun.commitmessagetemplate.service.replacer

import com.intellij.openapi.actionSystem.AnActionEvent

interface Replacer {
    val anchor: String

    suspend fun getReplacement(anActionEvent: AnActionEvent): Replacement
}

data class Replacement(
    val value: String,
    val hasValue: Boolean,
)
