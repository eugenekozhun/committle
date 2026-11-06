package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.invokeActionByShortcut
import com.intellij.driver.sdk.ui.components.common.IdeaFrameUI
import com.intellij.driver.sdk.ui.components.common.commitToolWindow
import com.intellij.driver.sdk.ui.components.elements.actionButtonByXpath
import com.intellij.driver.sdk.ui.enabled
import com.intellij.driver.sdk.ui.present
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.ui.xQuery

class CommittleCommitPage(private val frame: IdeaFrameUI) {
    fun open() {
        frame.leftToolWindowToolbar.commitButton.open()
        frame.commitToolWindow().commitEditor().shouldBe("Commit message editor is not visible", present)
    }

    fun insertPatternMessage() {
        clickAction("Insert Pattern Message")
    }

    fun insertBreakingChangePatternMessage() {
        clickAction("Insert Breaking Change Pattern Message")
    }

    fun invokePatternShortcut() {
        frame.commitToolWindow().commitEditor().invokeActionByShortcut(GENERATE_ACTION_ID)
    }

    fun invokeBreakingChangeShortcut() {
        frame.commitToolWindow().commitEditor().invokeActionByShortcut(BREAKING_ACTION_ID)
    }

    fun message(): String = frame.commitToolWindow().commitEditor().text

    fun setMessage(value: String) {
        frame.commitToolWindow().commitEditor().text = value
    }

    fun caretOffset(): Int = frame.driver.withReadAction {
        frame.commitToolWindow().commitEditor().editor.getCaretModel().getOffset()
    }

    fun selection(): String? = frame.commitToolWindow().commitEditor().getSelection()

    fun hasFocus(): Boolean = frame.driver.withContext(com.intellij.driver.model.OnDispatcher.EDT) {
        frame.driver.cast(frame.commitToolWindow().commitEditor().component, AwtComponentRef::class).isFocusOwner()
    }

    fun selectOnly(vararg fileNames: String) {
        val allFiles = setOf("UIButton.kt", "ApiClient.kt")
        allFiles.filterNot { it in fileNames }.forEach(frame.commitToolWindow()::unselectFile)
        fileNames.forEach(frame.commitToolWindow()::selectFile)
    }

    fun typeText(value: String) {
        frame.commitToolWindow().commitEditor().keyboard {
            typeText(value, delayBetweenCharsInMs = 0)
        }
    }

    fun commit() {
        frame.commitToolWindow().commitButton.click()
    }

    private fun clickAction(accessibleName: String) {
        frame.commitToolWindow().actionButtonByXpath(xQuery { byAccessibleName(accessibleName) })
            .shouldBe("Commit action is not enabled: $accessibleName", enabled)
            .performAction()
    }

    private companion object {
        const val GENERATE_ACTION_ID = "com.kozhun.commitmessagetemplate.action.GenerateCommitMessageAction"
        const val BREAKING_ACTION_ID = "com.kozhun.commitmessagetemplate.action.GenerateBreakingChangeCommitMessageAction"
    }
}
