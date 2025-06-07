package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.vcs.changes.ChangeListManager
import com.kozhun.commitmessagetemplate.enums.StringCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class FilePathScopeReplacerTest : BaseReplacerTest() {
    private var replacer = FilePathScopeReplacer(projectMock)

    @Test
    fun `should return empty string when template is empty`() {
        mockSettingState()
        mockAffectedPaths()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
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
        assertEquals("cmt: default message", runBlocking {
            replacer.replace("$ANCHOR: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return default scope when affected paths not contain value by regex`() {
        mockSettingState(
            scopeDefault = "cmt",
            scopePostprocessor = StringCase.CAPITALIZE
        )
        mockAffectedPaths(
            "something/something/something/something/something.kt",
            "test/test/test/test/test.kt",
            "project/project/project/project/project.kt"
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("cmt: default message", runBlocking {
            replacer.replace("$ANCHOR: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return scope when affected paths contain value by regex`() {
        mockSettingState(
            scopeDefault = "cmt",
            scopeRegex = "project"
        )
        mockAffectedPaths(
            "something/something/something/something/something.kt",
            "test/test/test/test/test.kt",
            "project/project/project/project/project.kt"
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("project: default message", runBlocking {
            replacer.replace("$ANCHOR: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return few scopes when affected paths contain value by regex`() {
        mockSettingState(
            scopeDefault = "cmt",
            scopeRegex = "project|test",
        )
        mockAffectedPaths(
            "something/something/something/something/something.kt",
            "test/test/test/test/test.kt",
            "project/project/project/project/project.kt"
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("test|project: default message", runBlocking {
            replacer.replace("$ANCHOR: default message", anActionEventMock)
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
            "something/something/something/something/something.kt",
            "test/test/test/test/test.kt",
            "project/project/project/project/project.kt"
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("test,project: default message", runBlocking {
            replacer.replace("$ANCHOR: default message", anActionEventMock)
        })
    }

    @Test
    fun `should return few scopes when affected paths contain value by regex and uppercase postprocessor`() {
        mockSettingState(
            scopeRegex = "project|test",
            scopePostprocessor = StringCase.UPPERCASE
        )
        mockAffectedPaths(
            "something/something/something/something/something.kt",
            "test/test/test/test/test.kt",
            "project/project/project/project/project.kt"
        )
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("TEST|PROJECT: default message", runBlocking {
            replacer.replace("$ANCHOR: default message", anActionEventMock)
        })
    }

    private fun mockAffectedPaths(vararg paths: String) {
        mockkStatic(ChangeListManager::class)

        val changeListManagerMock = mockk<ChangeListManager>()
        val filePaths = paths.map { mockFile(it) }

        every { changeListManagerMock.affectedPaths } returns filePaths
        every { ChangeListManager.getInstance(projectMock) } returns changeListManagerMock
    }

    private fun mockFile(path: String): File {
        val fileMock = mockk<File>()

        every { fileMock.path } returns path

        return fileMock
    }

    companion object {
        const val BRANCH_WITHOUT_TYPE_ID = "master"

        private const val ANCHOR = "\$SCOPE"
    }
}
