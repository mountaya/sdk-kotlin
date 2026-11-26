package com.mountaya.geo

import com.mountaya.client.generated.TileSchema

/**
 * paintZoomRange answers the zoom window a tile specification paints across, its layers' and never
 * its sources': from the lowest `zoom_min` to the highest `zoom_max` of every layer of every one of
 * [sources]. A source's own zoom range says where its tiles are served, and a layer drawing from it
 * keeps painting past that on overzoomed tiles, so it bounds nothing here.
 *
 * Null where no source holds a layer: nothing paints at any zoom.
 */
public fun paintZoomRange(sources: List<TileSchema.TileSource>): ZoomBounds? {
    var lowest = Int.MAX_VALUE
    var highest = Int.MIN_VALUE
    var painted = false
    for (source in sources) {
        for (layer in source.layers) {
            painted = true
            lowest = minOf(lowest, layer.zoom_min)
            highest = maxOf(highest, layer.zoom_max)
        }
    }
    return if (painted) ZoomBounds(minZoom = lowest.toDouble(), maxZoom = highest.toDouble()) else null
}
