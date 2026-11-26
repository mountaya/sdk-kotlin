package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertTrue

class ControlChipTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aChipStacksItsControlsTopToBottomOnTheControlGroundADiscForTheCompass() {
        compose.setContent {
            Column {
                Drawn("square") {
                    ControlChip {
                        Box(Modifier.size(48.dp))
                        Box(Modifier.size(48.dp))
                    }
                }
                // The disc lies on a black ground, which shows where the disc leaves its corners.
                Drawn(
                    "disc",
                ) {
                    Box(
                        Modifier.background(Color.Black),
                    ) { ControlChip(isRound = true) { Box(Modifier.size(48.dp)) } }
                }
            }
        }

        compose.onNodeWithTag("square").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(96.dp)
        assertTrue(compose.pixel(compose.image("square"), 24.0, 72.0).isNear(color("#ffffff")))
        val disc = compose.image("disc")
        assertTrue(compose.pixel(disc, 24.0, 24.0).isNear(color("#ffffff")))
        assertTrue(compose.pixel(disc, 2.0, 2.0).isNear(color("#000000")), "outside the disc")
    }
}
