package com.mountaya.elements

import com.mountaya.geo.TerrainReading

/**
 * OpenReadout is the readout open on a map (`map.readout.open`): the place it was opened at, the point of the canvas
 * pressed, the camera's zoom then, and the terrain read off the elevation tiles there, null until every tile it needs
 * has answered, and for good where one failed or the zoom was below the detail zoom.
 */
internal class OpenReadout(
    /** The place pressed, `[longitude, latitude]`. */
    val position: List<Double>,
    /** The point of the canvas pressed, `[x, y]` in density-independent pixels. */
    val point: List<Double>,
    /** The camera's zoom when it opened. */
    val zoom: Double,
    /** The terrain at the place, or null where none is read. */
    val terrain: TerrainReading? = null,
) {
    /** This readout with [terrain] read. */
    fun withTerrain(terrain: TerrainReading): OpenReadout = OpenReadout(position, point, zoom, terrain)
}
