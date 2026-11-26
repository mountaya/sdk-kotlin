package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class StatStripCellTest {
    private val t3 = RestSchema.TrailDifficultyType("T3")

    @Test
    fun aCellNamesItsFigureAndItsNumberTheDifficultyNone() {
        assertEquals(StatStripStat.Distance, StatStripCell.Distance(24_300.0).stat)
        assertEquals(18.5, StatStripCell.Grade(18.5).number)
        assertEquals(StatStripStat.Difficulty, StatStripCell.Difficulty(t3).stat)
        assertNull(StatStripCell.Difficulty(t3).number)
    }

    @Test
    fun twoCellsAreEqualWhereTheyStateOneFigureOfOneValue() {
        assertEquals<StatStripCell>(StatStripCell.Ascent(1_531.0), StatStripCell.Ascent(1_531.0))
        assertNotEquals<StatStripCell>(StatStripCell.Descent(1_531.0), StatStripCell.Ascent(1_531.0))
        assertNotEquals<StatStripCell>(StatStripCell.Ascent(-0.0), StatStripCell.Ascent(0.0))
        assertEquals<StatStripCell>(StatStripCell.Difficulty(t3), StatStripCell.Difficulty(t3))
        assertNotEquals<StatStripCell>(
            StatStripCell.Difficulty(RestSchema.TrailDifficultyType("t3")),
            StatStripCell.Difficulty(t3),
        )
        assertEquals(StatStripCell.Elevation(1_850.0).hashCode(), StatStripCell.Elevation(1_850.0).hashCode())
    }
}
