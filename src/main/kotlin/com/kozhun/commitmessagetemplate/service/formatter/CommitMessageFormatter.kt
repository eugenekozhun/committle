package com.kozhun.commitmessagetemplate.service.formatter

import com.intellij.openapi.actionSystem.AnActionEvent

fun interface CommitMessageFormatter {

    fun getFormattedCommitMessage(anActionEvent: AnActionEvent): String
}
