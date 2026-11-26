package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.pixel
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DownloadControlTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun anActionControlIsLiveWithNoMapAtAllItsHandlerTheOneItDrives() {
        var clicks = 0
        compose.setContent {
            Column {
                Drawn("live") { DownloadControl(onClick = { clicks += 1 }) }
                Drawn("shut") { DownloadControl(onClick = {}, enabled = false) }
            }
        }

        compose.onNodeWithTag("live").assertWidthIsEqualTo(48.dp).assertHeightIsEqualTo(48.dp)
        // The download arrow's shaft runs down the middle of the glyph.
        assertTrue(compose.pixel(compose.image("live"), 24.0, 22.0).isNear(color("#212529")))
        assertTrue(compose.pixel(compose.image("shut"), 24.0, 22.0).isNear(color("#adb5bd")))
        compose.onAllNodesWithContentDescription("Download")[0].performClick()
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}
