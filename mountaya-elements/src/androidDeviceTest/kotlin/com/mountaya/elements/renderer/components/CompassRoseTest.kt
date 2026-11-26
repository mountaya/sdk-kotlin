package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.GlyphDrawing
import com.mountaya.geo.HexColor
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.generated.ElementTokens
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompassRoseTest {
    @get:Rule
    val compose = createComposeRule()

    private val needle = color("#e9501e")

    @Test
    fun theRosesNeedlePointsNorthAndTurnsAgainstTheCamerasBearing() {
        compose.setContent {
            Column {
                Drawn("north") { CompassRose(bearing = 0.0, size = 32.dp) }
                Drawn("south") { CompassRose(bearing = 180.0, size = 32.dp) }
            }
        }

        val north = compose.image("north")
        val south = compose.image("south")
        assertTrue(compose.pixel(north, 16.0, 9.0).isNear(needle))
        assertFalse(compose.pixel(north, 16.0, 23.0).isNear(needle))
        assertTrue(compose.pixel(south, 16.0, 23.0).isNear(needle))
    }

    @Test
    fun theRosesDiscIsTheControlGroundUnderTheSchemeInForce() {
        compose.setContent { Drawn("dark", dark = true) { CompassRose(bearing = 0.0, size = 32.dp) } }

        assertTrue(compose.pixel(compose.image("dark"), 6.0, 16.0).isNear(color("#25262b")))
        assertEquals(
            hexColor(HexColor("#e9501e")),
            compassPaintColor(GlyphDrawing.Paint.Color(HexColor("#e9501e")), ElementTokens.standard, ThemeScheme.Dark),
        )
    }
}
