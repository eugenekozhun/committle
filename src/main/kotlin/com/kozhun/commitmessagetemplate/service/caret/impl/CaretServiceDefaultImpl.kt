package com.kozhun.commitmessagetemplate.service.caret.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.service.caret.CaretService

@Service(Service.Level.PROJECT)
class CaretServiceDefaultImpl : CaretService {

    override fun getCaretOffsetByAnchor(message: String): Pair<String, Int> {
        return when (val caretOffset = message.indexOf(CARET_POSITION_ANCHOR)) {
            NOT_FOUND_INDEX -> message to message.length
            else -> message.replace(CARET_POSITION_ANCHOR, "") to caretOffset
        }
    }

    companion object {
        private const val NOT_FOUND_INDEX = -1
        const val CARET_POSITION_ANCHOR = "\$CARET_POSITION"

        @JvmStatic
        fun getInstance(project: Project): CaretService = project.service<CaretServiceDefaultImpl>()
    }
}
