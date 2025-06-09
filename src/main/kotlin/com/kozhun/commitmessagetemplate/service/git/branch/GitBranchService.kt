package com.kozhun.commitmessagetemplate.service.git.branch

import com.intellij.openapi.actionSystem.AnActionEvent
import git4idea.GitBranch

/**
 * Represents a service for retrieving the current Git branch.
 */
fun interface GitBranchService {

    /**
     * Returns the current Git branch.
     *
     * @return the current Git branch
     */
    suspend fun getCurrentBranch(anActionEvent: AnActionEvent): GitBranch
}
