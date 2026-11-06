package com.kozhun.commitmessagetemplate.e2e

import com.intellij.driver.client.Remote

@Remote("com.kozhun.commitmessagetemplate.storage.SettingsStorage", plugin = "com.kozhun.commit-message-template")
interface SettingsStorageRef {
    fun getState(): SettingsStateRef
}

@Remote("com.kozhun.commitmessagetemplate.storage.SettingsState", plugin = "com.kozhun.commit-message-template")
interface SettingsStateRef {
    fun getPattern(): String?
    fun setPattern(value: String?)
    fun getTrimWhitespacesStart(): Boolean
    fun setTrimWhitespacesStart(value: Boolean)
    fun getTrimWhitespacesEnd(): Boolean
    fun setTrimWhitespacesEnd(value: Boolean)
    fun getUnnecessaryWhitespaces(): Boolean
    fun setUnnecessaryWhitespaces(value: Boolean)
    fun getTaskIdRegex(): String?
    fun setTaskIdRegex(value: String?)
    fun getTaskIdDefault(): String?
    fun setTaskIdDefault(value: String?)
    fun getTaskIdPostProcessor(): String?
    fun setTaskIdPostProcessor(value: String?)
    fun getTypeRegex(): String?
    fun setTypeRegex(value: String?)
    fun getTypeDefault(): String?
    fun setTypeDefault(value: String?)
    fun getTypeSynonyms(): Map<String, String>
    fun setTypeSynonyms(value: Map<String, String>)
    fun getTypePostprocessor(): String?
    fun setTypePostprocessor(value: String?)
    fun getScopeRegex(): String?
    fun setScopeRegex(value: String?)
    fun getScopeDefault(): String?
    fun setScopeDefault(value: String?)
    fun getScopeSeparator(): String?
    fun setScopeSeparator(value: String?)
    fun getScopePostprocessor(): String?
    fun setScopePostprocessor(value: String?)
}

@Remote("com.kozhun.commitmessagetemplate.service.settings.SettingsExporter", plugin = "com.kozhun.commit-message-template")
interface SettingsExporterRef {
    fun exportTo(file: java.io.File)
    fun importFrom(file: java.io.File): SettingsStateRef?
}

@Remote("com.intellij.openapi.vfs.LocalFileSystem")
interface LocalFileSystemRef {
    fun refreshAndFindFileByPath(path: String): com.intellij.driver.sdk.VirtualFile?
}

@Remote("com.intellij.openapi.fileEditor.FileEditorManager")
interface FileEditorManagerRef {
    fun openFile(file: com.intellij.driver.sdk.VirtualFile, focusEditor: Boolean): Array<FileEditorRef>
}

@Remote("com.intellij.openapi.fileEditor.FileEditor")
interface FileEditorRef

@Remote("com.intellij.openapi.fileTypes.FileTypeManager")
interface FileTypeManagerRef {
    fun getFileTypeByFile(file: com.intellij.driver.sdk.VirtualFile): FileTypeRef
}

@Remote("com.intellij.openapi.fileTypes.FileType")
interface FileTypeRef {
    fun getName(): String
}

@Remote("com.intellij.psi.PsiElement")
interface CmtPsiElementRef {
    fun getText(): String?
    fun getChildren(): Array<CmtPsiElementRef>
}

@Remote("com.kozhun.commitmessagetemplate.language.highlighter.CMTSyntaxHighlighter", plugin = "com.kozhun.commit-message-template")
interface CmtSyntaxHighlighterRef {
    fun getHighlightingLexer(): LexerRef
    fun getTokenHighlights(tokenType: IElementTypeRef): Array<TextAttributesKeyRef>
}

@Remote("com.intellij.lexer.Lexer")
interface LexerRef {
    fun start(buffer: CharSequence)
    fun getTokenType(): IElementTypeRef?
    fun getTokenText(): String
    fun advance()
}

@Remote("com.intellij.psi.tree.IElementType")
interface IElementTypeRef

@Remote("com.intellij.openapi.editor.colors.TextAttributesKey")
interface TextAttributesKeyRef {
    fun getExternalName(): String
}

@Remote("com.intellij.openapi.project.ProjectManagerEx")
interface ProjectManagerExRef {
    fun openProject(path: java.nio.file.Path): com.intellij.driver.sdk.Project?
}

@Remote("java.awt.Component")
interface AwtComponentRef {
    fun isFocusOwner(): Boolean
}

@Remote("javax.swing.JButton")
interface JButtonRef {
    fun doClick()
}
