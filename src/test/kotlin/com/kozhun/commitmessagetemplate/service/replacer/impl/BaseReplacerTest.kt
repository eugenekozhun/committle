package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.enums.StringCase
import com.kozhun.commitmessagetemplate.service.git.branch.GitBranchService
import com.kozhun.commitmessagetemplate.service.git.branch.impl.GitBranchServiceImpl
import com.kozhun.commitmessagetemplate.storage.SettingsState
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import git4idea.GitLocalBranch
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach

abstract class BaseReplacerTest {
    protected var projectMock = mockk<Project>()
    protected var anActionEventMock = mockk<AnActionEvent>()

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    protected fun mockBranchName(branchName: String) {
        mockkStatic(GitBranchServiceImpl::class)

        val gitBranchServiceMock = mockk<GitBranchService>()
        val gitLocalBranchMock = mockk<GitLocalBranch>()

        every { gitLocalBranchMock.name } returns branchName
        every {
            runBlocking {
                gitBranchServiceMock.getCurrentBranch(anActionEventMock)
            }
        } returns gitLocalBranchMock
        every { GitBranchServiceImpl.getInstance(projectMock) } returns gitBranchServiceMock
    }

    @Suppress("LongParameterList")
    protected fun mockSettingState(
        taskIdRegex: String = "",
        taskIdDefault: String = "",
        taskIdPostprocessor: String = "",
        typeRegex: String = "",
        typeDefault: String = "",
        typePostprocessor: StringCase = StringCase.NONE,
        typeSynonyms: MutableMap<String, String> = mutableMapOf(),
        scopeRegex: String = "",
        scopeDefault: String = "",
        scopeSeparator: String = "",
        scopePostprocessor: StringCase = StringCase.NONE,
    ) {
        mockkStatic(SettingsStorage::class)

        val settingsStorageMock = mockk<SettingsStorage>()
        val settingsStateMock = mockk<SettingsState>()

        // Task id
        every { settingsStateMock.taskIdRegex } returns taskIdRegex
        every { settingsStateMock.taskIdDefault } returns taskIdDefault
        every { settingsStateMock.taskIdPostProcessor } returns taskIdPostprocessor

        // Type
        every { settingsStateMock.typeRegex } returns typeRegex
        every { settingsStateMock.typeDefault } returns typeDefault
        every { settingsStateMock.typePostprocessor } returns typePostprocessor.label
        every { settingsStateMock.typeSynonyms } returns typeSynonyms

        // Scope
        every { settingsStateMock.scopeRegex } returns scopeRegex
        every { settingsStateMock.scopeDefault } returns scopeDefault
        every { settingsStateMock.scopePostprocessor } returns scopePostprocessor.label
        every { settingsStateMock.scopeSeparator } returns scopeSeparator

        // Other
        every { settingsStorageMock.state } returns settingsStateMock
        every { SettingsStorage.getInstance(projectMock) } returns settingsStorageMock
    }
}
