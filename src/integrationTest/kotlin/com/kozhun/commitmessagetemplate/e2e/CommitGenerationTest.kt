package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.waitFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

class CommitGenerationTest : CommittleIdeTest() {
    @Test
    fun `commit message is generated from branch and selected changes`(@TempDir tempDir: Path) {
        runIde("commitGeneration", createGitProject(tempDir)) {
            ideFrame {
                val commitPage = CommittleCommitPage(this)
                commitPage.open()

                commitPage.insertPatternMessage()
                val expectedMessages = setOf(
                    "feat(api|ui): \n\nCloses CMT-123",
                    "feat(ui|api): \n\nCloses CMT-123"
                )
                waitFor("regular commit message generation", timeout = 15.seconds) {
                    commitPage.message() in expectedMessages
                }
                assertTrue(commitPage.message() in expectedMessages)
                assertEquals("feat(api|ui): ".length, commitPage.caretOffset())

                commitPage.insertBreakingChangePatternMessage()
                val expectedBreakingMessages = setOf(
                    "feat(api|ui)!: \n\nCloses CMT-123",
                    "feat(ui|api)!: \n\nCloses CMT-123"
                )
                waitFor("breaking change commit message generation", timeout = 15.seconds) {
                    commitPage.message() in expectedBreakingMessages
                }
                assertTrue(commitPage.message() in expectedBreakingMessages)
                assertEquals("feat(api|ui)!: ".length, commitPage.caretOffset())
            }
        }
    }
}
