package com.kozhun.commitmessagetemplate.util

import ai.grazie.utils.capitalize
import com.kozhun.commitmessagetemplate.enums.StringCase

fun String.toNotBlankRegex() = this.takeIf { it.isNotBlank() }?.toRegex()

fun String.toNotBlankRegex(option: RegexOption) = this.takeIf { it.isNotBlank() }?.toRegex(option)

fun String.toCase(type: StringCase) = when (type) {
    StringCase.CAPITALIZE -> this.capitalize()
    StringCase.UPPERCASE -> this.uppercase()
    StringCase.LOWERCASE -> this.lowercase()
    else -> this
}
