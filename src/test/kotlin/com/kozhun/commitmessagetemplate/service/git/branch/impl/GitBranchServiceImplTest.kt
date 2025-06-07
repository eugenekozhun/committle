package com.kozhun.commitmessagetemplate.service.git.branch.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
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
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GitBranchServiceImplTest {
    private val projectMock = mockk<Project>(relaxed = true)
    private val gitBranchMock = mockk<GitLocalBranch>(relaxed = true)
    private val anActionEventMock = mockk<AnActionEvent>(relaxed = true)

    private lateinit var gitBranchService: GitBranchServiceImpl

    @BeforeEach
    fun setUp() {
        gitBranchService = GitBranchServiceImpl(projectMock)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `get current branch from first repository when no selected changes`() {
        mockkStatic(GitRepositoryManager::class)
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns null

        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager> {
            every { repositories } returns listOf(repoMock)
        }
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock

        val result = runBlocking {
            gitBranchService.getCurrentBranch(anActionEventMock)
        }

        assertEquals(gitBranchMock, result)
    }

    @Test
    fun `should throw exception if repository list is empty when no selected changes`() {
        mockkStatic(GitRepositoryManager::class)
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns null

        val repoManagerMock = mockk<GitRepositoryManager> {
            every { repositories } returns emptyList()
        }
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock

        val exception = assertThrows<IllegalStateException> {
            runBlocking {
                gitBranchService.getCurrentBranch(anActionEventMock)
            }
        }
        assertEquals("Current git branch not found.", exception.message)
    }

    @Test
    fun `get current branch from selected change`() {
        mockkStatic(GitRepositoryManager::class)
        mockkStatic(ApplicationManager::class)

        val appMock = mockk<Application>()
        every { ApplicationManager.getApplication() } returns appMock
        every { appMock.isDispatchThread } returns false

        val fileMock = mockk<VirtualFile>()
        val changeMock = mockk<Change> {
            every { virtualFile } returns fileMock
        }
        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager> {
            every { getRepositoryForFile(fileMock) } returns repoMock
        }
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns arrayOf(changeMock)

        val result = runBlocking {
            gitBranchService.getCurrentBranch(anActionEventMock)
        }
        assertEquals(gitBranchMock, result)
    }

    @Test
    fun `should throw exception when repository is not found for selected file`() {
        mockkStatic(GitRepositoryManager::class)
        mockkStatic(ApplicationManager::class)

        val appMock = mockk<Application>()
        every { ApplicationManager.getApplication() } returns appMock
        every { appMock.isDispatchThread } returns false

        val fileMock = mockk<VirtualFile>()
        val changeMock = mockk<Change> {
            every { virtualFile } returns fileMock
        }
        val repoManagerMock = mockk<GitRepositoryManager> {
            every { getRepositoryForFile(fileMock) } returns null
            every { repositories } returns emptyList()
        }
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns arrayOf(changeMock)

        val exception = assertThrows<IllegalStateException> {
            runBlocking {
                gitBranchService.getCurrentBranch(anActionEventMock)
            }
        }
        assertEquals("Git repository not found for the selected file.", exception.message)
    }

    @Test
    fun `get branch fallback to first repository when selected changes array is empty`() {
        mockkStatic(GitRepositoryManager::class)
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns emptyArray()

        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager> {
            every { repositories } returns listOf(repoMock)
        }
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock

        val result = runBlocking {
            gitBranchService.getCurrentBranch(anActionEventMock)
        }
        assertEquals(gitBranchMock, result)
    }

    @Test
    fun `get branch fallback to first repository when selected change has null virtual file`() {
        mockkStatic(GitRepositoryManager::class)
        val changeMock = mockk<Change> {
            every { virtualFile } returns null
        }
        every { anActionEventMock.getData(VcsDataKeys.SELECTED_CHANGES) } returns arrayOf(changeMock)

        val repoMock = mockk<GitRepository> {
            every { currentBranch } returns gitBranchMock
        }
        val repoManagerMock = mockk<GitRepositoryManager> {
            every { repositories } returns listOf(repoMock)
        }
        every { GitRepositoryManager.getInstance(projectMock) } returns repoManagerMock

        val result = runBlocking {
            gitBranchService.getCurrentBranch(anActionEventMock)
        }
        assertEquals(gitBranchMock, result)
    }
}
