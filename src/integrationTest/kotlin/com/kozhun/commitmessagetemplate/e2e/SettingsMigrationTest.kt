package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.writeText

@Tag("smoke")
class SettingsMigrationTest : CommittleIdeTest() {
    @ParameterizedTest(name = "{0}")
    @MethodSource("migrationCases")
    fun `project name migration handles all pattern variants`(
        caseName: String,
        pattern: String,
        expected: String,
        @TempDir tempDir: Path
    ) {
        val project = createGitProject(tempDir) { replacePattern(it, pattern) }
        runIde("migration-${caseName.replace(" ", "-")}", project) {
            assertEquals(expected, settingsState().getPattern().orEmpty())
        }
    }

    @Test
    fun `migrated project name is generated as scope`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir) {
            replacePattern(it, "${'$'}TYPE(${'$'}PROJECT_NAME): ${'$'}CARET_POSITION")
        }
        runIde("migration-generation", project) {
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                page.selectOnly("UIButton.kt", "ApiClient.kt")
                generateAndAssertAny(this, setOf("feat(ui|api): ", "feat(api|ui): "), "feat(api|ui): ".length)
            }
        }
    }

    @Test
    fun `legacy misc xml settings are loaded and migrated without losing other fields`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir) {
            it.resolve(".idea/commit-message-template.xml").toFile().delete()
            it.resolve(".idea/misc.xml").writeText(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project version="4">
                  <component name="commit-message-template">
                    <option name="pattern" value="${'$'}PROJECT_NAME ${'$'}TASK_ID" />
                    <option name="scopeRegex" value="(?&lt;=/)(ui|api)(?=/)" />
                    <option name="trimWhitespacesEnd" value="true" />
                  </component>
                </project>
                """.trimIndent()
            )
        }
        runIde("migration-legacy-storage", project) {
            val state = settingsState()
            assertEquals("${'$'}SCOPE ${'$'}TASK_ID", state.getPattern())
            assertEquals("(?<=/)(ui|api)(?=/)", state.getScopeRegex())
            assertEquals(true, state.getTrimWhitespacesEnd())
        }
    }

    companion object {
        @JvmStatic
        fun migrationCases(): Stream<Arguments> = Stream.of(
            Arguments.of("single", "${'$'}PROJECT_NAME", "${'$'}SCOPE"),
            Arguments.of("multiple", "${'$'}PROJECT_NAME ${'$'}PROJECT_NAME", "${'$'}SCOPE ${'$'}SCOPE"),
            Arguments.of("mixed", "${'$'}TYPE ${'$'}PROJECT_NAME ${'$'}TASK_ID", "${'$'}TYPE ${'$'}SCOPE ${'$'}TASK_ID"),
            Arguments.of("without legacy variable", "plain ${'$'}TYPE", "plain ${'$'}TYPE"),
            Arguments.of("empty", "", "")
        )
    }
}
