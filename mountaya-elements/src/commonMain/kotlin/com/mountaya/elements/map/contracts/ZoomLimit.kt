package com.mountaya.elements

/**
 * ZoomLimit is one end of a zoom window a call states (`map.camera.zoom-window`): kept as the map holds
 * it, released back to the renderer's own, its floor for the least zoom and its default ceiling for the
 * greatest, or moved to a zoom.
 *
 * ```
 * map.setZoomBounds(min = ZoomLimit.Zoom(8.0), max = ZoomLimit.Zoom(12.0))
 * map.setZoomBounds(max = ZoomLimit.Released)
 * ```
 *
 * A limit left out of a call is kept. Two are equal where they are of one kind, a zoom as the same
 * number, `NaN` the same as `NaN` and 0 another than -0.
 */
public sealed interface ZoomLimit {
    /** Kept leaves the limit where the map holds it. */
    public object Kept : ZoomLimit {
        override fun toString(): String = "Kept"
    }

    /**
     * Released moves the limit back to the renderer's own: the floor under which it draws nothing, or the
     * ceiling it builds a map with.
     */
    public object Released : ZoomLimit {
        override fun toString(): String = "Released"
    }

    /** Zoom moves the limit to [zoom], a finite number. */
    public class Zoom(
        /** The zoom the limit moves to. */
        public val zoom: Double,
    ) : ZoomLimit {
        override fun equals(other: Any?): Boolean = other is Zoom && other.zoom.toBits() == zoom.toBits()

        override fun hashCode(): Int = zoom.toBits().hashCode()

        override fun toString(): String = "Zoom($zoom)"
    }
}
