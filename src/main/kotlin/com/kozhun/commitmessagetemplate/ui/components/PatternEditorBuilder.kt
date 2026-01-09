package com.kozhun.commitmessagetemplate.ui.components

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import com.kozhun.commitmessagetemplate.language.CMTLanguage
import java.awt.Dimension

object PatternEditorBuilder {
    private const val TEXT_AREA_HEIGHT = 125

    fun buildEditor(project: Project, initialText: String = ""): LanguageTextField {
        return LanguageTextField(
            CMTLanguage.INSTANCE,
            project,
            initialText,
            false
        ).apply {
            preferredSize = Dimension(preferredSize.width, TEXT_AREA_HEIGHT)

            addSettingsProvider { editor ->
                editor.settings.apply {
                    isLineNumbersShown = true
                    isAdditionalPageAtBottom = false
                    isFoldingOutlineShown = false
                    isLineMarkerAreaShown = false
                    isFoldingOutlineShown = false
                }
                editor.colorsScheme = EditorColorsManager.getInstance().globalScheme
            }
        }
    }
}
