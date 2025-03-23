package com.kozhun.commitmessagetemplate.service.replacer.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BranchTaskIdReplacerTest : BaseReplacerTest() {
    private var replacer = BranchTaskIdReplacer(projectMock)

    @Test
    fun `replace empty template with default regex`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TASK_ID)
        assertEquals("", replacer.replace("", anActionEventMock))
    }

    @Test
    fun `replace empty template with custom regex`() {
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITHOUT_TASK_ID)
        assertEquals("", replacer.replace("", anActionEventMock))
    }

    @Test
    fun `replace non-task-id branch empty template`() {
        val template = "Some changes"
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TASK_ID)
        assertEquals(template, replacer.replace(template, anActionEventMock))
    }

    @Test
    fun `replace non-task-id branch template`() {
        val template = "Some changes"
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITH_TASK_ID)
        assertEquals(template, replacer.replace(template, anActionEventMock))
    }

    @Test
    fun `replace with mismatched task-id`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TASK_ID)
        assertEquals("[]: Some changes", replacer.replace("[${BranchTaskIdReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with custom default when task-id mismatched`() {
        mockSettingState(taskIdDefault = "CMT-000")
        mockBranchName(BRANCH_WITHOUT_TASK_ID)
        assertEquals("[CMT-000]: Some changes", replacer.replace("[${BranchTaskIdReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with task-id in branch`() {
        mockSettingState()
        mockBranchName(BRANCH_WITH_TASK_ID)
        assertEquals("[$TASK_ID]: Some changes", replacer.replace("[${BranchTaskIdReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with custom mismatched task-id in branch`() {
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITH_TASK_ID)
        assertEquals("[]: Some changes", replacer.replace("[${BranchTaskIdReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with custom task-id in branch`() {
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITH_CUSTOM_TASK_ID)
        assertEquals("[$CUSTOM_TASK_ID]: Some changes", replacer.replace("[${BranchTaskIdReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    private companion object {
        const val BRANCH_WITHOUT_TASK_ID = "master"

        const val TASK_ID = "CMT-123"
        const val BRANCH_WITH_TASK_ID = "feature/$TASK_ID-refactoring"

        const val CUSTOM_TASK_ID = "TEST-123"
        const val CUSTOM_TASK_ID_REGEX = "TEST-\\d+"
        const val BRANCH_WITH_CUSTOM_TASK_ID = "feature/$CUSTOM_TASK_ID-refactoring"
    }
}
