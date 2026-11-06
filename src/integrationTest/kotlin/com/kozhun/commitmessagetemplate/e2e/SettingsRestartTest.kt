package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.client.service
import com.intellij.driver.client.utility
import com.intellij.driver.sdk.getOpenProjects
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitForIndicators
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.minutes

class SettingsRestartTest : CommittleIdeTest() {
    @Test
    fun `project settings survive a complete ide restart`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        val pattern = "fix: ${'$'}CARET_POSITION\n\nCloses ${'$'}TASK_ID"
        runIde("settings-restart-write", project) {
            settings {
                setPattern(pattern)
                setTaskIdDefault("CMT-000")
                setTypeSynonyms(mapOf("feature" to "feat"))
                setTrimWhitespacesEnd(true)
            }
        }

        assertEquals(true, project.resolve(".idea/commit-message-template.xml").readText().contains("Closes"))
        runIde("settings-restart-read", project) {
            val state = settingsState()
            assertEquals(pattern, state.getPattern())
            assertEquals("CMT-000", state.getTaskIdDefault())
            assertEquals(mapOf("feature" to "feat"), state.getTypeSynonyms())
            assertEquals(true, state.getTrimWhitespacesEnd())
        }
    }

    @Test
    fun `project name migration is executed only once across restarts`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir) { replacePattern(it, "${'$'}PROJECT_NAME") }
        runIde("migration-restart-first", project) {
            assertEquals("${'$'}SCOPE", settingsState().getPattern())
        }

        replacePattern(project, "${'$'}PROJECT_NAME")
        runIde("migration-restart-second", project) {
            assertEquals("${'$'}PROJECT_NAME", settingsState().getPattern())
        }
    }

    @Test
    fun `two open projects keep independent committle settings`(@TempDir tempDir: Path) {
        val first = createGitProject(tempDir.resolve("first").createDirectories())
        val second = createGitProject(tempDir.resolve("second").createDirectories())
        runIde("settings-project-isolation", first) {
            val firstProject = singleProject()
            val secondProject = withContext {
                utility(ProjectManagerExRef::class).openProject(second)
            }
            requireNotNull(secondProject)
            waitForIndicators(2.minutes)
            assertEquals(2, getOpenProjects().size)
            withContext {
                service(SettingsStorageRef::class, firstProject).getState().setPattern("first")
                service(SettingsStorageRef::class, secondProject).getState().setPattern("second")
                assertEquals("first", service(SettingsStorageRef::class, firstProject).getState().getPattern())
                assertEquals("second", service(SettingsStorageRef::class, secondProject).getState().getPattern())
            }
        }
    }
}
