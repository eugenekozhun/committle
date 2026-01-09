package com.kozhun.commitmessagetemplate.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.kozhun.commitmessagetemplate.service.caret.impl.CaretServiceDefaultImpl
import com.kozhun.commitmessagetemplate.service.formatter.impl.CommitMessageFormatterDefaultImpl
import com.kozhun.commitmessagetemplate.service.helper.CoroutineScopeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GenerateCommitMessageAction : DumbAwareAction() {
    init {
        isEnabledInModalContext = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitMessage = e.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as? CommitMessage ?: return
        val scope = project.service<CoroutineScopeService>().coroutineScope

        scope.launch {
            val formatter = CommitMessageFormatterDefaultImpl.getInstance(project)
            val caretService = CaretServiceDefaultImpl.getInstance(project)
            val (message, offset) = formatter.getFormattedCommitMessage(e)
                .let { caretService.getCaretOffsetByAnchor(it) }

            updateUI(commitMessage, message, offset)
        }
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

