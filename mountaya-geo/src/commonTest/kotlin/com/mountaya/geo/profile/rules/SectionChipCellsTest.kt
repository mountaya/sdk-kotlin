package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SectionChipCellsTest {
    private val leg =
        ItinerarySegmentRowMetrics(sectionDistance = 6_000.0, sectionAscent = 450.0, sectionDescent = 30.0)

    @Test
    fun theDefaultFiguresAreTheLegsLengthAndClimb() {
        assertEquals(listOf(SectionChipStat.Distance, SectionChipStat.Ascent), defaultSectionChipStats)
        assertEquals(listOf(6_000.0, 450.0), sectionChipCells(leg).map { it.value })
    }

    @Test
    fun theFiguresInTheOrderAskedAMissingOneAndAMissingGradeTakingNoCell() {
        assertEquals(
            listOf(SectionChipStat.Grade, SectionChipStat.Distance),
            sectionChipCells(leg, listOf(SectionChipStat.Grade, SectionChipStat.Distance)).map { it.stat },
        )
        val partial = ItinerarySegmentRowMetrics(sectionDistance = 6_000.0, sectionAscent = 450.0)

        assertEquals(
            listOf(SectionChipStat.Distance, SectionChipStat.Ascent),
            sectionChipCells(partial, SectionChipStat.entries).map { it.stat },
        )
        assertTrue(sectionChipCells(ItinerarySegmentRowMetrics(), SectionChipStat.entries).isEmpty())
    }
}
