package com.kozhun.commitmessagetemplate.ui.page

import SynonymDialog
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
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

    private lateinit var patternEditor: Editor
    private lateinit var panel: DialogPanel

    private lateinit var tableModel: ListTableModel<SynonymPair>
    private lateinit var table: TableView<SynonymPair>

    @Suppress("LongMethod")
    override fun createComponent(): JComponent {
        patternEditor = PatternEditorBuilder.buildEditor(project)

        tableModel = ListTableModel(
            arrayOf(
                SynonymColumnInfo("Value") { it.key },
                SynonymColumnInfo("Synonym") { it.value }
            ),
            getSynonymsMapFromStorage()
        )

        table = TableView(tableModel)
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.putClientProperty("terminateEditOnFocusLost", true)

        val resourceBundle = ResourceBundle.getBundle("messages")

        panel = panel {
            row {
                button("Import Settings") {
                    val importState = settingsExporter.import() ?: return@button

                    settingsStorage.loadState(importState)

                    reset()
                    project.showCommittleNotification("Commit Template settings imported successfully", NotificationType.INFORMATION)
                }
                button("Export Settings") {
                    settingsExporter.export()
                }.align(AlignX.RIGHT)
            }.bottomGap(BottomGap.NONE)
            row {
                cell(patternEditor.component)
                    .align(AlignX.FILL)
                    .comment(comment = resourceBundle.getString("settings.message-pattern-notes"))
                    .bind(
                        { _ -> patternEditor.document.text.ifBlank { null } },
                        { _, value: String? -> runWriteAction { patternEditor.document.setText(value.orEmpty()) } },
                        MutableProperty({ settingsStorage.state.pattern }, { newValue -> runWriteAction { settingsStorage.state.pattern = newValue } })
                    )
            }
            row {
                checkBox(resourceBundle.getString("settings.trim-whitespaces-start"))
                    .bindSelected(
                        { settingsStorage.state.trimWhitespacesStart },
                        { newValue -> runWriteAction { settingsStorage.state.trimWhitespacesStart = newValue } }
                    )
                checkBox(resourceBundle.getString("settings.trim-whitespaces-end"))
                    .bindSelected(
                        { settingsStorage.state.trimWhitespacesEnd },
                        { newValue -> settingsStorage.state.trimWhitespacesEnd = newValue }
                    )
                checkBox(resourceBundle.getString("settings.duplicated-whitespaces"))
                    .bindSelected(
                        { settingsStorage.state.unnecessaryWhitespaces },
                        { newValue -> settingsStorage.state.unnecessaryWhitespaces = newValue }
                    )
            }
            group(resourceBundle.getString("settings.variables-config"), false) {
                collapsibleGroup(resourceBundle.getString("settings.advanced.task-id.title")) {
                    row {
                        expandableTextField()
                            .label(resourceBundle.getString("settings.advanced.common.label"), LabelPosition.TOP)
                            .comment(comment = "Default regex: $DEFAULT_TASK_ID_REGEX")
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .bindNullableText(
                                MutableProperty(
                                    { settingsStorage.state.taskIdRegex },
                                    { newValue -> runWriteAction { settingsStorage.state.taskIdRegex = newValue } })
                            )
                        cell(ContextHelpLabel.create("Value extracted from current branch name")).align(AlignX.RIGHT)
                    }
                    row {
                        textField()
                            .label(resourceBundle.getString("settings.advanced.task-id.default.value"), LabelPosition.TOP)
                            .comment(resourceBundle.getString("settings.advanced.task-id.default.comment"))
                            .align(AlignX.FILL)
                            .bindNullableText(
                                MutableProperty(
                                    { settingsStorage.state.taskIdDefault },
                                    { newValue -> runWriteAction { settingsStorage.state.taskIdDefault = newValue } })
                            )
                        comboBox(StringCase.values().map { it.label })
                            .label(resourceBundle.getString("settings.advanced.common.postprocess"), LabelPosition.TOP)
                            .bindItem(
                                MutableProperty(
                                    { settingsStorage.state.taskIdPostProcessor },
                                    { newValue -> runWriteAction { settingsStorage.state.taskIdPostProcessor = newValue } })
                            )
                    }
                }.apply {
                    expanded = !settingsStorage.state.isDefaultTaskFields()
                }.withoutGaps()
                collapsibleGroup(resourceBundle.getString("settings.advanced.type.title")) {
                    row {
                        expandableTextField()
                            .label(resourceBundle.getString("settings.advanced.common.label"), LabelPosition.TOP)
                            .comment(comment = "Default regex: $DEFAULT_TYPE_REGEX")
                            .align(AlignX.FILL)
                            .resizableColumn()
                            .bindNullableText(
                                MutableProperty(
                                    { settingsStorage.state.typeRegex },
                                    { newValue -> runWriteAction { settingsStorage.state.typeRegex = newValue } })
                            )
                        cell(ContextHelpLabel.create("Value extracted from current branch name")).align(AlignX.RIGHT)
                    }
                    row {
                        textField()
                            .label(resourceBundle.getString("settings.advanced.type.default-type.value"), LabelPosition.TOP)
                            .comment(resourceBundle.getString("settings.advanced.type.default-type.comment"))
                            .align(AlignX.FILL)
                            .bindNullableText(
                                MutableProperty(
                                    { settingsStorage.state.typeDefault },
                                    { newValue -> runWriteAction { settingsStorage.state.typeDefault = newValue } })
                            )
                        comboBox(StringCase.values().map { it.label }, null)
                            .label(resourceBundle.getString("settings.advanced.common.postprocess"), LabelPosition.TOP)
                            .bindItem(
                                MutableProperty(
                                    { settingsStorage.state.typePostprocessor },
                                    { newValue -> runWriteAction { settingsStorage.state.typePostprocessor = newValue } })
                            )
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
                            .bindNullableText(
                                MutableProperty(
                                    { settingsStorage.state.scopeRegex },
                                    { newValue -> runWriteAction { settingsStorage.state.scopeRegex = newValue } })
                            )
                        cell(ContextHelpLabel.create("Value extracted from the modified file's path"))
                            .align(AlignX.RIGHT)
                    }
                    row {
                        textField()
                            .label(resourceBundle.getString("settings.advanced.scope.default-value"), LabelPosition.TOP)
                            .comment("When the scope isn't defined.")
                            .align(AlignX.FILL)
                            .bindNullableText(
                                MutableProperty(
                                    { settingsStorage.state.scopeDefault },
                                    { newValue -> runWriteAction { settingsStorage.state.scopeDefault = newValue } })
                            )
                        textField()
                            .label(resourceBundle.getString("settings.advanced.common.separator"), LabelPosition.TOP)
                            .comment(comment = "If there are multiple scopes.<br/>Default: $DEFAULT_SCOPE_SEPARATOR")
                            .align(AlignX.FILL)
                            .bindNullableText(
                                MutableProperty(
                                    { settingsStorage.state.scopeSeparator },
                                    { newValue -> runWriteAction { settingsStorage.state.scopeSeparator = newValue } })
                            )
                        comboBox(StringCase.values().map { it.label })
                            .label(resourceBundle.getString("settings.advanced.common.postprocess"), LabelPosition.TOP)
                            .bindItem(
                                MutableProperty(
                                    { settingsStorage.state.scopePostprocessor },
                                    { newValue -> runWriteAction { settingsStorage.state.scopePostprocessor = newValue } })
                            )
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
        settingsStorage.state.typeSynonyms = getSynonymsMapFromTable().toMutableMap()
        panel.apply()
    }

    override fun reset() {
        tableModel.apply {
            items = getSynonymsMapFromStorage()
            fireTableDataChanged()
        }
        panel.reset()
    }

    private fun getSynonymsMapFromStorage() = settingsStorage.state.typeSynonyms
        .map { SynonymPair(it.key, it.value) }
        .toMutableList()

    private fun getSynonymsMapFromTable(): Map<String, String> {
        return tableModel.items
            .filter { it.key.isNotBlank() && it.value.isNotBlank() }
            .associate { it.key to it.value }
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
        PatternEditorBuilder.dispose(patternEditor)
    }

    override fun getDisplayName(): String {
        return DISPLAY_NAME
    }

    override fun getId(): String {
        return ID
    }

    companion object {
        private const val DISPLAY_NAME = "Committle"
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
