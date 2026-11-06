package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.commitToolWindow
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.waitFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream

class TemplateGenerationTest : CommittleIdeTest() {
    @ParameterizedTest(name = "{0}")
    @MethodSource("templateCases")
    fun `template variants are generated correctly`(
        caseName: String,
        pattern: String,
        expectedMessage: String,
        expectedCaret: Int?,
        @TempDir tempDir: Path
    ) {
        runIde("template-${caseName.replace(" ", "-")}", createGitProject(tempDir)) {
            settings {
                setPattern(pattern)
                setScopeRegex("ui")
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, expectedMessage, expectedCaret ?: expectedMessage.length)
            }
        }
    }

    @Test
    fun `buttons replace existing text clear selection and support repeated generation`(@TempDir tempDir: Path) {
        runIde("template-actions", createGitProject(tempDir)) {
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                page.setMessage("Existing message")
                commitToolWindow().commitEditor().setSelection(0, 8)

                val regularMessages = setOf("feat(api|ui): \n\nCloses CMT-123", "feat(ui|api): \n\nCloses CMT-123")
                val breakingMessages = setOf("feat(api|ui)!: \n\nCloses CMT-123", "feat(ui|api)!: \n\nCloses CMT-123")
                generateAndAssertAny(this, regularMessages, "feat(api|ui): ".length)
                assertNull(page.selection())
                assertEquals(true, page.hasFocus())

                repeat(3) { page.insertPatternMessage() }
                waitFor("rapid repeated generation") { page.message() in regularMessages }
                page.insertBreakingChangePatternMessage()
                waitFor("breaking generation after regular generation") { page.message() in breakingMessages }
                page.insertPatternMessage()
                waitFor("regular generation after breaking generation") { page.message() in regularMessages }
            }
        }
    }

    @Test
    fun `registered shortcuts generate regular and breaking messages`(@TempDir tempDir: Path) {
        runIde("template-shortcuts", createGitProject(tempDir)) {
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssertAny(
                    this,
                    setOf("feat(api|ui): \n\nCloses CMT-123", "feat(ui|api): \n\nCloses CMT-123"),
                    "feat(api|ui): ".length,
                    shortcut = true
                )
                generateAndAssertAny(
                    this,
                    setOf("feat(api|ui)!: \n\nCloses CMT-123", "feat(ui|api)!: \n\nCloses CMT-123"),
                    "feat(api|ui)!: ".length,
                    breakingChange = true,
                    shortcut = true
                )
            }
        }
    }

    @Test
    fun `caret accepts immediate text input`(@TempDir tempDir: Path) {
        runIde("template-caret-input", createGitProject(tempDir)) {
            settings { setPattern("fix: ${'$'}CARET_POSITION") }
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                generateAndAssert(this, "fix: ", 5)
                page.typeText("hello")
                waitFor("typing at generated caret") { page.message() == "fix: hello" }
                assertEquals("fix: hello", page.message())
            }
        }
    }

    companion object {
        @JvmStatic
        fun templateCases(): Stream<Arguments> = Stream.of(
            Arguments.of("empty", "", "", null),
            Arguments.of("plain text", "Custom commit template", "Custom commit template", null),
            Arguments.of("multiline", "Header\n\nBody", "Header\n\nBody", null),
            Arguments.of("unicode", "Исправление ✨", "Исправление ✨", null),
            Arguments.of("unknown variable", "Value ${'$'}UNKNOWN", "Value ${'$'}UNKNOWN", null),
            Arguments.of("single dollar", "Value $", "Value $", null),
            Arguments.of("incomplete variable", "Value ${'$'}TASK", "Value ${'$'}TASK", null),
            Arguments.of(
                "all variables",
                "${'$'}TYPE|${'$'}TASK_ID|${'$'}SCOPE|${'$'}BREAKING_CHANGE|${'$'}CARET_POSITION",
                "feat|CMT-123|ui|",
                16
            ),
            Arguments.of(
                "repeated variables",
                "${'$'}TYPE ${'$'}TYPE ${'$'}TASK_ID ${'$'}TASK_ID",
                "feat feat CMT-123 CMT-123",
                null
            ),
            Arguments.of(
                "different order",
                "${'$'}TASK_ID ${'$'}SCOPE ${'$'}TYPE",
                "CMT-123 ui feat",
                null
            )
        )
    }
}
