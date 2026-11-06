package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream

class TaskIdGenerationTest : CommittleIdeTest() {
    @ParameterizedTest(name = "{0}")
    @MethodSource("taskIdCases")
    fun `task id extraction defaults and postprocessing`(
        caseName: String,
        branch: String,
        regex: String?,
        defaultValue: String?,
        postProcessor: String,
        expected: String,
        @TempDir tempDir: Path
    ) {
        runIde("task-${caseName.replace(" ", "-")}", createGitProject(tempDir, branch = branch)) {
            settings {
                setPattern("${'$'}TASK_ID${'$'}CARET_POSITION")
                setTaskIdRegex(regex)
                setTaskIdDefault(defaultValue)
                setTaskIdPostProcessor(postProcessor)
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, expected)
            }
        }
    }

    @Test
    fun `task id default is used for detached head`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        git(project, "checkout", "--detach")
        runIde("task-detached-head", project) {
            settings {
                setPattern("${'$'}TASK_ID")
                setTaskIdDefault("FALLBACK-1")
                setTaskIdPostProcessor("UPPERCASE")
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, "FALLBACK-1")
            }
        }
    }

    companion object {
        @JvmStatic
        fun taskIdCases(): Stream<Arguments> = Stream.of(
            Arguments.of("default regex", "feat/CMT-123", null, null, "None", "CMT-123"),
            Arguments.of("inside branch", "feat/some/CMT-456/work", null, null, "None", "CMT-456"),
            Arguments.of("first match", "feat/ABC-1-DEF-2", null, null, "None", "ABC-1"),
            Arguments.of("missing without default", "main", null, null, "None", ""),
            Arguments.of("missing with default", "main", null, "CMT-000", "None", "CMT-000"),
            Arguments.of("custom regex", "feat/TEST-123", "TEST-\\d+", null, "None", "TEST-123"),
            Arguments.of("custom mismatch", "feat/CMT-123", "TEST-\\d+", "TEST-0", "None", "TEST-0"),
            Arguments.of("blank regex", "feat/CMT-123", "   ", null, "None", "CMT-123"),
            Arguments.of("none case", "feat/cmt-123", null, null, "None", "cmt-123"),
            Arguments.of("capitalize", "feat/cmt-123", null, null, "Capitalize", "Cmt-123"),
            Arguments.of("uppercase", "feat/cmt-123", null, null, "UPPERCASE", "CMT-123"),
            Arguments.of("lowercase", "feat/CMT-123", null, null, "lowercase", "cmt-123"),
            Arguments.of("default is not postprocessed", "main", null, "cmt-000", "UPPERCASE", "cmt-000")
        )
    }
}
