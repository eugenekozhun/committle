package com.kozhun.commitmessagetemplate.service.caret

fun interface CaretService {
    fun getCaretOffsetByAnchor(message: String): Pair<String, Int>
}
