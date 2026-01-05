package com.kozhun.commitmessagetemplate.util

import com.kozhun.commitmessagetemplate.enums.StringCase

fun String.toNotBlankRegex() = this.takeIf { it.isNotBlank() }?.toRegex()

fun String.toNotBlankRegex(option: RegexOption) = this.takeIf { it.isNotBlank() }?.toRegex(option)

fun String.toCase(type: StringCase) = when (type) {
    StringCase.CAPITALIZE -> this.replaceFirstChar { it.uppercase() }
    StringCase.UPPERCASE -> this.uppercase()
    StringCase.LOWERCASE -> this.lowercase()
    else -> this
}
