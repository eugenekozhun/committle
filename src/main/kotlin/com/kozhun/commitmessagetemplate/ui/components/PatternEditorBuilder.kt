package com.kozhun.commitmessagetemplate.ui.components

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.kozhun.commitmessagetemplate.language.CMTLanguage
import java.awt.Dimension

object PatternEditorBuilder {
    private const val DEFAULT_DOCUMENT_TEXT = ""
    private const val VIRTUAL_FILE_NAME = "settings-pattern.cmt"
    private const val TEXT_AREA_HEIGHT = 125

    fun buildEditor(project: Project): Editor {
        val file = LightVirtualFile(VIRTUAL_FILE_NAME, CMTLanguage.INSTANCE, DEFAULT_DOCUMENT_TEXT)
        val document = FileDocumentManager.getInstance().getDocument(file)!!
        val editorFactory = EditorFactory.getInstance()
        val editor = editorFactory.createEditor(document, project, file, false)

        editor.settings.apply {
            additionalLinesCount = 0
            isLineNumbersShown = true
            isVirtualSpace = false
            isLineMarkerAreaShown = false
            isIndentGuidesShown = false
            isFoldingOutlineShown = false
            isCaretRowShown = false
        }
        editor.setupHighlighter(file)

        editor.component.apply {
            preferredSize = Dimension(preferredSize.width, TEXT_AREA_HEIGHT)
        }

        return editor
    }

    fun dispose(patternEditor: Editor) {
        EditorFactory.getInstance().releaseEditor(patternEditor)
    }
}

private fun Editor.setupHighlighter(file: VirtualFile) {
    val syntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(file.fileType, project, file)
    val editorHighlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(syntaxHighlighter, this.colorsScheme)
    (this as EditorEx).highlighter = editorHighlighter
}
