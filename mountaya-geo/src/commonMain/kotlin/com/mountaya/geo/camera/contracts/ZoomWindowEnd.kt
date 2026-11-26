package com.mountaya.geo

/**
 * ZoomWindowEnd is which end of its zoom window a camera stands at (`geo.camera.zoom-limit-reached`):
 * neither, its floor, its ceiling, or both in a window of one zoom, each carrying the contract's spelling.
 */
public enum class ZoomWindowEnd(
    /** The end as `geo/zoom-limit-reached.json` spells it. */
    public val id: String,
) {
    /** A camera inside its window, a fraction from either end included. */
    None("none"),

    /** A camera at its window's floor or below it. */
    Floor("floor"),

    /** A camera at its window's ceiling or above it. */
    Ceiling("ceiling"),

    /** A camera in a window of one zoom, at its floor and its ceiling at once. */
    Both("both"),
}
