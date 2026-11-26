package com.mountaya.elements

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextWidthTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun aLongerNameIsWiderNoNameHasNoWidthAndALargerStyleIsWider() {
        var measurer: TextMeasurer? = null
        compose.setContent { measurer = rememberTextMeasurer() }
        val caption = TextStyle(fontSize = 11.sp)
        val title = TextStyle(fontSize = 28.sp)

        compose.runOnIdle {
            val measuring = assertNotNull(measurer, "the composition measures no text")
            assertEquals(0f, textWidth("", measuring, caption))
            assertTrue(textWidth("Col de Balme", measuring, caption) > textWidth("Col", measuring, caption))
            assertTrue(textWidth("Col", measuring, title) > textWidth("Col", measuring, caption))
        }
    }
}
