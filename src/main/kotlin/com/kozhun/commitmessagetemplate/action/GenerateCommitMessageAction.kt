package com.kozhun.commitmessagetemplate.action

import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.kozhun.commitmessagetemplate.service.caret.impl.CaretServiceDefaultImpl
import com.kozhun.commitmessagetemplate.service.formatter.CommitMessageGenerationContext
import com.kozhun.commitmessagetemplate.service.formatter.impl.CommitMessageFormatterDefaultImpl
import com.kozhun.commitmessagetemplate.service.helper.CoroutineScopeService
import com.kozhun.commitmessagetemplate.ui.util.showCommittleNotification
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseGenerateCommitMessageAction(
    private val isBreakingChange: Boolean
) : DumbAwareAction() {
    private val logger = Logger.getInstance(javaClass)

    init {
        isEnabledInModalContext = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitMessage = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as? CommitMessage ?: return
        val context = CommitMessageGenerationContext.from(e, project).copy(isBreakingChange = isBreakingChange)
        val scope = CoroutineScopeService.getInstance(project).coroutineScope

        scope.launch {
            try {
                val formatter = CommitMessageFormatterDefaultImpl.getInstance(project)
                val caretService = CaretServiceDefaultImpl.getInstance(project)
                val (message, offset) = formatter.getFormattedCommitMessage(context)
                    .let { caretService.getCaretOffsetByAnchor(it) }

                updateUI(commitMessage, message, offset)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IllegalArgumentException) {
                handleFailure(project, exception)
            } catch (exception: IllegalStateException) {
                handleFailure(project, exception)
            }
        }
    }

    private fun handleFailure(project: Project, exception: RuntimeException) {
        logger.warn("Failed to generate commit message", exception)
        val reason = exception.message ?: "Unknown error"
        project.showCommittleNotification("Failed to generate commit message: $reason", NotificationType.ERROR)
    }

    private suspend fun updateUI(commitMessage: CommitMessage, message: String, offset: Int) {
        withContext(Dispatchers.EDT) {
            commitMessage.setCommitMessage(message)
            commitMessage.editorField.apply {
                removeSelection()
                requestFocus()
                caretModel.moveToOffset(offset)
            }
        }
    }
}

class GenerateCommitMessageAction : BaseGenerateCommitMessageAction(isBreakingChange = false)

