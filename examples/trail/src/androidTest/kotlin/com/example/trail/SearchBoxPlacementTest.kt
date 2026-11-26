package com.example.trail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.mountaya.geo.watermarkClearance
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

/** Trail's search box over its full-bleed map, which keeps the Mountaya watermark's frame unobstructed. */
class SearchBoxPlacementTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theSearchBoxStartsBelowTheWatermarksFrame() {
        compose.setContent {
            Box(Modifier.fillMaxSize()) {
                // As wide as the row the placement leaves it, as the search field is: a box of no width has no bounds.
                Box(
                    Modifier
                        .searchBoxPlacement()
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("search"),
                )
            }
        }

        val box = compose.onNodeWithTag("search").fetchSemanticsNode().boundsInRoot
        // The frame's bottom edge below the safe area's top, which the SDK publishes for a host's own interface.
        val bottom = with(compose.density) { watermarkClearance.dp.toPx() }
        assertTrue(box.top >= bottom, "the search box starts at ${box.top}, over the frame's bottom at $bottom")
    }
}
