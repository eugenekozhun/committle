package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.client.Driver
import com.intellij.driver.client.service
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitForIndicators
import com.intellij.ide.starter.ci.CIServer
import com.intellij.ide.starter.ci.NoCIServer
import com.intellij.ide.starter.di.di
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.ide.installer.ExistingIdeInstaller
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.project.ProjectInfoSpec
import com.intellij.ide.starter.runner.Starter
import com.intellij.platform.testFramework.teamCity.TeamCityReporter
import org.junit.jupiter.api.fail
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes

abstract class CommittleIdeTest {
    init {
        di = DI {
            extend(di)
            bindSingleton<CIServer>(overrides = true) {
                object : CIServer by NoCIServer {
                    override fun reportTestFailure(
                        testName: String,
                        message: String,
                        details: String,
                        linkToLogs: String?,
                        kind: TeamCityReporter.SyntheticTestKind,
                        generifyTestName: Boolean
                    ) {
                        fail { "$testName fails: $message.\n$details" }
                    }
                }
            }
        }
    }

    protected fun runIde(testName: String, projectDir: Path? = null, test: Driver.() -> Unit) {
        val ideInfo = IdeInfo(
            productCode = "IU",
            platformPrefix = "idea",
            executableFileName = "idea",
            fullName = "IntelliJ IDEA Ultimate"
        ).let { info ->
            System.getProperty("e2e.ide.path")
                ?.let { path -> info.copy(getInstaller = { ExistingIdeInstaller(Path(path)) }) }
                ?: info
        }
        val projectInfo: ProjectInfoSpec = projectDir?.let(::LocalProjectInfo) ?: NoProject
        val testCase = TestCase(ideInfo, projectInfo).let { case ->
            if (System.getProperty("e2e.ide.path") == null) {
                case.withVersion(System.getProperty("e2e.ide.version"))
            } else {
                case
            }
        }

        Starter.newContext(testName, testCase).apply {
            val pathToPlugin = requireNotNull(System.getProperty("path.to.build.plugin"))
            PluginConfigurator(this).installPluginFromPath(Path(pathToPlugin))
            applyVMOptionsPatch {
                addSystemProperty("idea.trust.all.projects", true)
                addSystemProperty("ide.show.tips.on.startup.default.value", false)
                addSystemProperty("expose.ui.hierarchy.url", true)
            }
            disableAIAssistantToolwindowActivationOnStart()
            disableGotItTooltips()
            removeMigrateConfigAndCreateStubFile()
        }.runIdeWithDriver().useDriverAndCloseIde {
            if (projectDir != null) {
                waitForIndicators(2.minutes)
            }
            test()
        }
    }

    protected fun Driver.settings(configure: SettingsStateRef.() -> Unit) {
        withContext {
            service(SettingsStorageRef::class, singleProject()).getState().configure()
        }
    }

    protected fun Driver.settingsState(): SettingsStateRef = withContext {
        service(SettingsStorageRef::class, singleProject()).getState()
    }

    protected fun createGitProject(
        parent: Path,
        branch: String = "feat/CMT-123",
        dirtyFiles: Boolean = true,
        configure: (Path) -> Unit = {}
    ): Path {
        val source = Path.of(requireNotNull(javaClass.getResource("/git-project")).toURI())
        val projectDir = parent.resolve("git-project")
        source.toFile().copyRecursively(projectDir.toFile())
        configure(projectDir)

        git(projectDir, "init", "-b", branch)
        git(projectDir, "config", "user.name", "Committle E2E")
        git(projectDir, "config", "user.email", "committle-e2e@example.com")
        git(projectDir, "add", ".")
        git(projectDir, "commit", "-m", "test: initial fixture")
        if (dirtyFiles) {
            projectDir.resolve("ui/UIButton.kt").appendText("\n// Updated for E2E\n")
            projectDir.resolve("api/ApiClient.kt").appendText("\n// Updated for E2E\n")
        }
        return projectDir
    }

    protected fun replacePattern(projectDir: Path, pattern: String) {
        val config = projectDir.resolve(".idea/commit-message-template.xml")
        val escaped = pattern
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace("\"", "&quot;")
            .replace("\r", "&#13;")
            .replace("\n", "&#10;")
        val updated = config.readText().replace(
            Regex("(<option name=\"pattern\" value=\")[^\"]*(\"\\s*/>)")
        ) { match -> "${match.groupValues[1]}$escaped${match.groupValues[2]}" }
        config.writeText(updated)
    }

    protected fun git(projectDir: Path, vararg arguments: String): String {
        val process = ProcessBuilder("git", *arguments)
            .directory(projectDir.toFile())
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor(30, TimeUnit.SECONDS)) { "Git command timed out: ${arguments.joinToString(" ")}" }
        check(process.exitValue() == 0) { "Git command failed: ${arguments.joinToString(" ")}\n$output" }
        return output.trim()
    }
}
