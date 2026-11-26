package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SectionChipCellTest {
    @Test
    fun twoCellsAreEqualWhereTheyStateOneFigureOfTheSameNumber() {
        assertEquals(SectionChipCell(SectionChipStat.Grade, 4.0), SectionChipCell(SectionChipStat.Grade, 4.0))
        assertNotEquals(SectionChipCell(SectionChipStat.Ascent, 4.0), SectionChipCell(SectionChipStat.Grade, 4.0))
        assertEquals(
            SectionChipCell(SectionChipStat.Distance, 6_000.0).hashCode(),
            SectionChipCell(SectionChipStat.Distance, 6_000.0).hashCode(),
        )
    }
}
