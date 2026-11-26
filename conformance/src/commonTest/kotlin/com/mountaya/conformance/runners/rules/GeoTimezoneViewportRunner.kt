package com.mountaya.conformance

import com.mountaya.geo.MapViewport
import com.mountaya.geo.timeZoneViewport
import kotlinx.coroutines.test.TestScope

/**
 * `geo.timezone.viewport`: the viewport the case's `timeZone` opens on, or null where it answers none.
 *
 * The fixture's table is held to what [timeZoneViewport] answers for every name it holds: each zone of
 * `zoneViewports` answers its viewport, and each name of `reportedZoneNames` answers the viewport of the
 * zone it stands for, written back as that zone's name where it does and as itself where it does not.
 */
object GeoTimezoneViewportRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val constants = case.root("constants")
        val zones =
            constants.objectOrNull("zoneViewports") ?: throw AssertionError("the fixture declares no `zoneViewports`")
        val reported =
            constants.objectOrNull("reportedZoneNames")
                ?: throw AssertionError("the fixture declares no `reportedZoneNames`")
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "zoneViewports" to
                        FixtureValue.Object(zones.keys.map { name -> name to written(timeZoneViewport(name)) }),
                    "reportedZoneNames" to
                        FixtureValue.Object(
                            reported.entries.map { (name, zone) ->
                                val standing =
                                    (zone as? FixtureValue.Text)?.value
                                        ?: throw AssertionError("`$name` stands for no zone")
                                val answered = timeZoneViewport(name)
                                name to
                                    FixtureValue.Text(
                                        if (answered != null &&
                                            answered == timeZoneViewport(standing)
                                        ) {
                                            standing
                                        } else {
                                            name
                                        },
                                    )
                            },
                        ),
                ),
        )
        return ConformanceOutcome.Value(written(timeZoneViewport(case.fields.text("timeZone"))))
    }

    /** [viewport] as the fixture writes one, `{center, zoom}`, or null for none. */
    private fun written(viewport: MapViewport?): FixtureValue =
        viewport?.let {
            FixtureValue.Object(
                listOf(
                    "center" to FixtureValue.Array(it.center.map { coordinate -> FixtureValue.Number(coordinate) }),
                    "zoom" to FixtureValue.Number(it.zoom),
                ),
            )
        } ?: FixtureValue.Null
}
