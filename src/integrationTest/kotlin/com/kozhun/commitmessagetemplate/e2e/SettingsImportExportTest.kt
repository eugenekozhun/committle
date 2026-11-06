package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.client.service
import com.intellij.driver.sdk.getNotifications
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.ui.components.common.ideFrame
import com.intellij.driver.sdk.waitFor
import com.kozhun.commitmessagetemplate.service.settings.ExportableSettings
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds

class SettingsImportExportTest : CommittleIdeTest() {
    @Test
    fun `export contains every persisted setting`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        val exportFile = tempDir.resolve("all-settings.json")
        runIde("settings-export-all", project) {
            val expected = completeSettings()
            settings { copyFrom(expected) }
            withContext {
                service(SettingsExporterRef::class, singleProject()).exportTo(exportFile.toFile())
            }

            val actual = Json.decodeFromString<ExportableSettings>(exportFile.readText())
            assertEquals(expected, actual)
            waitFor("export notification", timeout = 15.seconds) {
                getNotifications().any { it.getGroupId() == "Committle Notifications" && "exported successfully" in it.getContent() }
            }
        }
    }

    @Test
    fun `export uses persisted state rather than unapplied draft`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        val exportFile = tempDir.resolve("persisted.json")
        runIde("settings-export-draft", project) {
            settings { setPattern("persisted") }
            ideFrame {
                openCommittleSettings().apply {
                    pattern = "draft"
                    withContext {
                        service(SettingsExporterRef::class, singleProject()).exportTo(exportFile.toFile())
                    }
                    cancel()
                }
            }
            assertEquals("persisted", Json.decodeFromString<ExportableSettings>(exportFile.readText()).pattern)
        }
    }

    @Test
    fun `full import export round trip restores all values`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        val exportFile = tempDir.resolve("round-trip.json")
        runIde("settings-round-trip", project) {
            val expected = completeSettings()
            settings { copyFrom(expected) }
            withContext {
                service(SettingsExporterRef::class, singleProject()).exportTo(exportFile.toFile())
            }
            settings {
                setPattern("changed")
                setTypeSynonyms(emptyMap())
                setScopeDefault("changed")
            }
            val imported = withContext {
                service(SettingsExporterRef::class, singleProject()).importFrom(exportFile.toFile())
            }
            assertNotNull(imported)
            settings { copyFrom(requireNotNull(imported).toExportable()) }
            assertEquals(expected, settingsState().toExportable())
        }
    }

    @Test
    fun `partial json uses defaults and unicode survives import`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        val importFile = tempDir.resolve("partial.json")
        importFile.writeText("""{"pattern":"Исправление ✨\n${'$'}TYPE"}""")
        runIde("settings-import-partial", project) {
            val imported = withContext {
                service(SettingsExporterRef::class, singleProject()).importFrom(importFile.toFile())
            }
            assertNotNull(imported)
            assertEquals("Исправление ✨\n${'$'}TYPE", imported?.getPattern())
            assertEquals(false, imported?.getTrimWhitespacesStart())
            assertEquals(emptyMap<String, String>(), imported?.getTypeSynonyms())
            assertEquals("None", imported?.getTypePostprocessor())
        }
    }

    @Test
    fun `malformed empty and wrong schema files report errors without state changes`(@TempDir tempDir: Path) {
        val project = createGitProject(tempDir)
        runIde("settings-import-errors", project) {
            settings { setPattern("unchanged") }
            listOf("malformed" to "{", "empty" to "", "wrong-schema" to "[]").forEach { (name, content) ->
                val file = tempDir.resolve("$name.json")
                file.writeText(content)
                val result = withContext {
                    service(SettingsExporterRef::class, singleProject()).importFrom(file.toFile())
                }
                assertNull(result)
                assertEquals("unchanged", settingsState().getPattern())
            }
            waitFor("import error notifications", timeout = 15.seconds) {
                getNotifications().count { it.getGroupId() == "Committle Notifications" && "Failed to import" in it.getContent() } >= 3
            }
        }
    }

    private fun completeSettings() = ExportableSettings(
        pattern = "${'$'}TYPE(${'$'}SCOPE)${'$'}BREAKING_CHANGE: ${'$'}TASK_ID ${'$'}CARET_POSITION\nИсправление ✨",
        trimWhitespacesStart = true,
        trimWhitespacesEnd = true,
        unnecessaryWhitespaces = true,
        taskIdRegex = "TASK-\\d+",
        taskIdDefault = "TASK-0",
        taskIdPostProcessor = "UPPERCASE",
        typeRegex = "feature|bug",
        typeDefault = "feature",
        typeSynonyms = mapOf("feature" to "feat", "bug" to "fix"),
        typePostprocessor = "lowercase",
        scopeRegex = "(?<=/)(ui|api)(?=/)",
        scopeDefault = "core",
        scopeSeparator = ",",
        scopePostprocessor = "Capitalize"
    )

    private fun SettingsStateRef.copyFrom(source: ExportableSettings) {
        setPattern(source.pattern)
        setTrimWhitespacesStart(source.trimWhitespacesStart)
        setTrimWhitespacesEnd(source.trimWhitespacesEnd)
        setUnnecessaryWhitespaces(source.unnecessaryWhitespaces)
        setTaskIdRegex(source.taskIdRegex)
        setTaskIdDefault(source.taskIdDefault)
        setTaskIdPostProcessor(source.taskIdPostProcessor)
        setTypeRegex(source.typeRegex)
        setTypeDefault(source.typeDefault)
        setTypeSynonyms(source.typeSynonyms)
        setTypePostprocessor(source.typePostprocessor)
        setScopeRegex(source.scopeRegex)
        setScopeDefault(source.scopeDefault)
        setScopeSeparator(source.scopeSeparator)
        setScopePostprocessor(source.scopePostprocessor)
    }

    private fun SettingsStateRef.toExportable() = ExportableSettings(
        pattern = getPattern().orEmpty(),
        trimWhitespacesStart = getTrimWhitespacesStart(),
        trimWhitespacesEnd = getTrimWhitespacesEnd(),
        unnecessaryWhitespaces = getUnnecessaryWhitespaces(),
        taskIdRegex = getTaskIdRegex(),
        taskIdDefault = getTaskIdDefault(),
        taskIdPostProcessor = getTaskIdPostProcessor(),
        typeRegex = getTypeRegex(),
        typeDefault = getTypeDefault(),
        typeSynonyms = getTypeSynonyms(),
        typePostprocessor = getTypePostprocessor(),
        scopeRegex = getScopeRegex(),
        scopeDefault = getScopeDefault(),
        scopeSeparator = getScopeSeparator(),
        scopePostprocessor = getScopePostprocessor()
    )

}
