package com.mountaya.elements

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import com.mountaya.geo.watermarkClearance
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PanelControlTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aToggleOpensItsPanelInPlaceOfAnyOtherInTheStackAndClosesItsOwn() {
        val stack = mutableStateOf<MapPanel?>(null)

        togglePanel(MapPanel.Layers, stack)
        assertEquals(MapPanel.Layers, stack.value)
        togglePanel(MapPanel.Preferences, stack)
        assertEquals(MapPanel.Preferences, stack.value, "one panel open at a time")
        togglePanel(MapPanel("filters"), stack)
        assertEquals(MapPanel("filters"), stack.value, "an app's own panel among them")
        togglePanel(MapPanel("filters"), stack)
        assertNull(stack.value)
    }

    @Test
    fun aPanelIsOpenWhereTheStackHoldsItOpenOrOutOfAnyStackByItsOwnWord() {
        val stack = mutableStateOf<MapPanel?>(MapPanel.Pois)

        assertTrue(isPanelOpen(MapPanel.Pois, stack, alone = false))
        assertFalse(isPanelOpen(MapPanel.Layers, stack, alone = true))
        assertTrue(isPanelOpen(MapPanel.Layers, null, alone = true))
        assertFalse(isPanelOpen(MapPanel.Layers, null, alone = false))
    }

    @Test
    fun aToggleStandingAloneIsDrawnOnAChipOfItsOwnItsGlyphInTheControlColour() {
        compose.setContent {
            Drawn(
                "filters",
            ) { PanelControl(MapPanel("filters"), label = "Filters", glyph = "zoom-in") { BasicText("Huts") } }
        }

        compose.onNodeWithTag("filters").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        val image = compose.image("filters")
        assertTrue(compose.pixel(image, 24.0, 24.0).isNear(color("#212529")))
        assertTrue(compose.pixel(image, 6.0, 24.0).isNear(color("#ffffff")))
    }

    @Test
    fun aPressOpensThePanelBesideItsToggleHeldWhileItIsOpen() {
        compose.setContent {
            PanelControl(
                MapPanel("filters"),
                label = "Filters",
                glyph = "zoom-in",
            ) { BasicText("Huts") }
        }

        compose.onNodeWithText("Huts").assertDoesNotExist()
        compose.onNodeWithContentDescription("Filters").performClick()
        compose.onNodeWithText("Huts").assertExists()
        compose.onNodeWithContentDescription("Filters").assertIsSelected()
    }

    /**
     * A control in no stack knows of no map, so it holds the watermark's frame at the top left of its window's safe area as
     * a waiting read holds it, and its panel, which would reach over the frame beside it, opens below it.
     */
    @Test
    fun aControlInNoStackOpensItsPanelBelowTheFrameAtTheTopLeftOfItsWindow() {
        compose.setContent {
            Box(Modifier.safeDrawingPadding()) {
                PanelControl(MapPanel("filters"), label = "Filters", glyph = "zoom-in") {
                    Box(Modifier.width(300.dp).height(80.dp)) { BasicText("Huts") }
                }
            }
        }
        compose.onNodeWithContentDescription("Filters").performClick()

        val toggle =
            compose
                .onNodeWithContentDescription("Filters")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val panel =
            compose
                .onNodeWithText("Huts")
                .fetchSemanticsNode()
                .positionOnScreen.y
        val below = with(compose.density) { watermarkClearance.dp.toPx() }
        assertTrue(
            panel - toggle >= below - 1f,
            "the panel opens below the mark's frame, at ${panel - toggle} of $below",
        )
    }
}
