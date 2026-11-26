package com.mountaya.elements

/**
 * ScreenProjection is how an edit reads a position on the screen: the map's projection from a
 * position, `[longitude, latitude]`, to the point it is drawn at, `[x, y]` in the screen's
 * density-independent pixels, and back.
 *
 * An edit measures in pixels because its magnet reads alike at every zoom: zoomed in, the same pixels
 * span less ground, so the magnet narrows exactly where the editor is being precise. A pixel is the
 * density-independent one a layout is written in, Android's `dp`, the point iOS draws in and the CSS
 * pixel of the web, so a magnet reaches as far under the finger on every screen.
 *
 * The renderer's own projection is one, which `MapView` hands `MapEngine.placeWaypoint` for each
 * click; a consumer drawing a map of their own hands their renderer's, and a test any pair of
 * functions. It is read on the thread the engine is driven from, at the moment a waypoint is placed.
 */
public class ScreenProjection(
    /** Where a position is drawn on the screen, `[x, y]` in density-independent pixels. */
    public val point: (position: List<Double>) -> List<Double>,
    /** The position drawn at a point of the screen, `[longitude, latitude]`. */
    public val position: (point: List<Double>) -> List<Double>,
)
