package com.mountaya.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalMountayaMapBoundsTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun noBoundsStandOutsideAStackAndAStackHandsItsOwnToItsControls() {
        var outside: MapBounds? = MapBounds(Rect.Zero, Rect.Zero, null)
        var inside: MapBounds? = null
        compose.setContent {
            outside = LocalMountayaMapBounds.current
            Box(Modifier.size(300.dp)) {
                MapControls(null, zoom = false, compass = false, layers = false, preferences = false) {
                    inside = LocalMountayaMapBounds.current
                }
            }
        }

        compose.runOnIdle {
            assertNull(outside)
            val bounds = assertNotNull(inside, "the map states its bounds")
            assertTrue(bounds.map.width > 0f)
            assertTrue(bounds.safeArea.width > 0f, "the safe area lies within the map")
            assertNull(bounds.keepClear, "a stack over no map reads no organization and keeps nothing clear")
        }
    }
}
