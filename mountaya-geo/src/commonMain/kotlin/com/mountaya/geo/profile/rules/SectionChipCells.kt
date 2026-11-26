package com.mountaya.geo

/**
 * defaultSectionChipStats is what a section chip states where its caller names nothing, the `defaultStats`
 * `geo/profile-section-chips.json` declares: its leg's length and climb, the two a reader compares two legs on.
 */
public val defaultSectionChipStats: List<SectionChipStat> = listOf(SectionChipStat.Distance, SectionChipStat.Ascent)

/**
 * sectionChipCells is what a section chip under a profile states about its leg, [metrics], in the order [stats]
 * asks (`geo.profile.section-chips`): the leg's own length, climb and loss, a loss stated positive as the wire
 * states it, and its average grade ([sectionGrade]). A figure with no value takes no cell, so a leg of an
 * itinerary with no elevation states its length alone.
 */
public fun sectionChipCells(
    metrics: ItinerarySegmentRowMetrics,
    stats: List<SectionChipStat> = defaultSectionChipStats,
): List<SectionChipCell> =
    stats.mapNotNull { stat ->
        val value =
            when (stat) {
                SectionChipStat.Distance -> metrics.sectionDistance
                SectionChipStat.Ascent -> metrics.sectionAscent
                SectionChipStat.Descent -> metrics.sectionDescent
                SectionChipStat.Grade -> sectionGrade(metrics)
            }
        value?.let { SectionChipCell(stat, it) }
    }
