package com.kozhun.commitmessagetemplate.e2e

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

    fun message(): String = frame.commitToolWindow().commitEditor().text

    fun caretOffset(): Int = frame.driver.withReadAction {
        frame.commitToolWindow().commitEditor().editor.getCaretModel().getOffset()
    }

    private fun clickAction(accessibleName: String) {
        frame.commitToolWindow().actionButtonByXpath(xQuery { byAccessibleName(accessibleName) })
            .shouldBe("Commit action is not enabled: $accessibleName", enabled)
            .performAction()
    }
}
