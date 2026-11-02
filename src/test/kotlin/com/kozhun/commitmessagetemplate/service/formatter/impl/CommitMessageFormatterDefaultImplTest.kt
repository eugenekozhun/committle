package com.kozhun.commitmessagetemplate.service.formatter.impl

import com.kozhun.commitmessagetemplate.service.caret.impl.CaretServiceDefaultImpl
import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BaseReplacerTest
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTaskIdReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTypeReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BreakingChangeReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.FilePathScopeReplacer
import com.kozhun.commitmessagetemplate.service.whitespace.WhitespaceService
import com.kozhun.commitmessagetemplate.service.whitespace.impl.WhitespaceServiceDefaultImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommitMessageFormatterDefaultImplTest : BaseReplacerTest() {

    @Test
    fun `should format commit message using replacements metadata`() {
        val formatter = createFormatter("\$TYPE \$TASK_ID \$SCOPE")

        val result = runBlocking { formatter.getFormattedCommitMessage(generationContext) }

        assertEquals("feature  scope", result)
    }

    @Test
    fun `should remove breaking change variable during regular generation`() {
        val formatter = createFormatter("\$TYPE(\$SCOPE)\$BREAKING_CHANGE: \$CARET_POSITION")

        val result = runBlocking { formatter.getFormattedCommitMessage(generationContext) }
        val (message, caretOffset) = CaretServiceDefaultImpl().getCaretOffsetByAnchor(result)

        assertEquals("feature(scope): ", message)
        assertEquals(message.length, caretOffset)
    }

    @Test
    fun `should insert breaking change marker during breaking change generation`() {
        generationContext = generationContext.copy(isBreakingChange = true)
        val formatter = createFormatter("\$TYPE(\$SCOPE)\$BREAKING_CHANGE: \$CARET_POSITION")

        val result = runBlocking { formatter.getFormattedCommitMessage(generationContext) }
        val (message, caretOffset) = CaretServiceDefaultImpl().getCaretOffsetByAnchor(result)

        assertEquals("feature(scope)!: ", message)
        assertEquals(message.length, caretOffset)
    }

    @Test
    fun `should keep template without breaking change variable unchanged`() {
        val formatter = createFormatter("Custom commit template")

        val result = runBlocking { formatter.getFormattedCommitMessage(generationContext) }

        assertEquals("Custom commit template", result)
    }

    private fun createFormatter(pattern: String): CommitMessageFormatterDefaultImpl {
        mockSettingState(pattern = pattern)

        val branchTypeReplacer = mockk<Replacer>()
        val branchTaskIdReplacer = mockk<BranchTaskIdReplacer>()
        val filePathScopeReplacer = mockk<Replacer>()
        val whitespaceService = mockk<WhitespaceService>()

        mockkStatic(BranchTypeReplacer::class)
        mockkStatic(BranchTaskIdReplacer::class)
        mockkStatic(FilePathScopeReplacer::class)
        mockkStatic(BreakingChangeReplacer::class)
        mockkStatic(WhitespaceServiceDefaultImpl::class)

        every { BranchTypeReplacer.getInstance(projectMock) } returns branchTypeReplacer
        every { BranchTaskIdReplacer.getInstance(projectMock) } returns branchTaskIdReplacer
        every { FilePathScopeReplacer.getInstance(projectMock) } returns filePathScopeReplacer
        every { BreakingChangeReplacer.getInstance(projectMock) } returns BreakingChangeReplacer()
        every { WhitespaceServiceDefaultImpl.getInstance(projectMock) } returns whitespaceService

        every { branchTypeReplacer.anchor } returns "\$TYPE"
        every { branchTaskIdReplacer.anchor } returns "\$TASK_ID"
        every { filePathScopeReplacer.anchor } returns "\$SCOPE"

        coEvery { branchTypeReplacer.getReplacement(any()) } returns Replacement("feature", true)
        coEvery { branchTaskIdReplacer.getReplacement(any()) } returns Replacement("", false)
        coEvery { filePathScopeReplacer.getReplacement(any()) } returns Replacement("scope", true)
        every { whitespaceService.format(any()) } answers { firstArg() }

        return CommitMessageFormatterDefaultImpl(projectMock)
    }
}
