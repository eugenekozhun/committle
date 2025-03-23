package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.kozhun.commitmessagetemplate.enums.StringCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BranchTypeReplacerTest : BaseReplacerTest() {
    private var replacer = BranchTypeReplacer(projectMock)

    @Test
    fun `replace empty template with default regex`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("", replacer.replace("", anActionEventMock))
    }

    @Test
    fun `replace empty template with custom regex`() {
        mockSettingState(typeRegex = CUSTOM_TYPE_REGEX)
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("", replacer.replace("", anActionEventMock))
    }

    @Test
    fun `replace non-type branch with empty template`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("Some changes", replacer.replace("Some changes", anActionEventMock))
    }

    @Test
    fun `replace non-type branch with template`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("[]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }


    @Test
    fun `replace with default type`() {
        mockSettingState(typeDefault = TYPE_DEFAULT)
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)
        assertEquals("[$TYPE_DEFAULT]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with type in branch`() {
        mockSettingState()
        mockBranchName(BRANCH_WITH_TYPE)
        assertEquals("[${TYPE}]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }


    @Test
    fun `replace with mismatched type`() {
        mockSettingState(typeRegex = CUSTOM_TYPE_REGEX)
        mockBranchName(BRANCH_WITH_TYPE)
        assertEquals("[]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with custom type in branch`() {
        mockSettingState(typeRegex = CUSTOM_TYPE_REGEX)
        mockBranchName(BRANCH_WITH_CUSTOM_TYPE)
        assertEquals("[${CUSTOM_TYPE}]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with lowercase postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.LOWERCASE)
        mockBranchName("Feature/CMT-123-refactoring")
        assertEquals("[feature]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with UPPERCASE postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.UPPERCASE)
        mockBranchName(BRANCH_WITH_TYPE)
        assertEquals("[FEATURE]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with CAPITALIZE postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.CAPITALIZE)
        mockBranchName(BRANCH_WITH_TYPE)
        assertEquals("[Feature]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    @Test
    fun `replace with NONE postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.NONE)
        mockBranchName(BRANCH_WITH_TYPE)
        assertEquals("[${TYPE}]: Some changes", replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock))
    }

    private companion object {
        const val BRANCH_WITHOUT_TYPE_ID = "master"

        const val TYPE = "feature"
        const val TYPE_DEFAULT = "default"
        const val BRANCH_WITH_TYPE = "$TYPE/CMT-123-refactoring"

        const val CUSTOM_TYPE = "test"
        const val CUSTOM_TYPE_REGEX = "test"
        const val BRANCH_WITH_CUSTOM_TYPE = "$CUSTOM_TYPE/CMT-123-refactoring"
    }
}
