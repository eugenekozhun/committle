package com.kozhun.commitmessagetemplate.service.caret.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.service.caret.CaretService

@Service(Service.Level.PROJECT)
class CaretServiceDefaultImpl : CaretService {

    override fun getCaretOffsetByAnchor(message: String): Pair<String, Int> {
        val anchor = CARET_POSITION_ANCHOR
        val index = message.indexOf(anchor)

        if (index == NOT_FOUND_INDEX) {
            return message to message.length
        }

        val resultMessage = StringBuilder(message)
            .delete(index, index + anchor.length)
            .toString()

        return resultMessage to index
    }

    companion object {
        private const val NOT_FOUND_INDEX = -1
        const val CARET_POSITION_ANCHOR = "\$CARET_POSITION"

        @JvmStatic
        fun getInstance(project: Project): CaretService = project.service<CaretServiceDefaultImpl>()
    }
}

