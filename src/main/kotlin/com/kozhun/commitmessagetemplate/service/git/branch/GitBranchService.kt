package com.kozhun.commitmessagetemplate.service.git.branch

import com.intellij.openapi.actionSystem.AnActionEvent
import git4idea.GitBranch

fun interface GitBranchService {

    suspend fun getCurrentBranch(anActionEvent: AnActionEvent): GitBranch
}
