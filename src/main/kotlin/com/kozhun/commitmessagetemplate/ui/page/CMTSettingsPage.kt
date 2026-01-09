package com.kozhun.commitmessagetemplate.ui.page

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.options.ConfigurableWithId
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ContextHelpLabel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_SCOPE_SEPARATOR
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_TASK_ID_REGEX
import com.kozhun.commitmessagetemplate.constants.DefaultValues.DEFAULT_TYPE_REGEX
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.settings.SettingsExporter
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.ui.components.PatternEditorBuilder
import com.kozhun.commitmessagetemplate.ui.components.SynonymDialog
import com.kozhun.commitmessagetemplate.ui.model.SynonymColumnInfo
import com.kozhun.commitmessagetemplate.ui.model.SynonymPair
import com.kozhun.commitmessagetemplate.ui.util.bindNullableText
import com.kozhun.commitmessagetemplate.ui.util.showCommittleNotification
import java.util.ResourceBundle
import javax.swing.JComponent
import javax.swing.ListSelectionModel

@Suppress("TooManyFunctions")
class CMTSettingsPage(
    private val project: Project
) : ConfigurableWithId {
    private val settingsStorage = SettingsStorage.getInstance(project)
    private val settingsExporter = SettingsExporter.getInstance(project)

    private lateinit var panel: DialogPanel

    private val patternEditor = PatternEditorBuilder.buildEditor(project)
    private val resourceBundle = ResourceBundle.getBundle("messages")

    private val tableModel = ListTableModel(
        arrayOf(
            SynonymColumnInfo(resourceBundle.getString("settings.table.value")) { it.key },
            SynonymColumnInfo(resourceBundle.getString("settings.table.synonym")) { it.value }
        ),
        getSynonymsMapFromStorage()
    )

    private val table = TableView(tableModel).apply {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        putClientProperty("terminateEditOnFocusLost", true)
    }

    @Suppress("LongMethod")
    override fun createComponent(): JComponent {
        panel = panel {
            row {
                button(resourceBundle.getString("settings.import-settings")) {
                    val importState = settingsExporter.import() ?: return@button

                    settingsStorage.loadState(importState)

                    reset()
                    project.showCommittleNotification(
                        resourceBundle.getString("settings.import.success"),
                        NotificationType.INFORMATION
                    )
                }
                button(resourceBundle.getString("settings.export-settings")) {
                    settingsExporter.export()
                }.align(AlignX.RIGHT)
            }.bottomGap(BottomGap.NONE)
            row {
                cell(patternEditor.component)
                    .align(AlignX.FILL)
                    .bind(
                        { _ -> patternEditor.document.text.ifBlank { null } },
                        { _, value: String? -> runWriteAction { patternEditor.document.setText(value.orEmpty()) } },
                        MutableProperty({ settingsStorage.state.pattern }, { newValue -> runWriteAction { settingsStorage.state.pattern = newValue } })
                    )
            }
            row {
                checkBox(resourceBundle.getString("settings.trim-whitespaces-start"))
                    .bindSelected(settingsStorage.state::trimWhitespacesStart)
                checkBox(resourceBundle.getString("settings.trim-whitespaces-end"))
                    .bindSelected(settingsStorage.state::trimWhitespacesEnd)
                checkBox(resourceBundle.getString("settings.duplicated-whitespaces"))
                    .bindSelected(settingsStorage.state::unnecessaryWhitespaces)
                val patternHelp = listOf(
                    resourceBundle.getString("settings.message-pattern-notes.task-id"),
                    resourceBundle.getString("settings.message-pattern-notes.type"),
                    resourceBundle.getString("settings.message-pattern-notes.scope"),
                    resourceBundle.getString("settings.message-pattern-notes.caret-position")
                ).joinToString(separator = "<br/>")
                cell(ContextHelpLabel.create(patternHelp, "Help")).align(AlignX.RIGHT)
            }
            group(resourceBundle.getString("settings.variables-config"), false) {
                collapsibleGroup(resourceBundle.getString("settings.advanced.task-id.title")) {
                    row {
                        expandableTextField()
                            .label(resourceBundle.getString("settings.advanced.common.label"), LabelPosition.TOP)
                            .comment(comment = resourceBundle.getString("settings.advanced.task-id.regex.comment").format(DEFAULT_TASK_ID_REGEX))
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .bindNullableText(settingsStorage.state::taskIdRegex)
                        cell(ContextHelpLabel.create(resourceBundle.getString("settings.help.current-branch"))).align(AlignX.RIGHT)
                    }
                    row {
                        textField()
                            .label(resourceBundle.getString("settings.advanced.task-id.default.value"), LabelPosition.TOP)
                            .comment(resourceBundle.getString("settings.advanced.task-id.default.comment"))
                            .align(AlignX.FILL)
                            .bindNullableText(settingsStorage.state::taskIdDefault)
                        comboBox(StringCase.values().map { it.label })
                            .label(resourceBundle.getString("settings.advanced.common.postprocess"), LabelPosition.TOP)
                            .bindItem(settingsStorage.state::taskIdPostProcessor)
                    }
                }.apply {
                    expanded = !settingsStorage.state.isDefaultTaskFields()
                }.withoutGaps()
                collapsibleGroup(resourceBundle.getString("settings.advanced.type.title")) {
                    row {
                        expandableTextField()
                            .label(resourceBundle.getString("settings.advanced.common.label"), LabelPosition.TOP)
                            .comment(comment = resourceBundle.getString("settings.advanced.type.regex.comment").format(DEFAULT_TYPE_REGEX))
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .bindNullableText(settingsStorage.state::typeRegex)
                        cell(ContextHelpLabel.create(resourceBundle.getString("settings.help.current-branch"))).align(AlignX.RIGHT)
                    }
                    row {
                        textField()
                            .label(resourceBundle.getString("settings.advanced.type.default-type.value"), LabelPosition.TOP)
                            .comment(resourceBundle.getString("settings.advanced.type.default-type.comment"))
                            .align(AlignX.FILL)
                            .bindNullableText(settingsStorage.state::typeDefault)
                        comboBox(StringCase.values().map { it.label }, null)
                            .label(resourceBundle.getString("settings.advanced.common.postprocess"), LabelPosition.TOP)
                            .bindItem(settingsStorage.state::typePostprocessor)
                    }
                    group(resourceBundle.getString("settings.advanced.type.synonyms"), indent = false) {
                        row {
                            cell(createSynonymTablePanel()).align(AlignX.FILL)
                        }.resizableRow()
                    }.withoutGaps()
                }.apply {
                    expanded = !settingsStorage.state.isDefaultTypeFields() || settingsStorage.state.typeSynonyms.isNotEmpty()
                }.withoutGaps()
                collapsibleGroup(resourceBundle.getString("settings.advanced.scope.title")) {
                    row {
                        expandableTextField()
                            .label(resourceBundle.getString("settings.advanced.common.label"), LabelPosition.TOP)
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .bindNullableText(settingsStorage.state::scopeRegex)
                        cell(ContextHelpLabel.create(resourceBundle.getString("settings.help.file-path"))).align(AlignX.RIGHT)
                    }
                    row {
                        textField()
                            .label(resourceBundle.getString("settings.advanced.scope.default-value"), LabelPosition.TOP)
                            .comment(resourceBundle.getString("settings.advanced.scope.default.comment"))
                            .align(AlignX.FILL)
                            .bindNullableText(settingsStorage.state::scopeDefault)
                        textField()
                            .label(resourceBundle.getString("settings.advanced.common.separator"), LabelPosition.TOP)
                            .comment(comment = resourceBundle.getString("settings.advanced.scope.separator.comment").format(DEFAULT_SCOPE_SEPARATOR))
                            .align(AlignX.FILL)
                            .bindNullableText(settingsStorage.state::scopeSeparator)
                        comboBox(StringCase.values().map { it.label })
                            .label(resourceBundle.getString("settings.advanced.common.postprocess"), LabelPosition.TOP)
                            .bindItem(settingsStorage.state::scopePostprocessor)
                    }
                }.apply {
                    expanded = !settingsStorage.state.isDefaultScopeFields()
                }.withoutGaps()
            }
        }

        return panel
    }

    override fun isModified(): Boolean {
        return panel.isModified() || settingsStorage.state.typeSynonyms != getSynonymsMapFromTable()
    }

    override fun apply() {
        panel.apply()
        settingsStorage.state.typeSynonyms = getSynonymsMapFromTable().toMutableMap()
    }

    override fun reset() {
        panel.reset()
        tableModel.apply {
            items = getSynonymsMapFromStorage()
            fireTableDataChanged()
        }
    }

    private fun getSynonymsMapFromStorage() = settingsStorage.state.typeSynonyms
        .map { SynonymPair(it.key, it.value) }
        .toMutableList()

    private fun getSynonymsMapFromTable(): Map<String, String> {
        return tableModel.items
            .filter { it.key.isNotBlank() && it.value.isNotBlank() }
            .associate { it.key to it.value }
    }

    override fun getDisplayName(): String {
        return ResourceBundle.getBundle("messages").getString("settings.display-name")
    }

    override fun getId(): String {
        return ID
    }

    companion object {
        private const val ID = "preferences.CommittleConfigurable"
    }

    private fun createSynonymTablePanel(): JComponent {
        val tablePanel = ToolbarDecorator.createDecorator(table)
            .setAddAction { addSynonym() }
            .setEditAction { editSelectedSynonym() }
            .setRemoveAction { removeSelectedSynonym() }
            .disableUpAction()
            .disableDownAction()
            .createPanel()

        return tablePanel
    }

    private fun addSynonym() {
        val dialog = SynonymDialog(getSynonymsMapFromTable().keys)
        if (dialog.showAndGet()) {
            val newPair = SynonymPair(dialog.value, dialog.synonym)
            tableModel.addRow(newPair)
            tableModel.fireTableDataChanged()
        }
    }

    private fun editSelectedSynonym() {
        val selectedRow = table.selectedObject
        if (selectedRow != null) {
            val dialog = SynonymDialog(getSynonymsMapFromTable().keys, selectedRow)
            if (dialog.showAndGet()) {
                selectedRow.key = dialog.value
                selectedRow.value = dialog.synonym
                tableModel.fireTableDataChanged()
            }
        }
    }

    private fun removeSelectedSynonym() {
        val selectedRow = table.selectedRow
        if (selectedRow >= 0) {
            tableModel.removeRow(selectedRow)
        }
    }
}

private fun Row.withoutGaps() = apply {
    topGap(TopGap.NONE)
    bottomGap(BottomGap.NONE)
}
