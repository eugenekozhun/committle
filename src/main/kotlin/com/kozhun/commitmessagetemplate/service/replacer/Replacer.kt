package com.kozhun.commitmessagetemplate.service.replacer

import com.intellij.openapi.actionSystem.AnActionEvent

fun interface Replacer {

    suspend fun replace(message: String, anActionEvent: AnActionEvent): String
}
