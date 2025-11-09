package com.kozhun.commitmessagetemplate.service.formatter.impl

import com.kozhun.commitmessagetemplate.service.replacer.Replacement
import com.kozhun.commitmessagetemplate.service.replacer.Replacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BaseReplacerTest
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTaskIdReplacer
import com.kozhun.commitmessagetemplate.service.replacer.impl.BranchTypeReplacer
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
        mockSettingState(pattern = "\$TYPE \$TASK_ID \$SCOPE")

        val branchTypeReplacer = mockk<Replacer>()
        val branchTaskIdReplacer = mockk<Replacer>()
        val filePathScopeReplacer = mockk<Replacer>()
        val whitespaceService = mockk<WhitespaceService>()

        mockkStatic(BranchTypeReplacer::class)
        mockkStatic(BranchTaskIdReplacer::class)
        mockkStatic(FilePathScopeReplacer::class)
        mockkStatic(WhitespaceServiceDefaultImpl::class)

        every { BranchTypeReplacer.getInstance(projectMock) } returns branchTypeReplacer
        every { BranchTaskIdReplacer.getInstance(projectMock) } returns branchTaskIdReplacer
        every { FilePathScopeReplacer.getInstance(projectMock) } returns filePathScopeReplacer
        every { WhitespaceServiceDefaultImpl.getInstance(projectMock) } returns whitespaceService

        every { branchTypeReplacer.anchor } returns "\$TYPE"
        every { branchTaskIdReplacer.anchor } returns "\$TASK_ID"
        every { filePathScopeReplacer.anchor } returns "\$SCOPE"

        coEvery { branchTypeReplacer.getReplacement(anActionEventMock) } returns Replacement("feature", true)
        coEvery { branchTaskIdReplacer.getReplacement(anActionEventMock) } returns Replacement("", false)
        coEvery { filePathScopeReplacer.getReplacement(anActionEventMock) } returns Replacement("scope", true)

        var templateBeforeWhitespace: String? = null
        every { whitespaceService.format(any()) } answers {
            templateBeforeWhitespace = firstArg()
            firstArg()
        }

        val formatter = CommitMessageFormatterDefaultImpl(projectMock)

        val result = runBlocking { formatter.getFormattedCommitMessage(anActionEventMock) }

        assertEquals("feature  scope", templateBeforeWhitespace)
        assertEquals("feature  scope", result)
    }
}
