package com.mountaya.elements

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import com.mountaya.geo.ElevationEncoding
import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile
import com.mountaya.geo.READOUT_DETAIL_ZOOM
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

/**
 * `map.readout.open`: the case's script played on a map drawn on a `ScriptedMapSurface`, its readout the one a view
 * hands the engine, left on or turned off as `readout` says. Each step goes through the engine's own entry point, the
 * one MapLibre's renderer calls: a `press` is the long press the engine reads at its `position`, the point the scripted
 * projection draws it at, with the camera at its `zoom`, and is told to the map's listeners whether or not a readout
 * opens; a `tap` is the click the engine reads there; and a `move` is the start of a camera move the renderer reports.
 * An `answer` settles the tile read it numbers with its `pixels`, rows from the north, and a `fail` fails it. The
 * elevation source is the case's own, as the map read it, and every tile read is held until the script settles it. The
 * detail zoom is held to `READOUT_DETAIL_ZOOM`.
 *
 * The answer reads `reads`, the tile each read asked for, and `open`, the readout open once every step is played, its
 * position and its terrain, or null.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object MapReadoutOpenRunner : ConformanceRunner {
    /** A reader of the case's source whose every tile read is held until the script settles it. */
    private class HeldReader(
        private val elevation: ElevationSource,
    ) : ElevationReader {
        val reads = mutableListOf<Pair<ElevationTile.Address, CompletableDeferred<ElevationTile>>>()

        override suspend fun source(): ElevationSource = elevation

        override suspend fun tile(address: ElevationTile.Address): ElevationTile {
            val answer = CompletableDeferred<ElevationTile>()
            reads += address to answer
            return answer.await()
        }
    }

    /** The failure of a tile read the script fails. */
    private class TileFailed : Exception("the scripted tile read failed")

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val detail = case.root("constants")
        if (detail.keys != listOf("detailZoom") || detail.number("detailZoom") != READOUT_DETAIL_ZOOM) {
            throw AssertionError("the fixture declares ${detail.entries}, and the readout reads its terrain from 13")
        }
        val input = case.fields
        val written = input.objectOrNull("source") ?: throw AssertionError("`source` is absent")
        val reader =
            HeldReader(
                ElevationSource(
                    written.textOrNull("encoding")?.let(::ElevationEncoding),
                    written.number("tileSize").toInt(),
                    written.number("zoom").toInt(),
                ),
            )
        val map = MapEngine(MapRunnerFixtures.client(), geolocation = InertGeolocation())
        val surface = ScriptedMapSurface(map)
        // A read the script never settles stays out for good, so the reads run beside the test rather than in it.
        val readout = ReadoutEngine(scope.backgroundScope, reader)
        readout.isEnabled = input.bool("readout")
        map.readout = readout
        val pressesTold = mutableListOf<MapTap>()
        scope.backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            map.events().collect { event -> if (event is MapEvent.LongClick) pressesTold += event.tap }
        }
        var presses = 0
        for (step in input.items("steps")) {
            val stated = step as? FixtureValue.Object ?: throw AssertionError("a step is no object")
            when (val kind = stated.text("step")) {
                "press" -> {
                    val position = fixturePosition(stated["position"], "position")
                    presses += 1
                    map.longClicked(position, position, stated.number("zoom"))
                }

                "answer" -> {
                    val (address, answer) = reader.reads[stated.number("read").toInt()]
                    val rows = stated.items("pixels")
                    answer.complete(ElevationTile(address, rows.size, pixels(rows)))
                }

                "fail" -> {
                    reader.reads[stated.number("read").toInt()].second.completeExceptionally(TileFailed())
                }

                "move" -> {
                    map.cameraMoveStarted()
                }

                "tap" -> {
                    val open = readout.open.value
                    val position = open?.position ?: listOf(0.0, 0.0)
                    surface.click(position, position)
                }

                else -> {
                    throw AssertionError("no step is called `$kind`")
                }
            }
            scope.testScheduler.runCurrent()
        }
        if (pressesTold.size != presses) {
            throw AssertionError("${pressesTold.size} of $presses presses were told to the map's listeners")
        }
        val open = readout.open.value
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "open" to
                        (
                            open?.let {
                                FixtureValue.Object(
                                    listOf(
                                        "position" to
                                            FixtureValue.Array(
                                                it.position.map { number ->
                                                    FixtureValue.Number(number)
                                                },
                                            ),
                                        "terrain" to
                                            (
                                                it.terrain?.let { terrain ->
                                                    FixtureValue.Object(
                                                        listOf(
                                                            "aspect" to
                                                                (
                                                                    terrain.aspect?.let { aspect ->
                                                                        FixtureValue.Number(aspect)
                                                                    }
                                                                        ?: FixtureValue.Null
                                                                ),
                                                            "elevation" to FixtureValue.Number(terrain.elevation),
                                                            "slope" to FixtureValue.Number(terrain.slope),
                                                        ),
                                                    )
                                                } ?: FixtureValue.Null
                                            ),
                                    ),
                                )
                            } ?: FixtureValue.Null
                        ),
                    "reads" to
                        FixtureValue.Array(
                            reader.reads.map { (address, _) ->
                                FixtureValue.Object(
                                    listOf(
                                        "x" to FixtureValue.Number(address.x.toDouble()),
                                        "y" to FixtureValue.Number(address.y.toDouble()),
                                        "z" to FixtureValue.Number(address.z.toDouble()),
                                    ),
                                )
                            },
                        ),
                ),
            ),
        )
    }

    /** The pixels a fixture writes, rows from the north down, each `[R, G, B]`, packed row by row. */
    private fun pixels(rows: List<FixtureValue>): IntArray =
        rows
            .flatMap { row ->
                (row as? FixtureValue.Array)?.items ?: throw AssertionError("a row of pixels is no array")
            }.map { pixel ->
                val channels =
                    (pixel as? FixtureValue.Array)?.items?.map { (it as FixtureValue.Number).value.toInt() }
                        ?: throw AssertionError("a pixel is no array")
                (channels[0] shl 16) or (channels[1] shl 8) or channels[2]
            }.toIntArray()
}
