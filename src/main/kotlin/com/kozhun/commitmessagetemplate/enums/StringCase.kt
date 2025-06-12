package com.kozhun.commitmessagetemplate.enums

enum class StringCase(val label: String) {
    NONE("None"),
    CAPITALIZE("Capitalize"),
    UPPERCASE("UPPERCASE"),
    LOWERCASE("lowercase");

    companion object {
        fun labelValueOf(label: String) = values().find { it.label == label }
    }
}
