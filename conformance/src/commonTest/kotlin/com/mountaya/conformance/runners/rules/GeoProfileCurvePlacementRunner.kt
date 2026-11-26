package com.mountaya.conformance

import com.mountaya.geo.ProfilePlace
import com.mountaya.geo.ProfileWaypoint
import com.mountaya.geo.WaypointDetails
import com.mountaya.geo.placeOnCurve
import kotlinx.coroutines.test.TestScope
import kotlin.time.Instant

/**
 * `geo.profile.curve-placement`: where the case's `waypoints`, each placed at its distance, stand on its `series`
 * drawn against its `domain` over its `window`: the pins, the dividers and the cut-offs. A place's `pin`,
 * `divides`, `name` and `cutoffAt` are read as the profile's own place states them, and a cut-off is written
 * back as the instant the fixture writes, RFC 3339 in UTC.
 */
object GeoProfileCurvePlacementRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val places =
            input.items("waypoints").map { item ->
                val waypoint = item as? FixtureValue.Object ?: throw AssertionError("a waypoint is not an object")
                val cutoff = waypoint.textOrNull("cutoffAt")
                ProfilePlace(
                    ProfileWaypoint.AtDistance(waypoint.text("id"), waypoint.number("distance")),
                    name = waypoint.textOrNull("name"),
                    divides = (waypoint["divides"] as? FixtureValue.Bool)?.value,
                    isPinned = (waypoint["pin"] as? FixtureValue.Bool)?.value ?: true,
                    details =
                        cutoff?.let {
                            WaypointDetails(
                                timing = WaypointDetails.Timing(cutoffAt = Instant.parse(it)),
                            )
                        },
                )
            }
        val window = fixtureWindow(input["window"]) ?: throw AssertionError("the case states no `window`")
        val placement =
            placeOnCurve(fixtureSeries(input["series"]), null, fixtureDomain(input["domain"]), window, places)
        return ConformanceOutcome.Value(
            objectValue(
                "cutoffs" to
                    FixtureValue.Array(
                        placement.cutoffs.map {
                            objectValue(
                                "cutoffAt" to FixtureValue.Text(it.cutoffAt.toString()),
                                "key" to FixtureValue.Text(it.key),
                                "x" to FixtureValue.Number(it.x),
                            )
                        },
                    ),
                "dividers" to
                    FixtureValue.Array(
                        placement.dividers.map {
                            objectValue(
                                "key" to FixtureValue.Text(it.key),
                                "name" to FixtureValue.Text(it.name),
                                "x" to FixtureValue.Number(it.x),
                                "y" to numberOrNull(it.y),
                            )
                        },
                    ),
                "pins" to
                    FixtureValue.Array(
                        placement.pins.map {
                            objectValue(
                                "altitude" to FixtureValue.Number(it.altitude),
                                "key" to FixtureValue.Text(it.key),
                                "x" to FixtureValue.Number(it.x),
                                "y" to FixtureValue.Number(it.y),
                            )
                        },
                    ),
            ),
        )
    }
}
