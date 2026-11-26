package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.ScriptedMapSurface
import com.mountaya.geo.LayerRank
import kotlinx.coroutines.test.TestScope

/**
 * `map.events.feature-hover`: what a map tells as a hovering pointer moves over it, through the engine's
 * `events` on the scripted surface, which plays each `move` as a pointer hovering at its point, the features
 * `under` it being what the surface draws there, each `frame` as the next frame drawn and each `leave` as the
 * pointer leaving the canvas. The bands a hover reads are held to `LayerRank`'s.
 */
object MapEventsFeatureHoverRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "bands" to
                        MapFixtures.objectOf(
                            "itineraryLine" to FixtureValue.Number(LayerRank.ItineraryLine.rank.toDouble()),
                            "itineraryPoint" to FixtureValue.Number(LayerRank.ItineraryPoint.rank.toDouble()),
                        ),
                ),
        )
        val input = case.fields
        val steps =
            input.items("steps").map {
                it as? FixtureValue.Object
                    ?: throw AssertionError("a step is not an object")
            }
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        MapFixtures.place(input, map)
        val told =
            MapFixtures.told(scope, map) {
                for (step in steps) {
                    when (val kind = step.text("step")) {
                        "move" -> {
                            surface.rendered = MapFixtures.features(step, "under")
                            surface.hover(
                                fixturePosition(step["point"], "point"),
                                fixturePosition(step["lngLat"], "lngLat"),
                            )
                        }

                        "frame" -> {
                            surface.drawFrame()
                        }

                        "leave" -> {
                            surface.leave(
                                fixturePosition(step["point"], "point"),
                                fixturePosition(step["lngLat"], "lngLat"),
                            )
                        }

                        else -> {
                            throw AssertionError("no step is called `$kind`")
                        }
                    }
                }
            }
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                told.map { event ->
                    if (event !is MapEvent.FeatureHover) throw AssertionError("a hover told $event")
                    MapFixtures.objectOf(
                        "features" to FixtureValue.Array(event.features.map(MapFixtures::answer)),
                        "lngLat" to MapFixtures.numbers(event.position),
                    )
                },
            ),
        )
    }
}
