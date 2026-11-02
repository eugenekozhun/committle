package com.kozhun.commitmessagetemplate.service.replacer

import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageGenerationContext

interface Replacer {
    val anchor: String

    suspend fun getReplacement(context: CommitMessageGenerationContext): Replacement
}

data class Replacement(
    val value: String,
    val hasValue: Boolean,
)
