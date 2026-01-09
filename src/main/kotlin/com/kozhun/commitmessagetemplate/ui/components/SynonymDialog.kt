package com.kozhun.commitmessagetemplate.ui.components

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.kozhun.commitmessagetemplate.ui.model.SynonymPair
import javax.swing.JComponent

class SynonymDialog(
    private val existingKeys: Set<String>,
    private val initialValue: SynonymPair? = null
) : DialogWrapper(true) {

    var value: String = initialValue?.key.orEmpty()
    var synonym: String = initialValue?.value.orEmpty()

    init {
        title = if (initialValue == null) "Add Synonym" else "Edit Synonym"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Value:") {
                textField()
                    .bindText(::value)
                    .focused()
                    .validationOnApply {
                        val input = it.text.trim()
                        when {
                            input.isEmpty() -> error("Value cannot be empty")
                            input != initialValue?.key && existingKeys.contains(input) ->
                                error("Value already exists in synonyms table")

                            else -> null
                        }
                    }
            }
            row("Synonym:") {
                textField()
                    .bindText(::synonym)
                    .validationOnApply {
                        if (it.text.trim().isEmpty()) error("Synonym cannot be empty") else null
                    }
            }
        }
    }
}

