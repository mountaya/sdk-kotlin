package com.example.trail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.mountaya.geo.watermarkClearance
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

/** Trail's sheet, which never rises into the Mountaya watermark's frame however tall it grows. */
class SheetPlacementTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun theSheetsTopIsCappedBelowTheWatermarksFrame() {
        compose.setContent {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .sheetPlacement()
                        .fillMaxWidth()
                        .height(5000.dp)
                        .testTag("sheet"),
                )
            }
        }

        val sheet = compose.onNodeWithTag("sheet").fetchSemanticsNode().boundsInRoot
        // The frame's bottom edge below the safe area's top, which the SDK publishes for a host's own interface.
        val bottom = with(compose.density) { watermarkClearance.dp.toPx() }
        assertTrue(sheet.top >= bottom, "the sheet rises to ${sheet.top}, into the frame ending at $bottom")
    }
}
