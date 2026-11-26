package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatElevationTest {
    @Test
    fun anElevationIsWholeMetersOrFeet() {
        assertEquals("1,531 m", formatElevation(1_531.0, UnitPreferences("en")))
        assertEquals(
            "5,023 ft",
            formatElevation(1_531.0, UnitPreferences("en", length = RestSchema.UnitLength.Imperial)),
        )
        assertEquals("1 531 m", formatElevation(1_531.0, UnitPreferences("fr")))
        assertEquals(
            "5 023 pi",
            formatElevation(1_531.0, UnitPreferences("fr", length = RestSchema.UnitLength.Imperial)),
        )
    }

    @Test
    fun anElevationThatRoundsToZeroHasNoSignAndAMissingOneIsNothing() {
        assertEquals("0 m", formatElevation(-0.4, UnitPreferences("en")))
        assertEquals("-1 m", formatElevation(-0.5, UnitPreferences("en")))
        assertEquals("", formatElevation(null, UnitPreferences("en")))
        assertEquals("", formatElevation(Double.NEGATIVE_INFINITY, UnitPreferences("en")))
    }
}
