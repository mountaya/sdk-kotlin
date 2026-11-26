package com.mountaya.geo

/**
 * SearchPickCamera is how a map's camera moves onto a picked search result (`geo.search.pick`): it frames an
 * extent, or it flies to a point at a zoom.
 *
 * A box is `[west, south, east, north]`, written as the result states it, its west edge east of its east one
 * where it crosses the antimeridian, and a position `[longitude, latitude]`. Two are equal where they are of one
 * kind holding the same numbers, `NaN` the same as `NaN` and 0 another than -0.
 */
public sealed interface SearchPickCamera {
    /** Fit frames [bounds] whole (`geo.camera.fit`). */
    public class Fit(
        /** The box framed. */
        public val bounds: List<Double>,
    ) : SearchPickCamera {
        override fun equals(other: Any?): Boolean = other is Fit && other.bounds == bounds

        override fun hashCode(): Int = bounds.hashCode()

        override fun toString(): String = "SearchPickCamera.Fit(bounds=$bounds)"
    }

    /** Fly flies to [center] at [zoom]. */
    public class Fly(
        /** The position the camera flies to. */
        public val center: List<Double>,
        /** The zoom it flies to. */
        public val zoom: Double,
    ) : SearchPickCamera {
        override fun equals(other: Any?): Boolean =
            other is Fly && other.center == center && sameNumber(other.zoom, zoom)

        override fun hashCode(): Int = center.hashCode() * 31 + zoom.hashCode()

        override fun toString(): String = "SearchPickCamera.Fly(center=$center, zoom=$zoom)"
    }
}
