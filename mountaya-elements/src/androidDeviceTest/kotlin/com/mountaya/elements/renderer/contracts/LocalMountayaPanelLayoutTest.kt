package com.mountaya.elements

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.mountaya.geo.PanelLayout
import com.mountaya.geo.panelLayoutOf
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalMountayaPanelLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aPanelOnItsOwnHoldsNoLayoutAndTheOneProvidedReachesItsContent() {
        val laid = panelLayoutOf(1024.0, 520.0, 92.0)
        var alone: PanelLayout? = laid
        var provided: PanelLayout? = null
        compose.setContent {
            alone = LocalMountayaPanelLayout.current
            CompositionLocalProvider(LocalMountayaPanelLayout provides laid) {
                provided =
                    LocalMountayaPanelLayout.current
            }
        }

        compose.runOnIdle {
            assertNull(alone)
            assertEquals(laid, provided)
        }
    }
}
