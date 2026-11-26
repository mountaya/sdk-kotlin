package com.mountaya.geo

/**
 * specificationBounds is the box a collection is framed on (`geo.camera.content-bounds`): the `bbox` its
 * tile specification states, [bbox], read in the form every other box of the geo layer takes.
 *
 * The Tile API writes a box across the antimeridian as RFC 7946 writes one, its west edge east of its
 * east one, `[179.8, -17, -179.9, -16]`; such a box is read as `[west, south, east + 360, north]`,
 * `[179.8, -17, 180.1, -16]`, 0.3 degrees wide, as `boundsOf` frames one, where `normalizeBoundingBox`
 * handed it as received would read a box 359.7 degrees wide and a camera fitted or locked to it would
 * show the whole world. Any other box is answered as written.
 *
 * Null where the specification states no box, which frames nothing, and where the box it states is not
 * four numbers.
 */
public fun specificationBounds(bbox: List<Double>?): List<Double>? {
    if (bbox == null || bbox.size != 4) return null
    val (west, south, east, north) = bbox
    return if (west > east) listOf(west, south, east + DEGREES_PER_TURN, north) else bbox
}
