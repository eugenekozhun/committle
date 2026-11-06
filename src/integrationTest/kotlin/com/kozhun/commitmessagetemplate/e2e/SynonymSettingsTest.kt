package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SynonymSettingsTest : CommittleIdeTest() {
    @Test
    fun `synonyms can be added edited removed and persisted`(@TempDir tempDir: Path) {
        runIde("synonyms-crud", createGitProject(tempDir)) {
            ideFrame {
                openCommittleSettings().apply {
                    addSynonym("feature", "feat")
                    addSynonym("bug", "fix")
                    apply()
                    assertEquals(mapOf("feature" to "feat", "bug" to "fix"), settingsState().getTypeSynonyms())
                    editSynonym(synonymsTable().findRowColumn { it == "feature" }.first, "feature", "feat-updated")
                    apply()
                    assertEquals("feat-updated", settingsState().getTypeSynonyms()["feature"])
                    removeSynonym(synonymsTable().findRowColumn { it == "bug" }.first)
                    apply()
                    assertEquals(mapOf("feature" to "feat-updated"), settingsState().getTypeSynonyms())
                    close()
                }
                openCommittleSettings().apply {
                    expandAdvancedGroups()
                    assertEquals(1, synonymsTable().rowCount())
                    assertEquals("feature", synonymsTable().getValueAt(0, 0))
                    assertEquals("feat-updated", synonymsTable().getValueAt(0, 1))
                    cancel()
                }
            }
        }
    }

    @Test
    fun `synonym dialogs validate input and support cancel`(@TempDir tempDir: Path) {
        runIde("synonyms-validation", createGitProject(tempDir)) {
            settings { setTypeSynonyms(mapOf("feature" to "feat")) }
            ideFrame {
                openCommittleSettings().apply {
                    expandAdvancedGroups()
                    val table = synonymsTable()
                    assertEquals(1, table.rowCount())
                    addSynonym("", "fix")
                    assertEquals(1, table.rowCount())
                    cancelSynonymDialog()
                    cancel()
                }

                openCommittleSettings().apply {
                    addSynonym("feature", "fix")
                    assertEquals(1, synonymsTable().rowCount())
                    cancelSynonymDialog()
                    cancel()
                }

                openCommittleSettings().apply {
                    addSynonym("bug", "")
                    assertEquals(1, synonymsTable().rowCount())
                    cancelSynonymDialog()
                    cancel()
                }

                openCommittleSettings().apply {
                    addSynonym("bug", "fix", confirm = false)
                    editSynonym(0, "feature", "feat", confirm = false)
                    assertEquals(mapOf("feature" to "feat"), settingsState().getTypeSynonyms())
                    cancel()
                }
            }
        }
    }

    @Test
    fun `synonym added through ui is used in generation`(@TempDir tempDir: Path) {
        runIde("synonyms-generation", createGitProject(tempDir, branch = "feature/CMT-123")) {
            settings {
                setPattern("${'$'}TYPE")
                setTypeRegex("feature")
            }
            ideFrame {
                openCommittleSettings().apply {
                    addSynonym("feature", "feat")
                    close()
                }
                CommittleCommitPage(this).open()
                generateAndAssert(this, "feat")
            }
        }
    }
}
