package com.kozhun.commitmessagetemplate.service.replacer

import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * A functional interface for replacing substrings in a given message.
 */
fun interface Replacer {

    /**
     * Replaces certain characters in a given message with a specified replacement.
     *
     * @param message The message to be processed.
     * @return The modified message with replaced characters.
     */
    suspend fun replace(message: String, anActionEvent: AnActionEvent): String
}
