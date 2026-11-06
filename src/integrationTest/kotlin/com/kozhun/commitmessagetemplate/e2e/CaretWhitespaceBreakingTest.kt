package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream

class CaretWhitespaceBreakingTest : CommittleIdeTest() {
    @ParameterizedTest(name = "caret: {0}")
    @MethodSource("caretCases")
    fun `caret position variants`(
        caseName: String,
        pattern: String,
        expectedMessage: String,
        expectedCaret: Int,
        @TempDir tempDir: Path
    ) {
        runIde("caret-${caseName.replace(" ", "-")}", createGitProject(tempDir)) {
            settings { setPattern(pattern) }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, expectedMessage, expectedCaret)
            }
        }
    }

    @ParameterizedTest(name = "whitespace: {0}")
    @MethodSource("whitespaceCases")
    fun `whitespace options and their composition`(
        caseName: String,
        pattern: String,
        trimStart: Boolean,
        trimEnd: Boolean,
        consolidate: Boolean,
        expectedMessage: String,
        @TempDir tempDir: Path
    ) {
        runIde("whitespace-${caseName.replace(" ", "-")}", createGitProject(tempDir)) {
            settings {
                setPattern(pattern)
                setTrimWhitespacesStart(trimStart)
                setTrimWhitespacesEnd(trimEnd)
                setUnnecessaryWhitespaces(consolidate)
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, expectedMessage)
            }
        }
    }

    @ParameterizedTest(name = "breaking: {0}")
    @MethodSource("breakingCases")
    fun `breaking marker variants`(caseName: String, pattern: String, expected: String, @TempDir tempDir: Path) {
        runIde("breaking-${caseName.replace(" ", "-")}", createGitProject(tempDir)) {
            settings { setPattern(pattern) }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, expected.replace("!", ""), breakingChange = false)
                generateAndAssert(this, expected, breakingChange = true)
            }
        }
    }

    companion object {
        @JvmStatic
        fun caretCases(): Stream<Arguments> = Stream.of(
            Arguments.of("absent", "Hello world", "Hello world", 11),
            Arguments.of("start", "${'$'}CARET_POSITION Hello", " Hello", 0),
            Arguments.of("middle", "Hello ${'$'}CARET_POSITION world", "Hello  world", 6),
            Arguments.of("end", "Hello ${'$'}CARET_POSITION", "Hello ", 6),
            Arguments.of("multiline", "Header\n${'$'}CARET_POSITION\nBody", "Header\n\nBody", 7),
            Arguments.of(
                "multiple anchors",
                "A${'$'}CARET_POSITION B ${'$'}CARET_POSITION C",
                "A B ${'$'}CARET_POSITION C",
                1
            ),
            Arguments.of("unicode", "✨ ${'$'}CARET_POSITION text", "✨  text", 3)
        )

        @JvmStatic
        fun whitespaceCases(): Stream<Arguments> = Stream.of(
            Arguments.of("disabled", "  Hello  world  ", false, false, false, "  Hello  world  "),
            Arguments.of("trim start", "  Hello world ", true, false, false, "Hello world "),
            Arguments.of("trim end", " Hello world  ", false, true, false, " Hello world"),
            Arguments.of("both trims", "  Hello world  ", true, true, false, "Hello world"),
            Arguments.of("spaces", "Hello   world", false, false, true, "Hello world"),
            Arguments.of("tabs and newlines", "Hello\t \nworld", false, false, true, "Hello world"),
            Arguments.of("all rules", "  Hello   world  ", true, true, true, "Hello world"),
            Arguments.of("only whitespace", "  \t\n ", true, true, true, ""),
            Arguments.of("multiline preserved", "Header\n\nBody", false, false, false, "Header\n\nBody"),
            Arguments.of(
                "after replacements",
                "  ${'$'}TYPE   ${'$'}TASK_ID  ${'$'}CARET_POSITION",
                true,
                true,
                true,
                "feat CMT-123 "
            )
        )

        @JvmStatic
        fun breakingCases(): Stream<Arguments> = Stream.of(
            Arguments.of("conventional", "${'$'}TYPE${'$'}BREAKING_CHANGE:", "feat!:"),
            Arguments.of("without variable", "${'$'}TYPE:", "feat:"),
            Arguments.of("multiple markers", "${'$'}BREAKING_CHANGE${'$'}TYPE${'$'}BREAKING_CHANGE", "!feat!"),
            Arguments.of("empty pattern", "", "")
        )
    }
}
