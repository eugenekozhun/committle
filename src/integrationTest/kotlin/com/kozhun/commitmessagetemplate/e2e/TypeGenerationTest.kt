package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream

class TypeGenerationTest : CommittleIdeTest() {
    @ParameterizedTest(name = "{0}")
    @MethodSource("typeCases")
    fun `type extraction synonyms defaults and postprocessing`(
        caseName: String,
        branch: String,
        regex: String?,
        defaultValue: String?,
        synonyms: Map<String, String>,
        postProcessor: String,
        expected: String,
        @TempDir tempDir: Path
    ) {
        runIde("type-${caseName.replace(" ", "-")}", createGitProject(tempDir, branch = branch)) {
            settings {
                setPattern("${'$'}TYPE${'$'}CARET_POSITION")
                setTypeRegex(regex)
                setTypeDefault(defaultValue)
                setTypeSynonyms(synonyms)
                setTypePostprocessor(postProcessor)
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, expected)
            }
        }
    }

    @Test
    fun `type default is used for detached head`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        git(project, "checkout", "--detach")
        runIde("type-detached-head", project) {
            settings {
                setPattern("${'$'}TYPE")
                setTypeDefault("feature")
                setTypeSynonyms(mapOf("feature" to "feat"))
                setTypePostprocessor("UPPERCASE")
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, "FEAT")
            }
        }
    }

    companion object {
        @JvmStatic
        fun typeCases(): Stream<Arguments> {
            val standardTypes = listOf("feat", "fix", "docs", "style", "refactor", "perf", "test", "build", "ci", "chore", "revert")
                .map { Arguments.of("standard $it", "$it/CMT-123", null, null, emptyMap<String, String>(), "None", it) }
            val additional = listOf(
                Arguments.of("case insensitive", "Feature/CMT-123", null, null, emptyMap<String, String>(), "None", "Feat"),
                Arguments.of("first match", "fix/feat/CMT-123", null, null, emptyMap<String, String>(), "None", "fix"),
                Arguments.of("missing", "main", null, null, emptyMap<String, String>(), "None", ""),
                Arguments.of("default", "main", null, "chore", emptyMap<String, String>(), "None", "chore"),
                Arguments.of("custom regex", "feature/CMT-123", "feature", null, emptyMap<String, String>(), "None", "feature"),
                Arguments.of("custom mismatch", "feat/CMT-123", "feature", "other", emptyMap<String, String>(), "None", "other"),
                Arguments.of("blank regex", "feat/CMT-123", " ", null, emptyMap<String, String>(), "None", "feat"),
                Arguments.of("synonym", "feature/CMT-123", "feature", null, mapOf("feature" to "feat"), "None", "feat"),
                Arguments.of("synonym miss", "feature/CMT-123", "feature", null, mapOf("bug" to "fix"), "None", "feature"),
                Arguments.of("synonym case sensitive", "Feature/CMT-123", "Feature", null, mapOf("feature" to "feat"), "None", "Feature"),
                Arguments.of("default synonym and case", "main", null, "feature", mapOf("feature" to "feat"), "UPPERCASE", "FEAT"),
                Arguments.of("capitalize", "feat/CMT-123", null, null, emptyMap<String, String>(), "Capitalize", "Feat"),
                Arguments.of("uppercase", "feat/CMT-123", null, null, emptyMap<String, String>(), "UPPERCASE", "FEAT"),
                Arguments.of("lowercase", "Feature/CMT-123", null, null, emptyMap<String, String>(), "lowercase", "feat")
            )
            return Stream.of(*(standardTypes + additional).toTypedArray())
        }
    }
}
