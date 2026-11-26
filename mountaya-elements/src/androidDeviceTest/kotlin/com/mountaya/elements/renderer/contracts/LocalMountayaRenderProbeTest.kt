package com.mountaya.elements

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LocalMountayaRenderProbeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aComposableUnderNoProbeReadsNoneAndOneATestProvidesIsReadAndCountsWhatReportsToIt() {
        val probe = RenderProbe()
        var unprovided: RenderProbe? = probe
        var provided: RenderProbe? = null
        compose.setContent {
            unprovided = LocalMountayaRenderProbe.current
            CompositionLocalProvider(LocalMountayaRenderProbe provides probe) {
                provided = LocalMountayaRenderProbe.current
                PanelRow("Satellite", isChecked = false, onClick = {})
            }
            PanelRow("Hillshade", isChecked = false, onClick = {})
        }

        compose.runOnIdle {
            assertNull(unprovided)
            assertSame(probe, provided)
            assertTrue(probe["PanelRow Satellite"] >= 1, "${probe.counts}")
            assertEquals(0, probe["PanelRow Hillshade"], "a row under no probe reports nothing")
        }
    }
}
