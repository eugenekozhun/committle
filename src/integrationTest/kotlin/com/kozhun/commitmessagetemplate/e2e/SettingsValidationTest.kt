package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.components.elements.JTextFieldUI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SettingsValidationTest : CommittleIdeTest() {
    @Test
    fun `invalid regex fields block apply without changing persisted state`(@TempDir tempDir: Path) {
        runIde("settings-regex-validation", createGitProject(tempDir)) {
            ideFrame {
                openCommittleSettings().apply {
                    expandAdvancedGroups()
                    assertInvalidThenValid(taskRegex(), "TASK-\\d+") { settingsState().getTaskIdRegex() }
                    assertInvalidThenValid(typeRegex(), "feature|bug") { settingsState().getTypeRegex() }
                    assertInvalidThenValid(scopeRegex(), "(?<=/)(ui|api)(?=/)") { settingsState().getScopeRegex() }
                    cancel()
                }
            }
        }
    }

    private fun CommittleSettingsPage.assertInvalidThenValid(
        field: JTextFieldUI,
        validValue: String,
        persistedValue: () -> String?
    ) {
        val original = persistedValue()
        field.text = "["
        apply()
        assertEquals(original, persistedValue())
        assertEquals("[", field.text)
        field.text = validValue
        apply()
        assertEquals(validValue, persistedValue())
    }
}
