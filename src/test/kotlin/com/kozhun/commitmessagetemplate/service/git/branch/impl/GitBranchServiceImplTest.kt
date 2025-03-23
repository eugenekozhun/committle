package com.kozhun.commitmessagetemplate.service.git.branch.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitLocalBranch
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GitBranchServiceImplTest {
    private val projectMock = mockk<Project>(relaxed = true)
    private val gitBranchMock = mockk<GitLocalBranch>()
    private val anActionEventMock = mockk<AnActionEvent>(relaxed = true)

    private val gitBranchService = GitBranchServiceImpl(projectMock)

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `get current branch from first repository when no selected changes`() {
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns null
        mockkStatic(GitRepositoryManager::class)
        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager>()
        every { repoManagerMock.repositories } returns listOf(repoMock)
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        val result = gitBranchService.getCurrentBranch(anActionEventMock)
        assertEquals(gitBranchMock, result)
    }

    @Test
    fun `should throw exception if repository list is empty when no selected changes`() {
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns null
        mockkStatic(GitRepositoryManager::class)
        val repoManagerMock = mockk<GitRepositoryManager>()
        every { repoManagerMock.repositories } returns emptyList()
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        val exception = assertThrows<IllegalStateException> {
            gitBranchService.getCurrentBranch(anActionEventMock)
        }
        assertEquals("Current git branch not found.", exception.message)
    }

    @Test
    fun `get current branch from selected change`() {
        val fileMock = mockk<VirtualFile>()
        val changeMock = mockk<Change> {
            every { virtualFile } returns fileMock
        }
        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager>()
        every { repoManagerMock.getRepositoryForFile(fileMock) } returns repoMock
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns arrayOf(changeMock)
        val result = gitBranchService.getCurrentBranch(anActionEventMock)
        assertEquals(gitBranchMock, result)
    }

    @Test
    fun `should throw exception when repository is not found for selected file`() {
        val fileMock = mockk<VirtualFile>()
        val changeMock = mockk<Change> {
            every { virtualFile } returns fileMock
        }
        val repoManagerMock = mockk<GitRepositoryManager>()
        every { repoManagerMock.getRepositoryForFile(fileMock) } returns null
        every { repoManagerMock.repositories } returns emptyList()
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns arrayOf(changeMock)
        val exception = assertThrows<IllegalStateException> {
            gitBranchService.getCurrentBranch(anActionEventMock)
        }
        assertEquals("Current git branch not found.", exception.message)
    }

    @Test
    fun `get branch fallback to first repository when selected changes array is empty`() {
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns emptyArray()
        mockkStatic(GitRepositoryManager::class)
        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager>()
        every { repoManagerMock.repositories } returns listOf(repoMock)
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        val result = gitBranchService.getCurrentBranch(anActionEventMock)
        assertEquals(gitBranchMock, result)
    }

    @Test
    fun `get branch fallback to first repository when selected change has null virtual file`() {
        val changeMock = mockk<Change> {
            every { virtualFile } returns null
        }
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns arrayOf(changeMock)
        mockkStatic(GitRepositoryManager::class)
        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager>()
        every { repoManagerMock.repositories } returns listOf(repoMock)
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        val result = gitBranchService.getCurrentBranch(anActionEventMock)
        assertEquals(gitBranchMock, result)
    }
}
