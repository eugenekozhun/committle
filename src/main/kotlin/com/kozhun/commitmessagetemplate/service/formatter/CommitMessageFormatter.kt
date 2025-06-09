package com.kozhun.commitmessagetemplate.service.formatter

import com.intellij.openapi.actionSystem.AnActionEvent

fun interface CommitMessageFormatter {

    suspend fun getFormattedCommitMessage(anActionEvent: AnActionEvent): String
}
