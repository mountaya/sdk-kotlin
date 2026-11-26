package com.mountaya.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PanelSurfaceTest {
    @get:Rule
    val compose = createComposeRule()

    /** A surface holding a heading and a checked row, its mark a block in the row's colour. */
    @Composable
    private fun Panel() {
        PanelSurface(title = null) {
            PanelHeading("Base map")
            PanelRow("Satellite", isChecked = true, onClick = {}) { ink -> Box(Modifier.size(14.dp).background(ink)) }
        }
    }

    @Test
    fun aPanelIsDrawnOnThePanelsGroundEdgedInItsBorderItsTextInItsColour() {
        compose.setContent {
            Column {
                Drawn("light") { Panel() }
                Drawn("dark", dark = true) { Panel() }
            }
        }

        val bounds = compose.onNodeWithTag("light").getBoundsInRoot()
        val width = (bounds.right - bounds.left).value.toDouble()
        val height = (bounds.bottom - bounds.top).value.toDouble()
        assertTrue(width >= 240.0, "$width")
        val light = compose.image("light")
        val dark = compose.image("dark")
        assertTrue(compose.pixel(light, 4.0, 4.0).isNear(color("#ffffff")))
        assertTrue(compose.pixel(dark, 4.0, 4.0).isNear(color("#25262b")))
        // The row's mark is drawn in the panel's text colour, at the row's trailing edge.
        assertTrue(compose.pixel(light, width - 12 - 7, height - 12 - 24).isNear(color("#212529")))
        assertTrue(compose.pixel(dark, width - 12 - 7, height - 12 - 24).isNear(color("#c1c2c5")))
        // Both panels' rows, the light and the dark, read as selected.
        compose.onAllNodesWithText("Satellite").assertCountEquals(2).assertAll(isSelected())
    }

    @Test
    fun aPanelTallerThanItHoldsScrollsWithinItsMostHeightItsGroundStandingStill() {
        val pressed = mutableListOf<Int>()
        compose.setContent {
            Drawn("tall") {
                PanelSurface(title = "Points of interest") {
                    PanelHeading("Categories")
                    for (index in 0 until 30) PanelRow("Row $index", isChecked = false, onClick = { pressed += index })
                }
            }
        }

        // A panel drawn on its own is the embed's width, and stays under its title as its sections scroll.
        compose.onNodeWithTag("tall").assertHeightIsEqualTo(420.dp).assertWidthIsEqualTo(340.dp)
        compose.onNodeWithText("Points of interest").assertIsDisplayed()
        compose.onNodeWithText("Row 29").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(listOf(29), pressed) }
        // The ground stands still under the rows scrolled, its top edge where it was.
        assertTrue(compose.pixel(compose.image("tall"), 4.0, 4.0).isNear(color("#ffffff")))
    }

    @Test
    fun aRowIsThePlatformsTouchTargetHigh() {
        compose.setContent { Drawn("row") { PanelRow("Satellite", isChecked = false, onClick = {}) } }

        val bounds = compose.onNodeWithTag("row").getBoundsInRoot()
        assertTrue((bounds.bottom - bounds.top).value >= 48f)
    }
}
