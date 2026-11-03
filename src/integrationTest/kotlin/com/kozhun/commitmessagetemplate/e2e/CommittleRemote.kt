package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.client.Remote

@Remote("com.kozhun.commitmessagetemplate.storage.SettingsStorage", plugin = "com.kozhun.commit-message-template")
interface SettingsStorageRef {
    fun getState(): SettingsStateRef
}

@Remote("com.kozhun.commitmessagetemplate.storage.SettingsState", plugin = "com.kozhun.commit-message-template")
interface SettingsStateRef {
    fun getPattern(): String?
}

@Remote("javax.swing.JButton")
interface JButtonRef {
    fun doClick()
}
