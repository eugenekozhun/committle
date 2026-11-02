package com.kozhun.commitmessagetemplate.service.settings

import com.kozhun.commitmessagetemplate.storage.SettingsState
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.storage.copyFrom
import com.kozhun.commitmessagetemplate.storage.toExportableSettings

class SettingsDraft(
    private val settingsStorage: SettingsStorage
) {
    val state: SettingsState = settingsStorage.state.toExportableSettings().toSettingsState()

    fun loadFrom(importedState: SettingsState) {
        state.copyFrom(importedState)
    }

    fun reset() {
        state.copyFrom(settingsStorage.state)
    }

    fun apply() {
        settingsStorage.loadState(state.toExportableSettings().toSettingsState())
    }

    fun isModified(): Boolean {
        return state.toExportableSettings() != settingsStorage.state.toExportableSettings()
    }
}
