package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ReadoutCellTest {
    @Test
    fun twoCellsAreEqualWhereTheyStateOneFigureOfTheSameNumber() {
        assertEquals(ReadoutCell(ReadoutStat.Ascent, 40.0), ReadoutCell(ReadoutStat.Ascent, 40.0))
        assertNotEquals(ReadoutCell(ReadoutStat.Descent, 40.0), ReadoutCell(ReadoutStat.Ascent, 40.0))
        assertNotEquals(ReadoutCell(ReadoutStat.Descent, -0.0), ReadoutCell(ReadoutStat.Descent, 0.0))
        assertEquals(
            ReadoutCell(ReadoutStat.Distance, 400.0).hashCode(),
            ReadoutCell(ReadoutStat.Distance, 400.0).hashCode(),
        )
    }
}
