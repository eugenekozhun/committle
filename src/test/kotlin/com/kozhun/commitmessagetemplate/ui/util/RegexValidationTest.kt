package com.kozhun.commitmessagetemplate.ui.util

import com.intellij.ui.components.fields.ExpandableTextField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RegexValidationTest {
    @Test
    fun `blank regular expression is valid`() {
        assertNull(validateRegex(field("")))
        assertNull(validateRegex(field("   ")))
    }

    @Test
    fun `valid regular expression has no validation error`() {
        assertNull(validateRegex(field("[A-Z]+-[0-9]+")))
    }

    @Test
    fun `invalid regular expression is reported on its field`() {
        val field = field("[unclosed")

        val validationInfo = validateRegex(field)

        assertEquals(field, validationInfo?.component)
    }

    private fun field(text: String): ExpandableTextField {
        val field = io.mockk.mockk<ExpandableTextField>()
        io.mockk.every { field.text } returns text
        return field
    }
}
