package com.kozhun.commitmessagetemplate.service.formatter

fun interface CommitMessageFormatter {

    suspend fun getFormattedCommitMessage(context: CommitMessageGenerationContext): String
}
