package com.kozhun.commitmessagetemplate.service.replacer.impl

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BranchTaskIdReplacerTest : BaseReplacerTest() {

    @Test
    fun `replace empty template with default regex`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals("", replacement.value)
        assertEquals(false, replacement.hasValue)
    }

    @Test
    fun `replace empty template with custom regex`() {
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITHOUT_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals("", replacement.value)
        assertEquals(false, replacement.hasValue)
    }

    @Test
    fun `replace non-task-id branch empty template`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals("", replacement.value)
        assertEquals(false, replacement.hasValue)
    }

    @Test
    fun `replace non-task-id branch template`() {
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITH_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals("", replacement.value)
        assertEquals(false, replacement.hasValue)
    }

    @Test
    fun `replace with mismatched task-id`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals("", replacement.value)
        assertEquals(false, replacement.hasValue)
    }

    @Test
    fun `replace with custom default when task-id mismatched`() {
        mockSettingState(taskIdDefault = "CMT-000")
        mockBranchName(BRANCH_WITHOUT_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals("CMT-000", replacement.value)
        assertEquals(true, replacement.hasValue)
    }

    @Test
    fun `replace with task-id in branch`() {
        mockSettingState()
        mockBranchName(BRANCH_WITH_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals(TASK_ID, replacement.value)
        assertEquals(true, replacement.hasValue)
    }

    @Test
    fun `replace with custom mismatched task-id in branch`() {
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITH_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals("", replacement.value)
        assertEquals(false, replacement.hasValue)
    }

    @Test
    fun `replace with custom task-id in branch`() {
        mockSettingState(taskIdRegex = CUSTOM_TASK_ID_REGEX)
        mockBranchName(BRANCH_WITH_CUSTOM_TASK_ID)

        val replacer = BranchTaskIdReplacer(projectMock)

        val replacement = runBlocking { replacer.getReplacement(anActionEventMock) }

        assertEquals(CUSTOM_TASK_ID, replacement.value)
        assertEquals(true, replacement.hasValue)
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
