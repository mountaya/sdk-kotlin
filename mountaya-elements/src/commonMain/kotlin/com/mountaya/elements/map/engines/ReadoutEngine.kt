package com.mountaya.elements

import com.mountaya.geo.READOUT_DETAIL_ZOOM
import com.mountaya.geo.elevationTilesAt
import com.mountaya.geo.terrainFromElevationTiles
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ReadoutEngine is a map's readout (`map.readout.open`): a right-click or a long press, [press], opens it at its point
 * in place of one already open, unless the host turned the readout off, [isEnabled]; at the detail zoom it reads the
 * elevation tiles `elevationTilesAt` names for the point through [reader], each at once, their reads leaving in the
 * order it answers them, by `x` then `y`, and below it reads nothing. The
 * readout opens at once with what the map holds, and the terrain `terrainFromElevationTiles` reads off the tiles joins
 * it once every tile it needs has answered; a tile that fails leaves it without terrain, and a camera move or a tap on
 * the map, [close], closes it. An answer to a readout since closed or replaced lands nowhere.
 *
 * Its reads run in [scope], the caller's, whose dispatcher is the thread the map is driven from, so an answer lands
 * there; closing a readout or opening another cancels the reads still out for it. It holds no lock, and no work runs
 * per frame: a readout reads once, at its press.
 */
internal class ReadoutEngine(
    private val scope: CoroutineScope,
    private val reader: ElevationReader,
) {
    private val held = MutableStateFlow<OpenReadout?>(null)

    /** The readout open, or null for none. */
    val open: StateFlow<OpenReadout?> = held.asStateFlow()

    /** Whether a press opens a readout, as the host leaves it unless it turns it off. */
    var isEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) close()
        }

    /** How many readouts were opened or closed, which an answer for an earlier one is told apart by. */
    private var opened = 0

    /** The reads out for the readout open. */
    private var reading: Job? = null

    /**
     * Opens the readout at [position], `[longitude, latitude]`, pressed at [point] of the canvas with the camera at
     * [zoom], in place of the one open, and reads its terrain at the detail zoom. A press on a map whose readout is off
     * opens nothing and reads nothing.
     */
    fun press(
        position: List<Double>,
        point: List<Double>,
        zoom: Double,
    ) {
        if (!isEnabled) return
        reading?.cancel()
        reading = null
        opened += 1
        val ticket = opened
        held.value = OpenReadout(position, point, zoom)
        if (!(zoom >= READOUT_DETAIL_ZOOM)) return
        reading =
            scope.launch {
                val source =
                    try {
                        reader.source()
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (failure: Exception) {
                        // A source that cannot be read leaves the readout without terrain, as a tile that fails does.
                        null
                    } ?: return@launch
                val addresses = elevationTilesAt(position, source)
                if (addresses.isEmpty()) return@launch
                val tiles =
                    coroutineScope {
                        addresses
                            .map { address ->
                                // Each read starts where it is asked, so the reads leave in the order of the tiles.
                                async(start = CoroutineStart.UNDISPATCHED) {
                                    try {
                                        reader.tile(address)
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (failure: Exception) {
                                        // A tile that fails leaves the readout without terrain.
                                        null
                                    }
                                }
                            }.awaitAll()
                    }
                if (ticket != opened || tiles.any { it == null }) return@launch
                val terrain = terrainFromElevationTiles(position, source, tiles.filterNotNull()) ?: return@launch
                val shown = held.value ?: return@launch
                held.value = shown.withTerrain(terrain)
            }
    }

    /** Closes the readout open, its reads cancelled, as a camera move or a tap on the map does. */
    fun close() {
        opened += 1
        reading?.cancel()
        reading = null
        held.value = null
    }
}
