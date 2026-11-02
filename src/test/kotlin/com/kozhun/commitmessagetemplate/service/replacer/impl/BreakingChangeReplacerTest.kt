package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageGenerationContext
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BreakingChangeReplacerTest {
    private val replacer = BreakingChangeReplacer()

    @Test
    fun `should return marker for breaking change generation`() {
        val context = CommitMessageGenerationContext(isBreakingChange = true)

        val result = runBlocking { replacer.getReplacement(context) }

        assertEquals(Replacement("!", true), result)
    }

    @Test
    fun `should return empty value for regular generation`() {
        val context = CommitMessageGenerationContext(isBreakingChange = false)

        val result = runBlocking { replacer.getReplacement(context) }

        assertEquals(Replacement("", false), result)
    }
}
