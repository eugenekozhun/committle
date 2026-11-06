package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.model.OnDispatcher
import com.intellij.driver.sdk.invokeAction
import com.intellij.driver.sdk.ui.components.UiComponent
import com.intellij.driver.sdk.ui.components.common.IdeaFrameUI
import com.intellij.driver.sdk.ui.components.common.JEditorUiComponent
import com.intellij.driver.sdk.ui.components.elements.JComboBoxUiComponent
import com.intellij.driver.sdk.ui.components.elements.JTableUiComponent
import com.intellij.driver.sdk.ui.components.elements.JTextFieldUI
import com.intellij.driver.sdk.ui.components.elements.checkBoxWithName
import com.intellij.driver.sdk.ui.components.elements.dialog
import com.intellij.driver.sdk.ui.components.elements.table
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

    var trimStart: Boolean
        get() = dialog.checkBoxWithName("Trim whitespaces start").isSelected()
        set(value) {
            dialog.checkBoxWithName("Trim whitespaces start").apply { if (value) check() else uncheck() }
        }

    var trimEnd: Boolean
        get() = dialog.checkBoxWithName("Trim whitespaces end").isSelected()
        set(value) {
            dialog.checkBoxWithName("Trim whitespaces end").apply { if (value) check() else uncheck() }
        }

    var consolidateWhitespaces: Boolean
        get() = dialog.checkBoxWithName("Remove duplicated whitespaces").isSelected()
        set(value) {
            dialog.checkBoxWithName("Remove duplicated whitespaces").apply { if (value) check() else uncheck() }
        }

    fun expandAdvancedGroups() {
        if (textFields().size < 2) clickGroup("${'$'}TASK_ID")
        if (textFields().size < 4) clickGroup("${'$'}TYPE")
        if (textFields().size < 7) clickGroup("${'$'}SCOPE")
    }

    fun taskRegex(): JTextFieldUI = textFields()[0]
    fun taskDefault(): JTextFieldUI = textFields()[1]
    fun typeRegex(): JTextFieldUI = textFields()[2]
    fun typeDefault(): JTextFieldUI = textFields()[3]
    fun scopeRegex(): JTextFieldUI = textFields()[4]
    fun scopeDefault(): JTextFieldUI = textFields()[5]
    fun scopeSeparator(): JTextFieldUI = textFields()[6]

    fun taskCase(): JComboBoxUiComponent = comboBoxes()[0]
    fun typeCase(): JComboBoxUiComponent = comboBoxes()[1]
    fun scopeCase(): JComboBoxUiComponent = comboBoxes()[2]

    fun synonymsTable(): JTableUiComponent = dialog.content().table()

    fun addSynonym(value: String, synonym: String, confirm: Boolean = true) {
        expandAdvancedGroups()
        dialog.content().x("//div[@accessiblename='Add']").click()
        editSynonymDialog("Add Synonym", value, synonym, confirm)
    }

    fun editSynonym(row: Int, value: String, synonym: String, confirm: Boolean = true) {
        synonymsTable().clickCell(row, 0)
        dialog.content().x("//div[@accessiblename='Edit']").click()
        editSynonymDialog("Edit Synonym", value, synonym, confirm)
    }

    fun removeSynonym(row: Int) {
        synonymsTable().clickCell(row, 0)
        dialog.content().x("//div[@accessiblename='Remove']").click()
    }

    fun cancelSynonymDialog(title: String = "Add Synonym") {
        dialog.driver.ui.dialog(title = title).cancelButton.doClick()
    }

    fun invokePatternCompletion() {
        patternEditor.click()
        dialog.driver.invokeAction("CodeCompletion", component = patternEditor.component)
    }

    fun apply() {
        dialog.applyButton.doClick()
    }

    fun close() {
        dialog.okButton.doClick()
    }

    fun cancel() {
        dialog.cancelButton.doClick()
    }

    fun reset() {
        dialog.pressButton("Reset")
    }

    private fun editSynonymDialog(title: String, value: String, synonym: String, confirm: Boolean) {
        val synonymDialog = dialog.driver.ui.dialog(title = title)
        val fields = synonymDialog.xx("//div[@class='JTextField']", JTextFieldUI::class.java).list()
        fields[0].text = value
        fields[1].text = synonym
        if (confirm) synonymDialog.okButton.doClick() else synonymDialog.cancelButton.doClick()
    }

    private fun clickGroup(title: String) {
        dialog.content().x("//div[@visible_text='$title']").click()
    }

    private fun textFields(): List<JTextFieldUI> = dialog.content()
        .xx("//div[@class='JTextField']", JTextFieldUI::class.java)
        .list()

    private fun comboBoxes(): List<JComboBoxUiComponent> = dialog.content()
        .xx("//div[@class='JComboBox']", JComboBoxUiComponent::class.java)
        .list()
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
