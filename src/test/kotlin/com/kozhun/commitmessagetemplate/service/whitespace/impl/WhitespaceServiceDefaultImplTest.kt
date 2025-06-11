package com.kozhun.commitmessagetemplate.service.whitespace.impl

import com.intellij.openapi.project.Project
import com.kozhun.commitmessagetemplate.storage.SettingsState
import com.kozhun.commitmessagetemplate.storage.SettingsStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class WhitespaceServiceDefaultImplTest {
    private val projectMock = mockk<Project>()
    private val settingsStorageMock = mockk<SettingsStorage>()
    private val settingsStateMock = mockk<SettingsState>()

    @BeforeEach
    fun setUp() {
        mockkStatic(SettingsStorage::class)
        every { SettingsStorage.getInstance(projectMock) } returns settingsStorageMock
        every { settingsStorageMock.state } returns settingsStateMock
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun mockSettings(
        unnecessaryWhitespaces: Boolean = false,
        trimWhitespacesStart: Boolean = false,
        trimWhitespacesEnd: Boolean = false,
    ) {
        every { settingsStateMock.unnecessaryWhitespaces } returns unnecessaryWhitespaces
        every { settingsStateMock.trimWhitespacesStart } returns trimWhitespacesStart
        every { settingsStateMock.trimWhitespacesEnd } returns trimWhitespacesEnd
    }

    @Test
    fun `format should return original string when all rules disabled`() {
        mockSettings()
        val service = WhitespaceServiceDefaultImpl(projectMock)
        val input = "  Hello  world  "
        assertEquals(input, service.format(input))
    }

    @Test
    fun `format should remove duplicated whitespaces`() {
        mockSettings(unnecessaryWhitespaces = true)
        val service = WhitespaceServiceDefaultImpl(projectMock)
        val input = "Hello   world"
        assertEquals("Hello world", service.format(input))
    }

    @Test
    fun `format should trim whitespaces at the start`() {
        mockSettings(trimWhitespacesStart = true)
        val service = WhitespaceServiceDefaultImpl(projectMock)
        val input = "  Hello world "
        assertEquals("Hello world ", service.format(input))
    }

    @Test
    fun `format should trim whitespaces at the end`() {
        mockSettings(trimWhitespacesEnd = true)
        val service = WhitespaceServiceDefaultImpl(projectMock)
        val input = "Hello world   "
        assertEquals("Hello world", service.format(input))
    }

    @Test
    fun `format should apply all rules`() {
        mockSettings(
            unnecessaryWhitespaces = true,
            trimWhitespacesStart = true,
            trimWhitespacesEnd = true,
        )
        val service = WhitespaceServiceDefaultImpl(projectMock)
        val input = "  Hello   world  "
        assertEquals("Hello world", service.format(input))
    }
}

