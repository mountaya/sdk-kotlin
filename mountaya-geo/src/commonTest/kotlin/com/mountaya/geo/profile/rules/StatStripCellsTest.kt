package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatStripCellsTest {
    private val t3 = RestSchema.TrailDifficultyType("T3")
    private val figures =
        ResolvedCharacteristics(
            distance = 24_300.0,
            duration = 32_400.0,
            ascent = 1_531.0,
            descent = 1_408.0,
            altitudeMin = 1_035.0,
            altitudeMax = 1_850.0,
            slopeMin = -15.2,
            slopeMax = 18.5,
            trailDifficultyMin = RestSchema.TrailDifficultyType("T1"),
            trailDifficultyMax = t3,
        )

    @Test
    fun theDefaultFiguresAreTheLengthTheClimbTheLossAndTheHighestPoint() {
        assertEquals(
            listOf(StatStripStat.Distance, StatStripStat.Ascent, StatStripStat.Descent, StatStripStat.Elevation),
            defaultStatStripStats,
        )
        assertEquals(
            listOf(
                StatStripCell.Distance(24_300.0),
                StatStripCell.Ascent(1_531.0),
                StatStripCell.Descent(1_408.0),
                StatStripCell.Elevation(1_850.0),
            ),
            statStripCells(figures, null),
        )
    }

    @Test
    fun everyFigureInTheOrderAsked() {
        assertEquals(
            listOf(
                StatStripCell.Difficulty(t3),
                StatStripCell.Grade(18.5),
                StatStripCell.Elevation(1_850.0),
                StatStripCell.Descent(1_408.0),
                StatStripCell.Ascent(1_531.0),
                StatStripCell.Duration(32_400.0),
                StatStripCell.Distance(24_300.0),
            ),
            statStripCells(figures, null, StatStripStat.entries.reversed()),
        )
    }

    @Test
    fun whileAScrubStandsTheCursorStatesItsDistanceAltitudeAndGrade() {
        val cursor = StatStripCursor(5_000.0, 1_400.0, 6.2)
        assertEquals(
            listOf(
                StatStripCell.Grade(6.2),
                StatStripCell.Elevation(1_400.0),
                StatStripCell.Distance(5_000.0),
                StatStripCell.Ascent(1_531.0),
            ),
            statStripCells(
                figures,
                cursor,
                listOf(StatStripStat.Grade, StatStripStat.Elevation, StatStripStat.Distance, StatStripStat.Ascent),
            ),
        )
        val flat = StatStripCursor(5_000.0, 1_400.0, null)

        assertEquals(
            listOf(StatStripCell.Distance(5_000.0)),
            statStripCells(figures, flat, listOf(StatStripStat.Grade, StatStripStat.Distance)),
        )
    }

    @Test
    fun aFigureWithNoValueTakesNoCellAndNothingAskedStatesNothing() {
        val flat = ResolvedCharacteristics(distance = 24_300.0, slopeMin = -15.2)

        assertEquals(listOf(StatStripCell.Distance(24_300.0)), statStripCells(flat, null))
        val unstated = listOf(StatStripStat.Duration, StatStripStat.Difficulty, StatStripStat.Grade)
        assertTrue(statStripCells(flat, null, unstated).isEmpty())
        assertTrue(statStripCells(figures, null, emptyList()).isEmpty())
    }
}
