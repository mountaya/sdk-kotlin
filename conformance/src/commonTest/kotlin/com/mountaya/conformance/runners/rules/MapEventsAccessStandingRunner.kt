package com.mountaya.conformance

import com.mountaya.client.Url
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.ScriptedMapSurface
import kotlinx.coroutines.test.TestScope
import kotlin.math.floor

/**
 * `map.events.access-standing`: whether a refusal of access stands after each of the case's steps, which the
 * engine's `state.isAccessRequired` reads, and whether that step ended the last one standing, which its
 * `events` tell as `MapEvent.AccessRestored`. Each step is played on the scripted surface, the style holding
 * the sources `held` names as it arrives: a refusal as a renderer's request refused with the step's `status`, a
 * 402 where it states none, for the source named, or none, sent under the credential its `sentUnder` names, which
 * the engine's own rule reads as `MapView`'s renderer hands it, a tile as a tile of its source loading, a style
 * as one loading, and a base map given back as a switch asked for whose document does not arrive, the base map
 * shown before it then loading anew, as `MapView`'s renderer loads it. A click on the canvas at `[-1, -1]` after
 * each step marks where it ends in the events told, which a standing refusal neither starts nor ends.
 */
object MapEventsAccessStandingRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val steps =
            case.fields.items("steps").map {
                it as? FixtureValue.Object
                    ?: throw AssertionError("a step is not an object")
            }
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        val standing = mutableListOf<Boolean>()
        val told =
            MapFixtures.told(scope, map) {
                for (step in steps) {
                    if (step["held"] != null) surface.heldSources = fixtureTexts(step.items("held"), "`held`")
                    when (val kind = step.text("step")) {
                        "refused" -> {
                            surface.failRequest(
                                "https://tiles.test/x",
                                refusal(step),
                                step.textOrNull("source"),
                                superseded = MapFixtures.isSuperseded(step),
                            )
                        }

                        "tile" -> {
                            surface.loadTile(step.text("source"))
                        }

                        "style" -> {
                            surface.loadStyle()
                        }

                        "givenBack" -> {
                            // A switch asked for, its document not arriving, and the base map shown before it given
                            // back and loaded anew.
                            map.setStyle(Url("https://tiles.test/switch-${steps.indexOf(step)}.json"))
                            surface.failStyle()
                            surface.loadStyle()
                        }

                        else -> {
                            throw AssertionError("no step is called `$kind`")
                        }
                    }
                    standing += map.state.value.isAccessRequired
                    surface.click(listOf(-1.0, -1.0), listOf(0.0, 0.0))
                }
            }

        val restored = mutableListOf<Boolean>()
        var restoredInStep = false
        for (event in told) {
            when (event) {
                is MapEvent.Click -> {
                    restored += restoredInStep
                    restoredInStep = false
                }

                is MapEvent.AccessRestored -> {
                    if (restoredInStep) throw AssertionError("an end was told twice")
                    restoredInStep = true
                }

                is MapEvent.AccessRefused -> {
                    continue
                }

                else -> {
                    throw AssertionError("a step told $event")
                }
            }
        }
        if (restored.size !=
            standing.size
        ) {
            throw AssertionError("${standing.size} steps, and ${restored.size} were marked")
        }
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                restored.zip(standing).map { (ended, stands) ->
                    MapFixtures.objectOf(
                        "restored" to FixtureValue.Bool(ended),
                        "standing" to FixtureValue.Bool(stands),
                    )
                },
            ),
        )
    }

    /** The status a `refused` step refuses with: the one it states, a whole number, and a 402 where it states none. */
    private fun refusal(step: FixtureValue.Object): Int {
        val written = step.numberOrNull("status") ?: return UNSTATED_REFUSAL
        if (written != floor(written)) throw AssertionError("a step's `status`, $written, is no status")
        return written.toInt()
    }

    /** The status a `refused` step stating none refuses with, a refusal of the entitlement, which no credential moves. */
    private const val UNSTATED_REFUSAL = 402
}
