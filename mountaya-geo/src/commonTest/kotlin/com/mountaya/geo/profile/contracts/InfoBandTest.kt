package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InfoBandTest {
    @Test
    fun twoBandsAreEqualWhenTheyHoldTheSameNumbersAndEqualValues() {
        assertEquals(InfoBand(0.0, 0.77, "asphalt"), InfoBand(0.0, 0.77, "asphalt"))
        assertEquals(InfoBand(0.0, 0.77, "asphalt").hashCode(), InfoBand(0.0, 0.77, "asphalt").hashCode())
        assertNotEquals(InfoBand(0.0, 0.77, "asphalt"), InfoBand(-0.0, 0.77, "asphalt"))
        assertNotEquals(InfoBand(0.0, 0.77, "asphalt"), InfoBand(0.0, 0.77, "gravel"))
        assertEquals("InfoBand(startKm=0.0, endKm=0.77, value=asphalt)", InfoBand(0.0, 0.77, "asphalt").toString())
    }
}
