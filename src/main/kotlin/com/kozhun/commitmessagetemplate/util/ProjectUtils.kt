package com.kozhun.commitmessagetemplate.util

import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.storage.SettingsStorage

fun Project.storage() = SettingsStorage.getInstance(this)
