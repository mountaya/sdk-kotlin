package com.mountaya.elements

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class TextGrowthTest {
    @Test
    fun textGrowsByTheReadersFontScaleAndNeverShrinksTheRowItStandsIn() {
        assertEquals(1f, textGrowth(Density(2f), 11.sp))
        assertEquals(2f, textGrowth(Density(2f, fontScale = 2f), 11.sp))
        assertEquals(1.3f, textGrowth(Density(3f, fontScale = 1.3f), 11.sp), 1e-4f)
        assertEquals(1f, textGrowth(Density(2f, fontScale = 0.85f), 11.sp), "a smaller text leaves the row as it is")
    }
}
