package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.section-chips`: what a chip states about the case's `leg`, in the order its `stats` ask. The default
 * figures are held to the published ones.
 */
object GeoProfileSectionChipsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(case, mapOf("defaultStats" to profileTexts(defaultSectionChipStats.map { it.id })))
        val input = case.fields
        val leg = input.objectOrNull("leg") ?: throw AssertionError("the case's `leg` is not an object")
        val metrics =
            ItinerarySegmentRowMetrics(
                sectionDistance = leg.numberOrNull("sectionDistance"),
                sectionAscent = leg.numberOrNull("sectionAscent"),
                sectionDescent = leg.numberOrNull("sectionDescent"),
            )
        val stats = profileFigures(input, "stats", SectionChipStat.entries) { it.id } ?: defaultSectionChipStats
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                sectionChipCells(metrics, stats).map {
                    profileObject("key" to FixtureValue.Text(it.stat.id), "value" to FixtureValue.Number(it.value))
                },
            ),
        )
    }
}
