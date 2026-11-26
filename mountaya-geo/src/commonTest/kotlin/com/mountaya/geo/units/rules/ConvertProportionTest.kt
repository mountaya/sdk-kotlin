package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertProportionTest {
    @Test
    fun aRatioOfOneIsAHundredPercent() {
        assertEquals(50.0, convertProportion(0.5, ProportionUnit.Ratio, ProportionUnit.Percent))
        assertEquals(100.0, convertProportion(1.0, ProportionUnit.Ratio, ProportionUnit.Percent))
        assertEquals(0.07, convertProportion(7.0, ProportionUnit.Percent, ProportionUnit.Ratio))
        assertEquals(100L, PERCENT_PER_RATIO)
    }

    @Test
    fun nothingIsClamped() {
        assertEquals(250.0, convertProportion(2.5, ProportionUnit.Ratio, ProportionUnit.Percent))
        assertEquals(-0.5, convertProportion(-50.0, ProportionUnit.Percent, ProportionUnit.Ratio))
    }

    @Test
    fun aProportionInItsOwnUnitIsReturnedAsItIs() {
        for (unit in ProportionUnit.entries) assertEquals(0.1, convertProportion(0.1, unit, unit))
    }
}
