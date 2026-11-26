package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.ProfileRange
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItinerarySparklineTest {
    @get:Rule
    val compose = createComposeRule()

    private val series = ItineraryDrawing(Itineraries.resolved, null).series
    private val base = color("#dee2e6")

    /** The sparkline of the test itinerary, 120 by 24 density-independent pixels on a black ground. */
    @Composable
    private fun Spark(
        lit: ProfileRange? = null,
        color: String? = null,
    ) {
        Box(Modifier.background(Color.Black)) { ItinerarySparkline(series, Modifier.size(120.dp, 24.dp), lit, color) }
    }

    @Test
    fun theWholeCurveIsDrawnInTheBaseTokenTheLitStretchOverItInItsColour() {
        val firstHalf = ProfileRange(0.0, series.totalDistance / 2)
        compose.setContent {
            Column {
                Drawn("plain") { Spark() }
                Drawn("red") { Spark(firstHalf, "#e03131") }
                Drawn("accent") { Spark(firstHalf) }
            }
        }
        val plain = compose.image("plain")
        val red = compose.image("red")
        val accent = compose.image("accent")

        // Low in the area, a quarter and three quarters across.
        assertTrue(compose.pixel(plain, 30.0, 21.0).isNear(base))
        assertTrue(compose.pixel(plain, 90.0, 21.0).isNear(base))
        assertTrue(compose.pixel(red, 90.0, 21.0).isNear(base), "the stretch not lit is the base")
        assertFalse(compose.pixel(red, 30.0, 21.0).isNear(base), "the lit stretch is tinted")
        assertFalse(
            compose.pixel(red, 30.0, 21.0).isNear(compose.pixel(accent, 30.0, 21.0)),
            "in its colour, else the accent",
        )
        assertTrue(compose.pixel(plain, 60.0, 1.0).isNear(color("#000000")), "nothing over the curve")
    }
}
