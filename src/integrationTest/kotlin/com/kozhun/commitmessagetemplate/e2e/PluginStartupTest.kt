package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@Tag("smoke")
class PluginStartupTest : CommittleIdeTest() {
    @Test
    fun `plugin starts in the latest supported IDE`() {
        runIde("pluginStartup") {}
    }

    @Test
    fun `plugin starts with a git project and opens committle settings`(@TempDir tempDir: Path) {
        runIde("pluginStartupGit", createGitProject(tempDir)) {
            ideFrame {
                openCommittleSettings().cancel()
            }
        }
    }

    @Test
    fun `plugin starts with a project without git`(@TempDir tempDir: Path) {
        val project = tempDir.resolve("plain-project").createDirectories()
        project.resolve("file.txt").writeText("plain")
        runIde("pluginStartupWithoutGit", project) {
            ideFrame {
                openCommittleSettings().cancel()
            }
        }
    }
}
