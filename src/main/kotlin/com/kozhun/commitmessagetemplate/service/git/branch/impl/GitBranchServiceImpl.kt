package com.kozhun.commitmessagetemplate.service.git.branch.impl

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
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
import java.util.concurrent.Callable

@Service(Service.Level.PROJECT)
class GitBranchServiceImpl(
    private val project: Project
) : GitBranchService {

    override fun getCurrentBranch(anActionEvent: AnActionEvent): GitBranch {
        val gitRepositoryManager = GitRepositoryManager.getInstance(project)

        return getRepoByChanges(anActionEvent, gitRepositoryManager) ?: getFirstRepo(gitRepositoryManager)
    }

    private fun getRepoByChanges(anActionEvent: AnActionEvent, gitRepositoryManager: GitRepositoryManager): GitLocalBranch? {
        return anActionEvent.getData(VcsDataKeys.SELECTED_CHANGES)
            ?.firstOrNull()
            ?.virtualFile
            ?.let { getRepoForFile(gitRepositoryManager, it) }
            ?.currentBranch
    }

    private fun getRepoForFile(gitRepositoryManager: GitRepositoryManager, it: VirtualFile): GitRepository {
        val repository = if (ApplicationManager.getApplication().isDispatchThread) {
            ApplicationManager.getApplication().executeOnPooledThread(Callable {
                gitRepositoryManager.getRepositoryForFile(it)
            }).get()
        } else {
            gitRepositoryManager.getRepositoryForFile(it)
        }

        return repository ?: error("Git repository not found for the selected file.")
    }

    private fun getFirstRepo(gitRepositoryManager: GitRepositoryManager): GitLocalBranch {
        return gitRepositoryManager.repositories.firstOrNull()
            ?.currentBranch
            ?: error("Current git branch not found.")
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): GitBranchService = project.service<GitBranchServiceImpl>()
    }
}
