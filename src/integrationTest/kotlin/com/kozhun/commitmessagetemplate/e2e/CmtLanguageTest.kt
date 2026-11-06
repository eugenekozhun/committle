package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.client.service
import com.intellij.driver.client.utility
import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.PsiManager
import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.common.codeEditorForFile
import com.intellij.driver.sdk.ui.components.common.editor.completionList
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.ui.present
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.waitFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

class CmtLanguageTest : CommittleIdeTest() {
    @Test
    fun `cmt file type PSI and all variable tokens are available in a real editor`(@TempDir tempDir: Path) {
        val text = "plain ${'$'}TASK_ID ${'$'}TYPE ${'$'}SCOPE ${'$'}BREAKING_CHANGE ${'$'}CARET_POSITION ${'$'}UNKNOWN $"
        val project = createGitProject(tempDir, dirtyFiles = false) {
            it.resolve("template.cmt").writeText(text)
        }
        runIde("language-psi", project) {
            val file = withContext(OnDispatcher.EDT) {
                service(LocalFileSystemRef::class).refreshAndFindFileByPath(project.resolve("template.cmt").toString())
            }
            requireNotNull(file)
            assertEquals("Committle", service(FileTypeManagerRef::class).getFileTypeByFile(file).getName())

            val psi = withReadAction {
                service(PsiManager::class, singleProject()).findFile(file)
            }
            requireNotNull(psi)
            val leaves = withReadAction { cast(psi, CmtPsiElementRef::class).leafTexts() }
            assertEquals(text, leaves.joinToString(""))
            listOf("TASK_ID", "TYPE", "SCOPE", "BREAKING_CHANGE", "CARET_POSITION").forEach {
                assertTrue(leaves.contains("$$it"), "Missing PSI token $$it in $leaves")
            }
            assertTrue(leaves.contains("${'$'}UNKNOWN"))
            assertTrue(leaves.contains("$"))

            withContext(OnDispatcher.EDT) {
                service(FileEditorManagerRef::class, singleProject()).openFile(file, true)
            }
            ideFrame {
                codeEditorForFile("template.cmt").shouldBe("CMT editor is visible", com.intellij.driver.sdk.ui.present)
            }
        }
    }

    @Test
    fun `completion offers and inserts exactly the supported variables`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, dirtyFiles = false) {
            it.resolve("completion.cmt").writeText("")
        }
        runIde("language-completion", project) {
            val file = withContext(OnDispatcher.EDT) {
                service(LocalFileSystemRef::class).refreshAndFindFileByPath(project.resolve("completion.cmt").toString())
            }
            withContext(OnDispatcher.EDT) {
                service(FileEditorManagerRef::class, singleProject()).openFile(requireNotNull(file), true)
            }
            ideFrame {
                val editor = codeEditorForFile("completion.cmt")
                invokeAction("CodeCompletion", component = editor.component)
                waitFor("CMT completion list", timeout = 15.seconds) {
                    completionList().items.any { it.contains("${'$'}TASK_ID") }
                }
                val items = completionList().items
                listOf("TASK_ID", "TYPE", "SCOPE", "BREAKING_CHANGE", "CARET_POSITION").forEach { variable ->
                    assertTrue(items.any { it.contains("$$variable") }, "Missing $$variable in $items")
                }
                assertEquals(5, items.count { it.contains("${'$'}TASK_ID") || it.contains("${'$'}TYPE") || it.contains("${'$'}SCOPE") || it.contains("${'$'}BREAKING_CHANGE") || it.contains("${'$'}CARET_POSITION") })
                completionList().clickItem("${'$'}TYPE", fullMatch = false)
                waitFor("completion insertion") { editor.text == "${'$'}TYPE" }
                assertEquals("${'$'}TYPE", editor.text)
            }
        }
    }

    @Test
    fun `completion works in the settings pattern editor`(@TempDir tempDir: Path) {
        runIde("language-settings-completion", createGitProject(tempDir)) {
            ideFrame {
                openCommittleSettings().apply {
                    pattern = ""
                    invokePatternCompletion()
                    waitFor("Settings CMT completion list", timeout = 15.seconds) {
                        completionList().items.any { it.contains("${'$'}SCOPE") }
                    }
                    completionList().clickItem("${'$'}SCOPE", fullMatch = false)
                    assertEquals("${'$'}SCOPE", pattern)
                    close()
                }
                assertEquals("${'$'}SCOPE", settingsState().getPattern())
            }
        }
    }

    @Test
    fun `semantic highlighting distinguishes variables caret and plain text`() {
        runIde("language-highlighting") {
            val highlighter = new(CmtSyntaxHighlighterRef::class)
            val lexer = highlighter.getHighlightingLexer()
            val text = "plain ${'$'}TASK_ID ${'$'}TYPE ${'$'}SCOPE ${'$'}BREAKING_CHANGE ${'$'}CARET_POSITION"
            lexer.start(text)
            val highlights = mutableMapOf<String, List<String>>()
            while (true) {
                val tokenType = lexer.getTokenType() ?: break
                val tokenText = lexer.getTokenText()
                val keys = highlighter.getTokenHighlights(tokenType).map { it.getExternalName() }
                if (tokenText.isNotBlank()) highlights[tokenText] = keys
                lexer.advance()
            }
            listOf("TASK_ID", "TYPE", "SCOPE", "BREAKING_CHANGE").forEach {
                assertEquals(listOf("CMT_KEYWORD"), highlights["$$it"])
            }
            assertEquals(listOf("CMT_CARET_POSITION"), highlights["${'$'}CARET_POSITION"])
            assertEquals(emptyList<String>(), highlights["plain"])
        }
    }

    private fun CmtPsiElementRef.leafTexts(): List<String> {
        val children = getChildren()
        return if (children.isEmpty()) listOf(getText().orEmpty()) else children.flatMap { it.leafTexts() }
    }
}
