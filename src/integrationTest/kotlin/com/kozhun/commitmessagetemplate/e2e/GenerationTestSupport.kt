package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.client.Driver
import com.intellij.driver.sdk.ui.components.common.IdeaFrameUI
import com.intellij.driver.sdk.waitFor
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.time.Duration.Companion.seconds

internal fun Driver.generateAndAssert(
    frame: IdeaFrameUI,
    expectedMessage: String,
    expectedCaret: Int = expectedMessage.length,
    breakingChange: Boolean = false,
    shortcut: Boolean = false
) = generateAndAssertAny(frame, setOf(expectedMessage), expectedCaret, breakingChange, shortcut)

internal fun Driver.generateAndAssertAny(
    frame: IdeaFrameUI,
    expectedMessages: Set<String>,
    expectedCaret: Int,
    breakingChange: Boolean = false,
    shortcut: Boolean = false
) {
    val page = CommittleCommitPage(frame)
    when {
        shortcut && breakingChange -> page.invokeBreakingChangeShortcut()
        shortcut -> page.invokePatternShortcut()
        breakingChange -> page.insertBreakingChangePatternMessage()
        else -> page.insertPatternMessage()
    }
    waitFor("commit message generation", timeout = 15.seconds) { page.message() in expectedMessages }
    assertEquals(true, page.message() in expectedMessages)
    assertEquals(expectedCaret, page.caretOffset())
}
