package com.kozhun.commitmessagetemplate.service.helper

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class CoroutineScopeService(
    val coroutineScope: CoroutineScope
) {
    companion object {
        @JvmStatic
        fun getInstance(project: Project): CoroutineScopeService = project.service()
    }
}
