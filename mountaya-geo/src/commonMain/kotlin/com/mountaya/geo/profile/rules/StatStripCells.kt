package com.mountaya.geo

/**
 * defaultStatStripStats is what the stat strip states where its caller names nothing, the `defaultStats`
 * `geo/profile-stat-strip.json` declares: the itinerary's length, its climb, its loss and its highest point. The
 * steepest grade, read beside three totals, would read as an average, and no geometry implies a duration or a
 * difficulty.
 */
public val defaultStatStripStats: List<StatStripStat> =
    listOf(StatStripStat.Distance, StatStripStat.Ascent, StatStripStat.Descent, StatStripStat.Elevation)

/**
 * statStripCells is what the stat strip over a profile states, in the order [stats] asks
 * (`geo.profile.stat-strip`): the itinerary's length, its duration, its climb and its loss, its highest altitude,
 * its steepest grade and its hardest difficulty, as [characteristics] states them. While a scrub stands,
 * [cursor], the distance, the altitude and the grade are the cursor's own, never the itinerary's in their place,
 * so a cursor where no grade reads states no grade. A figure with no value takes no cell, so an itinerary with
 * no elevation states no climb rather than a climb of zero.
 */
public fun statStripCells(
    characteristics: ResolvedCharacteristics,
    cursor: StatStripCursor?,
    stats: List<StatStripStat> = defaultStatStripStats,
): List<StatStripCell> =
    stats.mapNotNull { stat ->
        when (stat) {
            StatStripStat.Distance -> {
                (cursor?.distance ?: characteristics.distance)?.let { StatStripCell.Distance(it) }
            }

            StatStripStat.Duration -> {
                characteristics.duration?.let { StatStripCell.Duration(it) }
            }

            StatStripStat.Ascent -> {
                characteristics.ascent?.let { StatStripCell.Ascent(it) }
            }

            StatStripStat.Descent -> {
                characteristics.descent?.let { StatStripCell.Descent(it) }
            }

            StatStripStat.Elevation -> {
                (cursor?.elevation ?: characteristics.altitudeMax)?.let { StatStripCell.Elevation(it) }
            }

            StatStripStat.Grade -> {
                (
                    if (cursor ==
                        null
                    ) {
                        characteristics.slopeMax
                    } else {
                        cursor.grade
                    }
                )?.let { StatStripCell.Grade(it) }
            }

            StatStripStat.Difficulty -> {
                characteristics.trailDifficultyMax?.let { StatStripCell.Difficulty(it) }
            }
        }
    }
