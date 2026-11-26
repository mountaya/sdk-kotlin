package com.mountaya.conformance

import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.ScriptedMapSurface
import kotlinx.coroutines.test.TestScope

/**
 * `map.events.pointer`: what a map tells of the case's gestures, through the engine's `events` on the scripted
 * surface, which plays each `click` as a click and each `contextmenu` as a long click, the features `under`
 * the pointer being what the surface draws there. A gesture the case says an edit took is played while a
 * waypoint is being placed, the edit Kotlin's map takes a gesture with. A `MapEvent.Click` is told as `click`,
 * a `MapEvent.LongClick` as `contextmenu` and a `MapEvent.FeatureClick` as `featureclick`.
 */
object MapEventsPointerRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
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
                    val point = fixturePosition(step["point"], "point")
                    val position = fixturePosition(step["lngLat"], "lngLat")
                    val taken = Scripted.flag(step, "taken")
                    surface.rendered = MapFixtures.features(step, "under")
                    if (taken) map.beginWaypointEdit("taking")
                    when (val gesture = step.text("step")) {
                        "click" -> surface.click(point, position)
                        "contextmenu" -> surface.longClick(point, position)
                        else -> throw AssertionError("no gesture is called `$gesture`")
                    }
                    if (taken) map.endWaypointEdit()
                }
            }
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                told.map { event ->
                    when (event) {
                        is MapEvent.Click -> {
                            MapFixtures.gesture("click", event.tap)
                        }

                        is MapEvent.LongClick -> {
                            MapFixtures.gesture("contextmenu", event.tap)
                        }

                        is MapEvent.FeatureClick -> {
                            MapFixtures.objectOf(
                                "event" to FixtureValue.Text("featureclick"),
                                "features" to FixtureValue.Array(event.features.map(MapFixtures::answer)),
                                "lngLat" to MapFixtures.numbers(event.position),
                            )
                        }

                        else -> {
                            throw AssertionError("a gesture told $event")
                        }
                    }
                },
            ),
        )
    }
}
