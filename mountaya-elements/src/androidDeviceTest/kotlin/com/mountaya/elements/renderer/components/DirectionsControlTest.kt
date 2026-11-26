package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DirectionsControlTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun anActionControlIsLiveWithNoMapAtAllAndShutByItsFlag() {
        var clicks = 0
        compose.setContent {
            Column {
                Drawn("live") { DirectionsControl(onClick = { clicks += 1 }) }
                Drawn("shut") { DirectionsControl(onClick = {}, enabled = false) }
            }
        }

        // The route's lower circle stands at (18, 19) of the glyph's box, its ring drawn.
        assertTrue(compose.pixel(compose.image("live"), 29.0, 32.5).isNear(color("#212529")))
        assertTrue(compose.pixel(compose.image("shut"), 29.0, 32.5).isNear(color("#adb5bd")))
        compose.onAllNodesWithContentDescription("Directions")[0].performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}
