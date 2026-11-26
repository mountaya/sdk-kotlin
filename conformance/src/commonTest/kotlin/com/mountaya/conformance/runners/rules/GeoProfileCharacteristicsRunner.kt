package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ProfileCharacteristics
import com.mountaya.geo.profileCharacteristics
import com.mountaya.geo.trailDifficultyRank
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.characteristics`: the summary the case's `segments` and `waypoints` fold into, each
 * built as the REST API's with the wire's fields the fixture writes. The rank table is held to the
 * rank the rule gives every grade the wire names.
 */
object GeoProfileCharacteristicsRunner : ConformanceRunner {
    private val grades =
        listOf(
            RestSchema.TrailDifficultyType.Unknown,
            RestSchema.TrailDifficultyType.Hiking,
            RestSchema.TrailDifficultyType.MountainHiking,
            RestSchema.TrailDifficultyType.DemandingMountainHiking,
            RestSchema.TrailDifficultyType.AlpineHiking,
            RestSchema.TrailDifficultyType.DemandingAlpineHiking,
            RestSchema.TrailDifficultyType.DifficultAlpineHiking,
            RestSchema.TrailDifficultyType.Mtb0,
            RestSchema.TrailDifficultyType.Mtb1,
            RestSchema.TrailDifficultyType.Mtb2,
            RestSchema.TrailDifficultyType.Mtb3,
            RestSchema.TrailDifficultyType.Mtb4,
            RestSchema.TrailDifficultyType.Mtb5,
            RestSchema.TrailDifficultyType.Mtb6,
        )

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "trailDifficultyRank" to
                        FixtureValue.Object(
                            grades.map { it.value to numberOrNull(trailDifficultyRank(it)?.toDouble()) },
                        ),
                ),
        )
        val input = case.fields
        val segments =
            input.items("segments").map {
                fixtureSegment(it as? FixtureValue.Object ?: throw AssertionError("a segment is not an object"))
            }
        val waypoints = input.items("waypoints").map(::timedWaypoint)
        return ConformanceOutcome.Value(characteristicsValue(profileCharacteristics(segments, waypoints)))
    }
}

/** A summary as `geo.profile.characteristics` writes it, every member under its name. */
internal fun characteristicsValue(summary: ProfileCharacteristics): FixtureValue =
    FixtureValue.Object(
        listOf(
            "altitudeMax" to numberOrNull(summary.altitudeMax?.toDouble()),
            "altitudeMin" to numberOrNull(summary.altitudeMin?.toDouble()),
            "ascent" to numberOrNull(summary.ascent?.toDouble()),
            "descent" to numberOrNull(summary.descent?.toDouble()),
            "distance" to numberOrNull(summary.distance),
            "duration" to numberOrNull(summary.duration),
            "durationPartial" to FixtureValue.Bool(summary.durationPartial),
            "slopeMax" to numberOrNull(summary.slopeMax),
            "slopeMin" to numberOrNull(summary.slopeMin),
            "stopDuration" to numberOrNull(summary.stopDuration?.toDouble()),
            "trailDifficultyMax" to textOrNull(summary.trailDifficultyMax?.value),
            "trailDifficultyMin" to textOrNull(summary.trailDifficultyMin?.value),
        ),
    )

/** The waypoint a case writes, holding the `timing` it states and a placeholder for every other member. */
internal fun timedWaypoint(item: FixtureValue): RestSchema.ItineraryWaypoint {
    val waypoint = item as? FixtureValue.Object ?: throw AssertionError("a waypoint is not an object")
    val stop = waypoint.objectOrNull("timing")?.let { wholeOrNull(it, "stop_duration") }
    return fixtureWaypoint(
        timing = waypoint.objectOrNull("timing")?.let { RestSchema.WaypointTiming { stop_duration = stop } },
    )
}
