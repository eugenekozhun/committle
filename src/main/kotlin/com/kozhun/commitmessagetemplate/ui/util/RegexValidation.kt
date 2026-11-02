package com.kozhun.commitmessagetemplate.ui.util

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.fields.ExpandableTextField
import java.util.regex.PatternSyntaxException

fun validateRegex(field: ExpandableTextField): ValidationInfo? {
    if (field.text.isBlank()) {
        return null
    }

    return try {
        field.text.toRegex()
        null
    } catch (exception: PatternSyntaxException) {
        ValidationInfo("Invalid regular expression: ${exception.message}", field)
    }
}
