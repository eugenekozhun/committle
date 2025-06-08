package com.kozhun.commitmessagetemplate.service.replacer.impl

import com.kozhun.commitmessagetemplate.enums.StringCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BranchTypeReplacerTest : BaseReplacerTest() {
    @Test
    fun `replace empty template with default regex`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("", runBlocking {
            replacer.replace("", anActionEventMock)
        })
    }

    @Test
    fun `replace empty template with custom regex`() {
        mockSettingState(typeRegex = CUSTOM_TYPE_REGEX)
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("", runBlocking {
            replacer.replace("", anActionEventMock)
        })
    }

    @Test
    fun `replace non-type branch with empty template`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("Some changes", runBlocking {
            replacer.replace("Some changes", anActionEventMock)
        })
    }

    @Test
    fun `replace non-type branch with template`() {
        mockSettingState()
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }


    @Test
    fun `replace with default type`() {
        mockSettingState(typeDefault = TYPE_DEFAULT)
        mockBranchName(BRANCH_WITHOUT_TYPE_ID)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[$TYPE_DEFAULT]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }

    @Test
    fun `replace with type in branch`() {
        mockSettingState()
        mockBranchName(BRANCH_WITH_TYPE)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[${TYPE}]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }


    @Test
    fun `replace with mismatched type`() {
        mockSettingState(typeRegex = CUSTOM_TYPE_REGEX)
        mockBranchName(BRANCH_WITH_TYPE)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }

    @Test
    fun `replace with custom type in branch`() {
        mockSettingState(typeRegex = CUSTOM_TYPE_REGEX)
        mockBranchName(BRANCH_WITH_CUSTOM_TYPE)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[${CUSTOM_TYPE}]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }

    @Test
    fun `replace with lowercase postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.LOWERCASE)
        mockBranchName("Feature/CMT-123-refactoring")

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[feature]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }

    @Test
    fun `replace with UPPERCASE postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.UPPERCASE)
        mockBranchName(BRANCH_WITH_TYPE)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[FEATURE]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }

    @Test
    fun `replace with CAPITALIZE postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.CAPITALIZE)
        mockBranchName(BRANCH_WITH_TYPE)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[Feature]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
    }

    @Test
    fun `replace with NONE postprocessor`() {
        mockSettingState(typePostprocessor = StringCase.NONE)
        mockBranchName(BRANCH_WITH_TYPE)

        val replacer = BranchTypeReplacer(projectMock)

        assertEquals("[${TYPE}]: Some changes", runBlocking {
            replacer.replace("[${BranchTypeReplacer.ANCHOR}]: Some changes", anActionEventMock)
        })
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
