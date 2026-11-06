package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.getNotifications
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.waitFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

class GenerationErrorHandlingTest : CommittleIdeTest() {
    @ParameterizedTest(name = "invalid {0} regex")
    @ValueSource(strings = ["task", "type", "scope"])
    fun `invalid regex reports cause and preserves existing message`(variable: String, @TempDir tempDir: Path) {
        runIde("error-$variable-regex", createGitProject(tempDir)) {
            settings {
                setPattern(
                    when (variable) {
                        "task" -> "${'$'}TASK_ID"
                        "type" -> "${'$'}TYPE"
                        else -> "${'$'}SCOPE"
                    }
                )
                when (variable) {
                    "task" -> setTaskIdRegex("[")
                    "type" -> setTypeRegex("[")
                    else -> setScopeRegex("[")
                }
            }
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                page.setMessage("Original message")
                page.insertPatternMessage()
                waitFor("generation error notification", timeout = 15.seconds) {
                    getNotifications().any {
                        it.getGroupId() == "Committle Notifications" &&
                            "Failed to generate commit message" in it.getContent() &&
                            "Unclosed" in it.getContent()
                    }
                }
                assertEquals("Original message", page.message())
            }
        }
    }

    @Test
    fun `generation recovers after invalid settings are fixed`(@TempDir tempDir: Path) {
        runIde("error-recovery", createGitProject(tempDir)) {
            settings {
                setPattern("${'$'}TASK_ID")
                setTaskIdRegex("[")
            }
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                page.setMessage("Original")
                page.insertPatternMessage()
                waitFor("initial generation failure", timeout = 15.seconds) {
                    getNotifications().any { "Failed to generate commit message" in it.getContent() }
                }
                assertEquals("Original", page.message())
                settings { setTaskIdRegex("CMT-\\d+") }
                generateAndAssert(this, "CMT-123")
            }
        }
    }
}
