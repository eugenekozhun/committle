package com.kozhun.commitmessagetemplate.service.settings

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.storage.SettingsState
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.storage.toExportableSettings
import com.kozhun.commitmessagetemplate.ui.util.showCommittleNotification
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.awt.FileDialog
import java.awt.Frame
import java.io.FilenameFilter

@Service(Service.Level.PROJECT)
class SettingsExporter(
    private val project: Project
) {
    private val settingsStorage = SettingsStorage.getInstance(project)

    init {
        if (System.getProperty("os.name").contains("Mac", ignoreCase = true)) {
            System.setProperty("apple.awt.fileDialogForDirectories", "false")
        }
    }

    fun export() {
        val fileDialog = FileDialog(null as Frame?, "Select Export Location", FileDialog.SAVE).apply {
            file = "committle-config.json"
            isVisible = true
        }

        val selectedFile = fileDialog.files.firstOrNull() ?: return
        val exportableSettings = settingsStorage.state.toExportableSettings()

        try {
            val json = Json.encodeToString(exportableSettings)
            selectedFile.writeText(json)
            project.showCommittleNotification("Settings exported successfully to ${selectedFile.absolutePath}.", NotificationType.INFORMATION)
        } catch (exception: IllegalArgumentException) {
            project.showCommittleNotification("Failed to export settings: ${exception.message}", NotificationType.ERROR)
        }
    }

    fun import(): SettingsState? {
        val fileDialog = FileDialog(null as Frame?, "Select Import File", FileDialog.LOAD).apply {
            filenameFilter = FilenameFilter { _, name -> name.endsWith(".json") }
            isVisible = true
        }

        val selectedFile = fileDialog.files.firstOrNull() ?: return null
        val importableSettings: ExportableSettings = try {
            Json.decodeFromString(selectedFile.readText())
        } catch (exception: IllegalArgumentException) {
            project.showCommittleNotification("Failed to import settings: ${exception.message}", NotificationType.ERROR)
            return null
        }

        return importableSettings.toSettingsState()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): SettingsExporter = project.service<SettingsExporter>()
    }
}
