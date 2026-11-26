package com.mountaya.conformance

import com.mountaya.geo.InfoRange
import com.mountaya.geo.buildInfoBands
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.info-bands`: the bands the case's `segments` yield on the itinerary's axis. Each
 * segment is built as the REST API's, its `geojson` the fixture's geometry, and its entries are the
 * fixture's `info`, an analysis of no one wire name, handed to the rule as the analysis it reads, each
 * value a word or a number as the fixture writes it, a number that is no number and a negative zero
 * included, and written back the same way.
 */
object GeoProfileInfoBandsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val built =
            case.fields.items("segments").map { item ->
                val fields = item as? FixtureValue.Object ?: throw AssertionError("a segment is not an object")
                fixtureSegment(fields) to
                    fields.items("info").map { entry ->
                        val range =
                            entry as? FixtureValue.Object ?: throw AssertionError("an entry of `info` is not an object")
                        InfoRange<Any>(
                            wholeOrNull(range, "from_index") ?: throw AssertionError("an entry has no `from_index`"),
                            wholeOrNull(range, "to_index") ?: throw AssertionError("an entry has no `to_index`"),
                            when (val value = range["value"]) {
                                is FixtureValue.Text -> value.value
                                is FixtureValue.Number -> value.value
                                else -> throw AssertionError("an entry's `value` is no word and no number")
                            },
                        )
                    }
            }
        // Each segment's entries, found by the segment itself rather than by an equal one.
        val bands = buildInfoBands(built.map { it.first }) { segment -> built.first { it.first === segment }.second }
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                bands.map { band ->
                    FixtureValue.Object(
                        listOf(
                            "endKm" to FixtureValue.Number(band.endKm),
                            "startKm" to FixtureValue.Number(band.startKm),
                            "value" to
                                when (val value = band.value) {
                                    is Double -> FixtureValue.Number(value)
                                    else -> FixtureValue.Text(value as String)
                                },
                        ),
                    )
                },
            ),
        )
    }
}
