package com.kozhun.commitmessagetemplate.e2e

import org.junit.jupiter.api.Test

class PluginStartupTest : CommittleIdeTest() {
    @Test
    fun `plugin starts in the latest supported IDE`() {
        runIde("pluginStartup") {}
    }
}
