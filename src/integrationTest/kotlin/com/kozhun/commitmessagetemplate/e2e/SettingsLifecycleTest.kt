package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SettingsLifecycleTest : CommittleIdeTest() {
    @Test
    fun `default settings are shown for a fresh project`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir) {
            it.resolve(".idea/commit-message-template.xml").toFile().delete()
        }
        runIde("settings-defaults", project) {
            ideFrame {
                openCommittleSettings().apply {
                    assertEquals("", pattern)
                    assertFalse(trimStart)
                    assertFalse(trimEnd)
                    assertFalse(consolidateWhitespaces)
                    cancel()
                }
            }
        }
    }

    @Test
    fun `cancel and reset do not change persisted pattern`(@TempDir tempDir: Path) {
        val original = "$" + "TYPE: " + "$" + "CARET_POSITION"
        runIde("settings-cancel-reset", createGitProject(tempDir)) {
            settings { setPattern(original) }
            ideFrame {
                openCommittleSettings().apply {
                    pattern = "draft"
                    reset()
                    assertEquals(original, pattern)
                    pattern = "cancelled"
                    cancel()
                }
                assertEquals(original, settingsState().getPattern())
                openCommittleSettings().apply {
                    assertEquals(original, pattern)
                    cancel()
                }
            }
        }
    }

    @Test
    fun `all settings fields survive apply and reopen`(@TempDir tempDir: Path) {
        runIde("settings-all-fields", createGitProject(tempDir)) {
            ideFrame {
                openCommittleSettings().apply {
                    pattern = "${'$'}TYPE(${'$'}SCOPE): ${'$'}TASK_ID ${'$'}CARET_POSITION"
                    trimStart = true
                    trimEnd = true
                    consolidateWhitespaces = true
                    expandAdvancedGroups()
                    taskRegex().text = "TASK-\\d+"
                    taskDefault().text = "TASK-0"
                    taskCase().selectItem("UPPERCASE")
                    typeRegex().text = "feature|bug"
                    typeDefault().text = "feature"
                    typeCase().selectItem("lowercase")
                    scopeRegex().text = "(?<=/)(ui|api)(?=/)"
                    scopeDefault().text = "core"
                    scopeSeparator().text = ","
                    scopeCase().selectItem("Capitalize")
                    apply()
                    close()
                }

                val state = settingsState()
                assertEquals("TASK-\\d+", state.getTaskIdRegex())
                assertEquals("TASK-0", state.getTaskIdDefault())
                assertEquals("UPPERCASE", state.getTaskIdPostProcessor())
                assertEquals("feature|bug", state.getTypeRegex())
                assertEquals("feature", state.getTypeDefault())
                assertEquals("lowercase", state.getTypePostprocessor())
                assertEquals("core", state.getScopeDefault())
                assertEquals(",", state.getScopeSeparator())
                assertEquals("Capitalize", state.getScopePostprocessor())
                assertEquals(true, state.getTrimWhitespacesStart())
                assertEquals(true, state.getTrimWhitespacesEnd())
                assertEquals(true, state.getUnnecessaryWhitespaces())

                openCommittleSettings().apply {
                    assertEquals("${'$'}TYPE(${'$'}SCOPE): ${'$'}TASK_ID ${'$'}CARET_POSITION", pattern)
                    expandAdvancedGroups()
                    assertEquals("TASK-\\d+", taskRegex().text)
                    assertEquals("TASK-0", taskDefault().text)
                    assertEquals("UPPERCASE", taskCase().getSelectedItem())
                    assertEquals("feature|bug", typeRegex().text)
                    assertEquals("feature", typeDefault().text)
                    assertEquals("lowercase", typeCase().getSelectedItem())
                    assertEquals("core", scopeDefault().text)
                    assertEquals(",", scopeSeparator().text)
                    assertEquals("Capitalize", scopeCase().getSelectedItem())
                    cancel()
                }
            }
        }
    }

    @Test
    fun `blank advanced fields retain default generation behavior`(@TempDir tempDir: Path) {
        runIde("settings-blank-fields", createGitProject(tempDir)) {
            ideFrame {
                openCommittleSettings().apply {
                    expandAdvancedGroups()
                    taskRegex().text = ""
                    typeRegex().text = ""
                    scopeSeparator().text = ""
                    apply()
                    close()
                }
                val page = CommittleCommitPage(this)
                page.open()
                page.selectOnly("UIButton.kt", "ApiClient.kt")
                generateAndAssertAny(
                    this,
                    setOf("feat(api|ui): \n\nCloses CMT-123", "feat(ui|api): \n\nCloses CMT-123"),
                    "feat(api|ui): ".length
                )
            }
        }
    }
}
