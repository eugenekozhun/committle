package com.kozhun.commitmessagetemplate.action

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.kozhun.commitmessagetemplate.service.caret.CaretService
import com.kozhun.commitmessagetemplate.service.caret.impl.CaretServiceDefaultImpl
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageFormatter
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageGenerationContext
import com.kozhun.commitmessagetemplate.service.formatter.impl.CommitMessageFormatterDefaultImpl
import com.kozhun.commitmessagetemplate.service.helper.CoroutineScopeService
import com.kozhun.commitmessagetemplate.ui.util.showCommittleNotification
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.regex.PatternSyntaxException

class GenerateCommitMessageActionTest {
    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `generation failure shows error notification without updating editor`() {
        val project = mockk<Project>()
        val event = mockk<AnActionEvent>()
        val commitMessage = mockk<CommitMessage>()
        val context = CommitMessageGenerationContext()
        val formatter = mockk<CommitMessageFormatter>()
        val caretService = mockk<CaretService>()
        val scopeService = mockk<CoroutineScopeService> {
            every { coroutineScope } returns CoroutineScope(Dispatchers.Unconfined)
        }

        every { event.project } returns project
        every { event.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) } returns commitMessage
        mockkObject(CommitMessageGenerationContext.Companion)
        every { CommitMessageGenerationContext.from(event, project) } returns context
        mockkStatic(CoroutineScopeService::class)
        every { CoroutineScopeService.getInstance(project) } returns scopeService
        mockkStatic(CommitMessageFormatterDefaultImpl::class)
        every { CommitMessageFormatterDefaultImpl.getInstance(project) } returns formatter
        mockkStatic(CaretServiceDefaultImpl::class)
        every { CaretServiceDefaultImpl.getInstance(project) } returns caretService
        coEvery { formatter.getFormattedCommitMessage(context) } throws PatternSyntaxException("Unclosed group", "[", 0)
        mockkStatic("com.kozhun.commitmessagetemplate.ui.util.ProjectExtentionsKt")
        justRun { project.showCommittleNotification(any(), any()) }

        GenerateCommitMessageAction().actionPerformed(event)

        verify {
            project.showCommittleNotification(
                match { it.startsWith("Failed to generate commit message: Unclosed group") },
                NotificationType.ERROR
            )
        }
        verify(exactly = 0) { commitMessage.setCommitMessage(any()) }
        verify(exactly = 0) { commitMessage.editorField }
    }
}
