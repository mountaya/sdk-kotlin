package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatDistanceTickTest {
    private val metric = UnitPreferences("en")
    private val imperial = UnitPreferences("en", length = RestSchema.UnitLength.Imperial)

    @Test
    fun aStopKeepsTheFractionDigitsItNeedsZerosIncludedAndAWholeStepReadsWhole() {
        assertEquals("0.5 km", formatDistanceTick(DistanceTick(0.5, 500.0, 1), metric))
        assertEquals(
            "1.0 km",
            formatDistanceTick(DistanceTick(1.0, 1_000.0, 1), metric),
            "a stop beside 0.5 km reads as a different number",
        )
        assertEquals("5 km", formatDistanceTick(DistanceTick(5.0, 5_000.0, 0), metric))
        assertEquals("0.25 km", formatDistanceTick(DistanceTick(0.25, 250.0, 2), metric))
    }

    @Test
    fun theReadersSystemNamesTheUnitTheStopAlreadyInIt() {
        assertEquals("2 mi", formatDistanceTick(DistanceTick(2.0, 3_218.688, 0), imperial))
    }

    @Test
    fun theReadersLanguageWritesTheNumberAndTheUnit() {
        val tick = DistanceTick(1.5, 1_500.0, 1)

        assertEquals("1,5\u202Fkm", formatDistanceTick(tick, UnitPreferences("fr-FR")))
        assertEquals("1,5 km", formatDistanceTick(tick, UnitPreferences("de")))
        assertEquals("1.5\u516C\u91CC", formatDistanceTick(tick, UnitPreferences("zh")))
    }
}
