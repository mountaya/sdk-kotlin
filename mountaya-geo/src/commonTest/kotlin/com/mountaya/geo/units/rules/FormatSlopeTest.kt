package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatSlopeTest {
    private val grade = UnitPreferences("en", slope = RestSchema.UnitSlope.Percentage)
    private val angle = UnitPreferences("en", slope = RestSchema.UnitSlope.Degrees)

    @Test
    fun aGradeIsTheTangentTimesAHundred() {
        assertEquals("22%", formatSlope(12.5, grade))
        assertEquals("100%", formatSlope(45.0, grade))
        assertEquals("-22%", formatSlope(-12.5, grade))
        assertEquals("22 %", formatSlope(12.5, UnitPreferences("fr", slope = RestSchema.UnitSlope.Percentage)))
    }

    @Test
    fun anAngleTakesTheDegreeSignInEveryLanguage() {
        assertEquals("13°", formatSlope(12.5, angle))
        assertEquals("13°", formatSlope(12.5, UnitPreferences("fr")))
        assertEquals("-13°", formatSlope(-12.5, angle))
    }

    @Test
    fun aSlopeThatRoundsToZeroIsLevelAndTakesNoSign() {
        assertEquals("0°", formatSlope(-0.3, angle))
        assertEquals("0%", formatSlope(-0.2, grade))
        assertEquals("", formatSlope(null, grade))
        assertEquals("13°", formatSlope(12.5, UnitPreferences("en", slope = RestSchema.UnitSlope("gradians"))))
    }
}
