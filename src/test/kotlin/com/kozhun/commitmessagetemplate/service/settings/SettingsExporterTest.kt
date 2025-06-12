package com.kozhun.commitmessagetemplate.service.settings

import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.storage.SettingsState
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.storage.toExportableSettings
import com.kozhun.commitmessagetemplate.ui.util.showCommittleNotification
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.FileDialog

class SettingsExporterTest {
    private val projectMock = mockk<Project>()

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `export writes settings to file`() {
        val settingsState = SettingsState().apply {
            pattern = "[%TYPE%] %TASK%"
            trimWhitespacesStart = true
            trimWhitespacesEnd = true
            unnecessaryWhitespaces = true
            taskIdRegex = "TASK-(\\d+)"
            taskIdDefault = "0"
            taskIdPostProcessor = "lowercase"
            typeRegex = "feat|bug"
            typeDefault = "feat"
            typePostprocessor = "UPPERCASE"
            typeSynonyms = mutableMapOf("f" to "feat")
            scopeRegex = "src/(\\w+)"
            scopeDefault = "src"
            scopeSeparator = ","
            scopePostprocessor = "Capitalize"
        }
        mockkStatic(SettingsStorage::class)
        val settingsStorageMock = mockk<SettingsStorage>()
        every { settingsStorageMock.state } returns settingsState
        every { SettingsStorage.getInstance(projectMock) } returns settingsStorageMock
        every { projectMock.getService(SettingsStorage::class.java) } returns settingsStorageMock
        mockkStatic("com.kozhun.commitmessagetemplate.ui.util.ProjectExtentionsKt")
        every { projectMock.showCommittleNotification(any(), any()) } returns Unit

        val tempFile = kotlin.io.path.createTempFile().toFile()
        mockkConstructor(FileDialog::class)
        every { anyConstructed<FileDialog>().files } returns arrayOf(tempFile)
        every { anyConstructed<FileDialog>().isVisible = any() } returns Unit
        every { anyConstructed<FileDialog>().file = any() } returns Unit

        SettingsExporter(projectMock).export()

        val expectedJson = Json.encodeToString(settingsState.toExportableSettings())
        assertEquals(expectedJson, tempFile.readText())
    }

    @Test
    fun `import reads settings from file`() {
        val exportable = ExportableSettings(
            pattern = "[%TYPE%] %TASK%",
            trimWhitespacesStart = true,
            trimWhitespacesEnd = true,
            unnecessaryWhitespaces = true,
            taskIdRegex = "TASK-(\\d+)",
            taskIdDefault = "0",
            taskIdPostProcessor = "lowercase",
            typeRegex = "feat|bug",
            typeDefault = "feat",
            typeSynonyms = mapOf("f" to "feat"),
            typePostprocessor = "UPPERCASE",
            scopeRegex = "src/(\\w+)",
            scopeDefault = "src",
            scopeSeparator = ",",
            scopePostprocessor = "Capitalize"
        )
        val tempFile = kotlin.io.path.createTempFile().toFile()
        tempFile.writeText(Json.encodeToString(exportable))

        mockkStatic(SettingsStorage::class)
        val settingsStorageMock = mockk<SettingsStorage>()
        every { SettingsStorage.getInstance(projectMock) } returns settingsStorageMock
        every { projectMock.getService(SettingsStorage::class.java) } returns settingsStorageMock

        mockkStatic("com.kozhun.commitmessagetemplate.ui.util.ProjectExtentionsKt")
        every { projectMock.showCommittleNotification(any(), any()) } returns Unit
        mockkConstructor(FileDialog::class)
        every { anyConstructed<FileDialog>().files } returns arrayOf(tempFile)
        every { anyConstructed<FileDialog>().isVisible = any() } returns Unit
        every { anyConstructed<FileDialog>().filenameFilter = any() } returns Unit

        val result = SettingsExporter(projectMock).import()
        val restored = result!!.toExportableSettings()
        assertEquals(exportable, restored)
    }
}
