package com.kozhun.commitmessagetemplate.language.highlighter

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.kozhun.commitmessagetemplate.language.lexer.CMTLexerAdapter
import com.kozhun.commitmessagetemplate.language.psi.CMTTypes

/**
 * CMT Syntax Highlighter for highlighting CMT code.
 */
class CMTSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        return CMTLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when (tokenType) {
            CMTTypes.TASK_ID,
            CMTTypes.TYPE,
            CMTTypes.SCOPE -> KEYWORD_KEYS

            CMTTypes.CARET_POSITION -> CARET_KEYS

            else -> EMPTY_KEYS
        }
    }

    companion object {
        private val KEYWORD = createTextAttributesKey("CMT_KEYWORD", DefaultLanguageHighlighterColors.CONSTANT)
        private val CARET = createTextAttributesKey("CMT_CARET_POSITION", DefaultLanguageHighlighterColors.KEYWORD)

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val CARET_KEYS = arrayOf(CARET)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }
}
