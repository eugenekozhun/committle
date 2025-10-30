package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserBase
import com.intellij.openapi.vcs.changes.ui.ChangesTree
import com.intellij.openapi.vfs.VirtualFile
import com.kozhun.commitmessagetemplate.enums.StringCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class FilePathScopeReplacerTest : BaseReplacerTest() {
    @Test
    fun `should return empty string when template is empty`() {
        mockSettingState()
        mockAffectedPaths()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("", runBlocking {
            replacer.replace("", anActionEventMock)
        })
    }

    @Test
    fun `should return default scope when affected paths are empty (change commit message case)`() {
        mockSettingState(
            scopeDefault = "cmt"
        )
        mockAffectedPaths()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("cmt: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return default scope when affected paths not contain value by regex`() {
        mockSettingState(
            scopeDefault = "cmt",
            scopePostprocessor = StringCase.CAPITALIZE
        )
        mockAffectedPaths(
            includedPaths = arrayOf(
                "something/something/something/something/something.kt",
                "test/test/test/test/test.kt",
                "project/project/project/project/project.kt"
            )
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("cmt: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return scope when affected paths contain value by regex`() {
        mockSettingState(
            scopeDefault = "cmt",
            scopeRegex = "project"
        )
        mockAffectedPaths(
            includedPaths = arrayOf(
                "something/something/something/something/something.kt",
                "test/test/test/test/test.kt",
                "project/project/project/project/project.kt"
            )
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("project: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return few scopes when affected paths contain value by regex`() {
        mockSettingState(
            scopeDefault = "cmt",
            scopeRegex = "project|test",
        )
        mockAffectedPaths(
            includedPaths = arrayOf(
                "something/something/something/something/something.kt",
                "test/test/test/test/test.kt",
                "project/project/project/project/project.kt"
            )
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("test|project: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return few scopes when affected paths contain value by regex and custom separator`() {
        mockSettingState(
            scopeDefault = "cmt",
            scopeRegex = "project|test",
            scopeSeparator = ","
        )
        mockAffectedPaths(
            includedPaths = arrayOf(
                "something/something/something/something/something.kt",
                "test/test/test/test/test.kt",
                "project/project/project/project/project.kt"
            )
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("test,project: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return few scopes when affected paths contain value by regex and uppercase postprocessor`() {
        mockSettingState(
            scopeRegex = "project|test",
            scopePostprocessor = StringCase.UPPERCASE
        )
        mockAffectedPaths(
            includedPaths = arrayOf(
                "something/something/something/something/something.kt",
                "test/test/test/test/test.kt",
                "project/project/project/project/project.kt"
            )
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("TEST|PROJECT: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return scopes from all included changes`() {
        mockSettingState(
            scopeRegex = "project|test",
        )
        mockAffectedPaths(
            includedPaths = arrayOf(
                "project/project/project/project/project.kt",
                "test/test/test/test/test.kt"
            )
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("project|test: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return scopes from changes when nothing is selected`() {
        mockSettingState(
            scopeRegex = "project|test",
        )
        mockAffectedPaths(
            affectedPaths = arrayOf(
                "project/project/project/project/project.kt",
                "test/test/test/test/test.kt"
            ),
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = FilePathScopeReplacer(projectMock)

        assertEquals("project|test: default message", runBlocking {
            replacer.replace("${replacer.anchor}: default message", anActionEventMock)
        })
    }

    private fun mockAffectedPaths(
        includedPaths: Array<String> = emptyArray(),
        affectedPaths: Array<String> = emptyArray(),
    ) {
        mockkStatic(ChangeListManager::class)

        val changeListManagerMock = mockk<ChangeListManager>()
        val filePaths = affectedPaths.map { mockFile(it) }

        every { changeListManagerMock.affectedPaths } returns filePaths
        every { ChangeListManager.getInstance(projectMock) } returns changeListManagerMock

        val includedChanges = mockIncludedChanges(includedPaths)

        val changesBrowserMock = mockk<ChangesBrowserBase>(relaxed = true)
        val changesTreeMock = mockk<ChangesTree>(relaxed = true)

        every { changesBrowserMock.viewer } returns changesTreeMock
        every { changesTreeMock.includedSet } returns includedChanges
        every { anActionEventMock.getData(ChangesBrowserBase.DATA_KEY) } returns changesBrowserMock
    }

    private fun mockFile(path: String): File {
        val fileMock = mockk<File>()

        every { fileMock.path } returns path

        return fileMock
    }

    private fun mockChanges(paths: Array<String>): Array<Change> {
        return paths.map { path ->
            val changeMock = mockk<Change>()
            val virtualFileMock = mockk<VirtualFile>()

            every { virtualFileMock.path } returns path
            every { changeMock.virtualFile } returns virtualFileMock
            every { changeMock.afterRevision } returns null
            every { changeMock.beforeRevision } returns null

            changeMock
        }.toTypedArray()
    }

    private fun mockIncludedChanges(paths: Array<String>): MutableSet<Change> {
        return mockChanges(paths).toMutableSet()
    }

    companion object {
        const val BRANCH_WITHOUT_TYPE_ID = "master"
    }
}
