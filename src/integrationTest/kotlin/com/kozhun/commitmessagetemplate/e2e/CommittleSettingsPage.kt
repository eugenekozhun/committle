package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.common.IdeaFrameUI
import com.intellij.driver.sdk.ui.components.common.JEditorUiComponent
import com.intellij.driver.sdk.ui.components.settings.SettingsDialogUiComponent
import com.intellij.driver.sdk.ui.components.settings.settingsDialog
import com.intellij.driver.sdk.ui.present
import com.intellij.driver.sdk.ui.shouldBe
import com.intellij.driver.sdk.ui.ui

class CommittleSettingsPage(private val dialog: SettingsDialogUiComponent) {
    private val patternEditor = dialog.driver.ui.x(
        "//div[@accessiblename='Editor' and @class='EditorComponentImpl']",
        JEditorUiComponent::class.java
    )

    init {
        dialog.searchTextField.text = "Committle"
        dialog.settingsTree.shouldBe("Committle settings category is not found") {
            pathExists("Version Control", "Committle")
        }
        patternEditor.shouldBe("Committle pattern editor is not visible", present)
    }

    var pattern: String
        get() = patternEditor.text
        set(value) {
            patternEditor.text = value
        }

    fun apply() {
        dialog.applyButton.doClick()
    }

    fun close() {
        dialog.okButton.doClick()
    }
}

private fun UiComponent.doClick() {
    driver.withContext(OnDispatcher.EDT) {
        driver.cast(component, JButtonRef::class).doClick()
    }
}

fun IdeaFrameUI.openCommittleSettings(): CommittleSettingsPage {
    openSettingsDialog()
    return CommittleSettingsPage(settingsDialog())
}
