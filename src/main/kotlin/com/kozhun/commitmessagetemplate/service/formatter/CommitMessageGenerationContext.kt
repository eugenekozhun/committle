package com.kozhun.commitmessagetemplate.service.formatter

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.ui.Refreshable
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

data class CommitMessageGenerationContext(
    val changes: List<Change> = emptyList(),
    val affectedPaths: List<String> = emptyList(),
    val repository: GitRepository? = null,
    val isBreakingChange: Boolean = false,
) {
    val currentBranchName: String?
        get() = repository?.currentBranch?.name

    companion object {
        /**
         * Captures VCS changes from the action event. Must be called on EDT inside `actionPerformed`;
         * the event itself must not be retained afterwards.
         */
        fun captureChanges(anActionEvent: AnActionEvent): List<Change> {
            val panel = anActionEvent.getData(Refreshable.PANEL_KEY) as? CheckinProjectPanel
            return panel?.selectedChanges?.toList()
                ?: anActionEvent.getData(VcsDataKeys.CHANGES)?.toList()
                .orEmpty()
        }

        /**
         * Resolves repository data. Must be called on a background thread:
         * [GitRepositoryManager.getRepositoryForFile] asserts non-EDT access.
         */
        fun create(project: Project, changes: List<Change>, selectedChange: Change?): CommitMessageGenerationContext {
            val affectedPaths = ChangeListManager.getInstance(project)
                .affectedPaths
                .mapNotNullTo(mutableListOf()) { it.path }
            val repositoryManager = GitRepositoryManager.getInstance(project)
            val repository = selectedChange
                ?.virtualFile
                ?.let(repositoryManager::getRepositoryForFile)
                ?: repositoryManager.repositories.firstOrNull()

            return CommitMessageGenerationContext(changes, affectedPaths, repository)
        }
    }
}
