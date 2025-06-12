package com.kozhun.commitmessagetemplate.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
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

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project ?: return
        val coroutineScope = project.getService(CoroutineScopeService::class.java).coroutineScope

        coroutineScope.launch {
            getCommitMessageInput(anActionEvent)
                ?.apply {
                    val formatter = CommitMessageFormatterDefaultImpl.getInstance(project)
                    val caretService = CaretServiceDefaultImpl.getInstance(project)
                    val (message, caretOffset) = formatter.getFormattedCommitMessage(anActionEvent)
                        .let(caretService::getCaretOffsetByAnchor)
                    setCommitMessageWithCaretOffset(message, caretOffset)
                }
        }
    }

    private fun getCommitMessageInput(anActionEvent: AnActionEvent): CommitMessage? {
        return anActionEvent.getData(VcsDataKeys.COMMIT_MESSAGE_CONTROL) as CommitMessage?
    }

    private suspend fun CommitMessage.setCommitMessageWithCaretOffset(message: String, offset: Int) {
        withContext(Dispatchers.EDT) {
            setCommitMessage(message)
            editorField.apply {
                removeSelection()
                requestFocus()
                caretModel.moveToOffset(offset)
            }
        }
    }
}
