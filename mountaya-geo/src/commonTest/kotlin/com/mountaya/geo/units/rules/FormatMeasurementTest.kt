package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormatMeasurementTest {
    @Test
    fun theNumberIsRoundedOnItsDecimalValueThenSetInThePattern() {
        assertEquals("0.9 km", formatMeasurement(0.85, 1, "{0} km", "en"))
        assertEquals("2,8 mi/h", formatMeasurement(2.796, 1, "{0} mi/h", "fr"))
        assertEquals("0 m", formatMeasurement(-0.4, 0, "{0} m", "en"))
    }

    /** CLDR groups no Spanish or Italian number under 10,000, and every other language from 1,000. */
    @Test
    fun aNumberIsGroupedAsItsLanguageGroupsIt() {
        assertEquals("1531 m", formatMeasurement(1_531.0, 0, "{0} m", "es"))
        assertEquals("15.310 m", formatMeasurement(15_310.0, 0, "{0} m", "es"))
        assertEquals("1531 m", formatMeasurement(1_531.0, 0, "{0} m", "it-IT"))
        assertEquals("1.531 m", formatMeasurement(1_531.0, 0, "{0} m", "de"))
        assertEquals("1,531 m", formatMeasurement(1_531.0, 0, "{0} m", "en"))
        assertEquals("999 m", formatMeasurement(999.0, 0, "{0} m", "en"))
    }

    @Test
    fun onlyTheImperialSystemIsImperial() {
        assertTrue(isImperial(RestSchema.UnitLength.Imperial))
        assertFalse(isImperial(RestSchema.UnitLength.Metric))
        assertFalse(isImperial(RestSchema.UnitLength("nautical")))
    }

    @Test
    fun aFigureStatedToAFixedPlaceKeepsItsZero() {
        assertEquals("1.0 km", formatMeasurement(1.0, 1, "{0} km", "en", minimumFractionDigits = 1))
        assertEquals("1.2 km", formatMeasurement(1.15, 1, "{0} km", "en", minimumFractionDigits = 1))
    }

    /**
     * A language outside the table keeps its own minimum grouping digits, as ICU writes it, so a Polish 1531
     * is grouped on no platform; the table's own languages group as the table says, region aside.
     */
    @Test
    fun aLanguageOutsideTheTableKeepsItsOwnMinimumGroupingDigits() {
        assertEquals("1531 m", formatMeasurement(1_531.0, 0, "{0} m", "pl"))
        assertEquals(8, formatMeasurement(15_310.0, 0, "{0} m", "pl").length, "a Polish 15,310 is grouped")
        assertEquals("1234 m", formatMeasurement(1_234.0, 0, "{0} m", "es-MX"))
    }
}
