package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.sdk.ui.components.common.ideFrame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.appendText
import kotlin.io.path.writeText

class ScopeGenerationTest : CommittleIdeTest() {
    @ParameterizedTest(name = "{0}")
    @MethodSource("scopeCases")
    fun `scope extraction selection separators and postprocessing`(
        caseName: String,
        regex: String?,
        defaultValue: String?,
        separator: String?,
        postProcessor: String,
        selectedFiles: List<String>,
        expectedMessages: Set<String>,
        @TempDir tempDir: Path
    ) {
        runIde("scope-${caseName.replace(" ", "-")}", createGitProject(tempDir)) {
            settings {
                setPattern("${'$'}SCOPE${'$'}CARET_POSITION")
                setScopeRegex(regex)
                setScopeDefault(defaultValue)
                setScopeSeparator(separator)
                setScopePostprocessor(postProcessor)
            }
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                page.selectOnly(*selectedFiles.toTypedArray())
                generateAndAssertAny(this, expectedMessages, expectedMessages.first().length)
            }
        }
    }

    @Test
    fun `scope uses path of an added file`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, dirtyFiles = false)
        project.resolve("ui/NewComponent.kt").writeText("class NewComponent\n")
        assertFileChangeScope(project, "scope-added", "ui")
    }

    @Test
    fun `scope uses before revision of a deleted file`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, dirtyFiles = false)
        git(project, "rm", "ui/UIButton.kt")
        assertFileChangeScope(project, "scope-deleted", "ui")
    }

    @Test
    fun `scope uses new path of a renamed file`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, dirtyFiles = false)
        git(project, "mv", "api/ApiClient.kt", "ui/ApiClient.kt")
        assertFileChangeScope(project, "scope-renamed", "ui")
    }

    @Test
    fun `duplicate scopes are emitted once`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, dirtyFiles = false) {
            it.resolve("ui/Panel.kt").writeText("class Panel\n")
        }
        project.resolve("ui/UIButton.kt").appendText("\n// dirty\n")
        project.resolve("ui/Panel.kt").appendText("\n// dirty\n")
        runIde("scope-duplicates", project) {
            settings {
                setPattern("${'$'}SCOPE")
                setScopeRegex("ui")
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, "ui")
            }
        }
    }

    @Test
    fun `empty changes use default scope`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, dirtyFiles = false)
        runIde("scope-no-changes", project) {
            settings {
                setPattern("${'$'}SCOPE")
                setScopeRegex("ui")
                setScopeDefault("core")
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, "core")
            }
        }
    }

    @Test
    fun `lowercase postprocessing is applied to extracted scope`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, dirtyFiles = false) {
            it.resolve("UI/Widget.kt").writeText("class Widget\n")
        }
        project.resolve("UI/Widget.kt").appendText("\n// dirty\n")
        runIde("scope-lowercase", project) {
            settings {
                setPattern("${'$'}SCOPE")
                setScopeRegex("UI")
                setScopePostprocessor("lowercase")
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, "ui")
            }
        }
    }

    @Test
    fun `selected change determines branch and scope in a nested repository`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir, branch = "main/ROOT-1", dirtyFiles = false)
        val nested = project.resolve("nested")
        nested.resolve("Nested.kt").writeText("class Nested\n")
        git(nested, "init", "-b", "feature/NEST-1")
        git(nested, "config", "user.name", "Committle E2E")
        git(nested, "config", "user.email", "committle-e2e@example.com")
        git(nested, "add", ".")
        git(nested, "commit", "-m", "test: initial nested fixture")
        nested.resolve("Nested.kt").appendText("\n// dirty\n")

        runIde("scope-nested-repository", project) {
            settings {
                setPattern("${'$'}TYPE ${'$'}TASK_ID ${'$'}SCOPE")
                setScopeRegex("(?<=/)nested(?=/)")
            }
            ideFrame {
                val page = CommittleCommitPage(this)
                page.open()
                page.selectOnly("Nested.kt")
                generateAndAssert(this, "feature NEST-1 nested")
            }
        }
    }

    private fun assertFileChangeScope(project: Path, testName: String, expected: String) {
        runIde(testName, project) {
            settings {
                setPattern("${'$'}SCOPE")
                setScopeRegex("(?<=/)(ui|api)(?=/)")
            }
            ideFrame {
                CommittleCommitPage(this).open()
                generateAndAssert(this, expected)
            }
        }
    }

    companion object {
        @JvmStatic
        fun scopeCases(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "single selected", "(?<=/)(ui|api)(?=/)", null, null, "None",
                listOf("UIButton.kt"), setOf("ui")
            ),
            Arguments.of(
                "all affected", "(?<=/)(ui|api)(?=/)", null, null, "None",
                listOf("UIButton.kt", "ApiClient.kt"), setOf("ui|api", "api|ui")
            ),
            Arguments.of(
                "custom separator", "(?<=/)(ui|api)(?=/)", null, ", ", "None",
                listOf("UIButton.kt", "ApiClient.kt"), setOf("ui, api", "api, ui")
            ),
            Arguments.of(
                "blank separator", "(?<=/)(ui|api)(?=/)", null, " ", "None",
                listOf("UIButton.kt", "ApiClient.kt"), setOf("ui|api", "api|ui")
            ),
            Arguments.of(
                "selected subset", "(?<=/)(ui|api)(?=/)", null, null, "None",
                listOf("ApiClient.kt"), setOf("api")
            ),
            Arguments.of(
                "no match default", "missing", "core", null, "None",
                listOf("UIButton.kt", "ApiClient.kt"), setOf("core")
            ),
            Arguments.of(
                "no match empty", "missing", null, null, "None",
                listOf("UIButton.kt", "ApiClient.kt"), setOf("")
            ),
            Arguments.of(
                "no regex default", null, "core", null, "None",
                listOf("UIButton.kt", "ApiClient.kt"), setOf("core")
            ),
            Arguments.of(
                "uppercase", "(?<=/)(ui|api)(?=/)", null, null, "UPPERCASE",
                listOf("UIButton.kt"), setOf("UI")
            ),
            Arguments.of(
                "capitalize", "(?<=/)(ui|api)(?=/)", null, null, "Capitalize",
                listOf("UIButton.kt", "ApiClient.kt"), setOf("Ui|api", "Api|ui")
            ),
            Arguments.of(
                "default not postprocessed", "missing", "core", null, "UPPERCASE",
                listOf("UIButton.kt"), setOf("core")
            )
        )
    }
}
