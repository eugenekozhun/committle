package com.kozhun.commitmessagetemplate.service.replacer

import com.intellij.openapi.actionSystem.AnActionEvent

interface Replacer {
    val anchor: String

    suspend fun replace(message: String, anActionEvent: AnActionEvent): String
}
