package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

@Tag("smoke")
class SettingsPersistenceTest : CommittleIdeTest() {
    @Test
    fun `settings are saved and restored in the settings dialog`(@TempDir tempDir: Path) {
        val updatedPattern = "fix: ${'$'}CARET_POSITION\n\nCloses ${'$'}TASK_ID"

        runIde("settingsPersistence", createGitProject(tempDir)) {
            ideFrame {
                openCommittleSettings().apply {
                    pattern = updatedPattern
                    apply()
                    close()
                }

                withContext {
                    val storage = service(SettingsStorageRef::class, singleProject())
                    assertEquals(updatedPattern, storage.getState().getPattern())
                }

                openCommittleSettings().apply {
                    assertEquals(updatedPattern, pattern)
                    close()
                }
            }
        }
    }
}
