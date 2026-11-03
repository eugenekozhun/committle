package com.kozhun.commitmessagetemplate.service.settings

import com.kozhun.commitmessagetemplate.storage.SettingsState
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import com.kozhun.commitmessagetemplate.storage.toExportableSettings
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SettingsDraftTest {
    @Test
    fun `import then apply persists imported settings`() {
        val original = settingsState(pattern = "original", typeSynonyms = mapOf("feature" to "feat"))
        val imported = settingsState(pattern = "imported", typeSynonyms = mapOf("bug" to "fix"))
        val persistedState = original
        val storage = storageWithState(persistedState)
        val draft = SettingsDraft(storage)

        draft.loadFrom(imported)

        assertTrue(draft.isModified())
        verify(exactly = 0) { storage.loadState(any()) }

        draft.apply()

        assertSame(persistedState, storage.state)
        assertEquals(imported.toExportableSettings(), persistedState.toExportableSettings())
        assertFalse(draft.isModified())
        verify(exactly = 0) { storage.loadState(any()) }
    }

    @Test
    fun `import then reset leaves persisted settings unchanged`() {
        val original = settingsState(pattern = "original", typeSynonyms = mapOf("feature" to "feat"))
        val imported = settingsState(pattern = "imported", typeSynonyms = mapOf("bug" to "fix"))
        val persistedState = original
        val storage = storageWithState(persistedState)
        val draft = SettingsDraft(storage)

        draft.loadFrom(imported)
        draft.reset()

        assertEquals(original.toExportableSettings(), draft.state.toExportableSettings())
        assertFalse(draft.isModified())
        verify(exactly = 0) { storage.loadState(any()) }
    }

    @Test
    fun `repeated apply keeps increasing persisted state modification count`() {
        val persistedState = settingsState(pattern = "original", typeSynonyms = emptyMap())
        val storage = storageWithState(persistedState)
        val draft = SettingsDraft(storage)

        draft.state.pattern = "first"
        draft.apply()
        val countAfterFirstApply = persistedState.modificationCount

        draft.state.pattern = "second"
        draft.apply()

        assertSame(persistedState, storage.state)
        assertTrue(persistedState.modificationCount > countAfterFirstApply)
    }

    private fun storageWithState(persistedState: SettingsState): SettingsStorage = mockk {
        every { state } answers { persistedState }
    }

    private fun settingsState(pattern: String, typeSynonyms: Map<String, String>) = SettingsState().apply {
        this.pattern = pattern
        this.typeSynonyms = typeSynonyms.toMutableMap()
    }
}
