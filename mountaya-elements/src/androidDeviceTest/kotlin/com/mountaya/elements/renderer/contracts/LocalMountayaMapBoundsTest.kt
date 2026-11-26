package com.mountaya.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.mountaya.geo.watermarkHeight
import com.mountaya.geo.watermarkInset
import com.mountaya.geo.watermarkWidth
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
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
            // A stack driving no map holds the watermark's frame as a waiting read does (SPEC.md section 8), at its
            // safe area's top-left corner moved by the inset, so its controls do not move as a map's mark lands.
            val clear = assertNotNull(bounds.keepClear, "a stack over no map holds the frame as a waiting read does")
            val density = compose.density.density
            assertEquals(bounds.safeArea.left + (watermarkInset * density).toFloat(), clear.left, 0.5f)
            assertEquals(bounds.safeArea.top + (watermarkInset * density).toFloat(), clear.top, 0.5f)
            assertEquals((watermarkWidth * density).toFloat(), clear.width, 0.5f)
            assertEquals((watermarkHeight * density).toFloat(), clear.height, 0.5f)
        }
    }
}
