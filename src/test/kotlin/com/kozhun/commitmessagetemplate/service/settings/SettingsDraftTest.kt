package com.kozhun.commitmessagetemplate.service.settings

import com.kozhun.commitmessagetemplate.storage.SettingsState
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.storage.toExportableSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsDraftTest {
    @Test
    fun `import then apply persists imported settings`() {
        val original = settingsState(pattern = "original", typeSynonyms = mapOf("feature" to "feat"))
        val imported = settingsState(pattern = "imported", typeSynonyms = mapOf("bug" to "fix"))
        var persistedState = original
        val storage = mockk<SettingsStorage> {
            every { state } answers { persistedState }
            every { loadState(any()) } answers { persistedState = firstArg() }
        }
        val draft = SettingsDraft(storage)

        draft.loadFrom(imported)

        assertTrue(draft.isModified())
        verify(exactly = 0) { storage.loadState(any()) }

        draft.apply()

        assertEquals(imported.toExportableSettings(), persistedState.toExportableSettings())
        assertFalse(draft.isModified())
    }

    @Test
    fun `import then reset leaves persisted settings unchanged`() {
        val original = settingsState(pattern = "original", typeSynonyms = mapOf("feature" to "feat"))
        val imported = settingsState(pattern = "imported", typeSynonyms = mapOf("bug" to "fix"))
        val storage = mockk<SettingsStorage> {
            every { state } returns original
        }
        val draft = SettingsDraft(storage)

        draft.loadFrom(imported)
        draft.reset()

        assertEquals(original.toExportableSettings(), draft.state.toExportableSettings())
        assertFalse(draft.isModified())
        verify(exactly = 0) { storage.loadState(any()) }
    }

    private fun settingsState(pattern: String, typeSynonyms: Map<String, String>) = SettingsState().apply {
        this.pattern = pattern
        this.typeSynonyms = typeSynonyms.toMutableMap()
    }
}
