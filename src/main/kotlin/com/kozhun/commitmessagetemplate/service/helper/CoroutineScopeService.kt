package com.kozhun.commitmessagetemplate.service.helper

import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class CoroutineScopeService(
    val coroutineScope: CoroutineScope
)
