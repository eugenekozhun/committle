package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.waitFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

class CommitWorkflowTest : CommittleIdeTest() {
    @Test
    fun `configured template generates and creates a real git commit`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, branch = "feature/task-123")
        runIde("workflow-real-commit", project) {
            settings {
                setPattern("${'$'}TYPE(${'$'}SCOPE)${'$'}BREAKING_CHANGE: ${'$'}CARET_POSITION\n\nCloses ${'$'}TASK_ID")
                setTaskIdRegex("task-\\d+")
                setTaskIdPostProcessor("UPPERCASE")
                setTypeRegex("feature")
                setTypeSynonyms(mapOf("feature" to "feat"))
                setTypePostprocessor("UPPERCASE")
                setScopeRegex("(?<=/)(ui|api)(?=/)")
                setScopePostprocessor("UPPERCASE")
            }
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                page.selectOnly("UIButton.kt")
                generateAndAssert(this, "FEAT(UI)!: \n\nCloses TASK-123", 11, breakingChange = true)
                page.typeText("Implement workflow")
                page.commit()
            }
            waitFor("Git commit completion", timeout = 30.seconds) {
                git(project, "status", "--porcelain").lines().none { it.endsWith("ui/UIButton.kt") }
            }
            assertEquals("FEAT(UI)!: Implement workflow\n\nCloses TASK-123", git(project, "log", "-1", "--pretty=%B"))
            assertEquals("M api/ApiClient.kt", git(project, "status", "--porcelain"))
        }
    }
}
