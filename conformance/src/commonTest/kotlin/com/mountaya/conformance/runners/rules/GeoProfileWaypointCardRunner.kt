package com.mountaya.conformance

import com.mountaya.geo.WaypointCardFacts
import com.mountaya.geo.waypointCard
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.waypoint-card`: the card a place of the case's `details` states, for its `units`, a cut-off written
 * as its `cutoffMoment` where it states one. Its words are read from a scope whose every word of the `amenities`
 * and `waypointCard` sections is its own path, so a label that reads as a path is that word, and a service's name
 * that does not is its own value.
 */
object GeoProfileWaypointCardRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val card =
            waypointCard(
                fixtureDetails(input["details"]),
                referencedScope,
                readerUnits(input["units"]),
                fixtureFormatter(input, "cutoffMoment"),
            )
        return ConformanceOutcome.Value(
            objectValue(
                "contact" to FixtureValue.Array(card.contact.map(::lineValue)),
                "fields" to FixtureValue.Array(card.fields.map(::fieldValue)),
                "services" to
                    FixtureValue.Array(
                        card.services.map {
                            objectValue("amenity" to FixtureValue.Text(it.amenity.value), "name" to wordValue(it.name))
                        },
                    ),
                "timing" to FixtureValue.Array(card.timing.map(::lineValue)),
            ),
        )
    }

    /** A card's line, its address and whether it leaves the app only where it states them. */
    private fun lineValue(line: WaypointCardFacts.Line): FixtureValue {
        val entries =
            mutableListOf(
                "label" to wordValue(line.label),
                "value" to FixtureValue.Text(line.value),
            )
        line.href?.let { entries += "href" to FixtureValue.Text(it) }
        if (line.isExternal) entries += "external" to FixtureValue.Bool(true)
        return FixtureValue.Object(entries)
    }
}
