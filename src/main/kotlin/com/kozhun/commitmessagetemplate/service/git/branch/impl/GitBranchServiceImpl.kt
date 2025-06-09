package com.kozhun.commitmessagetemplate.service.git.branch.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.kozhun.commitmessagetemplate.service.git.branch.GitBranchService
import git4idea.GitBranch
import git4idea.GitLocalBranch
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Service(Service.Level.PROJECT)
class GitBranchServiceImpl(project: Project) : GitBranchService {
    private val gitRepositoryManager = GitRepositoryManager.getInstance(project)

    override suspend fun getCurrentBranch(anActionEvent: AnActionEvent): GitBranch {
        return getCurrentBranchForSelectedChange(anActionEvent) ?: getCurrentBranchFromFirstRepo()
    }

    private suspend fun getCurrentBranchForSelectedChange(anActionEvent: AnActionEvent): GitLocalBranch? {
        return anActionEvent.getData(VcsDataKeys.SELECTED_CHANGES)
            ?.firstOrNull()
            ?.virtualFile
            ?.let { getRepository(it) }
            ?.currentBranch
    }

    private suspend fun getRepository(file: VirtualFile): GitRepository {
        val repository = withContext(Dispatchers.Default) {
            gitRepositoryManager.getRepositoryForFile(file)
        }

        return repository ?: error("Git repository not found for the selected file.")
    }

    private fun getCurrentBranchFromFirstRepo(): GitLocalBranch {
        return gitRepositoryManager.repositories.firstOrNull()
            ?.currentBranch
            ?: error("Current git branch not found.")
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): GitBranchService = project.service<GitBranchServiceImpl>()
    }
}
