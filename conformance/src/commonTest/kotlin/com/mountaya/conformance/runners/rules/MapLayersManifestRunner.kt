package com.mountaya.conformance

import com.mountaya.client.ExchangeRequest
import com.mountaya.client.generated.TileSchema
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapEvent
import com.mountaya.elements.MapException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent

/**
 * `map.layers.manifest`: how a map reads the layers catalogue, played over an exchange that holds every read
 * until a step settles it: `load` is a call of `loadLayers` nobody waits on, in a coroutine of its own, `answer`
 * a catalogue whose elevation names the read, `fail` a 404, `close` `close`, and `scope` `setLayersOrganization`.
 * A load a closed map refuses, with a `MapException` of kind `Closed`, is `closed`, and one that failed otherwise
 * `failed`. Every call runs on the test's scheduler, and each step settles before the next. `held` is
 * `layersManifest`, and `told` each `MapEvent.LayersChange` the map told, as the read whose catalogue it carries.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapLayersManifestRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val held = MapContentFixtures.Held()
        val map = MapEngine(MapFixtures.client(exchange = held), geolocation = InertGeolocation())
        val told = mutableListOf<FixtureValue>()
        val reading =
            scope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
                map.events().collect { if (it is MapEvent.LayersChange) told += readOf(it.layers) }
            }
        val loads = mutableListOf<FixtureValue>()
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            when (val name = step.text("step")) {
                "load" -> {
                    val index = loads.size
                    loads += FixtureValue.Null
                    scope.launch {
                        loads[index] =
                            try {
                                readOf(map.loadLayers())
                            } catch (refused: MapException) {
                                if (refused.kind != MapException.Kind.Closed) throw refused
                                FixtureValue.Text("closed")
                            } catch (failed: Exception) {
                                FixtureValue.Text("failed")
                            }
                    }
                }

                "answer" -> {
                    val read = wholeOrNull(step, "read") ?: throw AssertionError("an answer names no `read`")
                    held.answer(read, MapContentFixtures.catalogueBody(mark = "read-$read"))
                }

                "fail" -> {
                    held.fail(wholeOrNull(step, "read") ?: throw AssertionError("a failure names no `read`"))
                }

                "scope" -> {
                    map.setLayersOrganization(step.textOrNull("organization"))
                }

                "close" -> {
                    map.close()
                }

                else -> {
                    throw AssertionError("no step is called `$name`")
                }
            }
            scope.runCurrent()
        }
        val reads = held.reads.map { queryOf(it.request) }
        val holding = readOf(map.layersManifest)
        // A load still waiting as the script ends holds nothing, before the reads still out are let go.
        val outcomes = loads.toList()
        held.release()
        scope.runCurrent()
        map.close()
        reading.join()
        return ConformanceOutcome.Value(
            MapFixtures.objectOf(
                "held" to holding,
                "loads" to FixtureValue.Array(outcomes),
                "reads" to FixtureValue.Array(reads),
                "told" to FixtureValue.Array(told.toList()),
            ),
        )
    }

    /** The read whose catalogue [catalogue] is, by the mark its elevation carries, or null for none. */
    private fun readOf(catalogue: TileSchema.Layers?): FixtureValue {
        val mark = catalogue?.elevation?.id ?: return FixtureValue.Null
        val read =
            mark.removePrefix("read-").takeIf { mark.startsWith("read-") }?.toIntOrNull() ?: return FixtureValue.Null
        return FixtureValue.Number(read.toDouble())
    }

    /** The query items of [request], each by its name. */
    private fun queryOf(request: ExchangeRequest): FixtureValue {
        val query = request.url.toString().substringAfter('?', "")
        if (query.isEmpty()) return FixtureValue.Object(emptyList())
        return FixtureValue.Object(
            query.split('&').map { item ->
                val name = item.substringBefore('=')
                name to FixtureValue.Text(item.substringAfter('=', ""))
            },
        )
    }
}
