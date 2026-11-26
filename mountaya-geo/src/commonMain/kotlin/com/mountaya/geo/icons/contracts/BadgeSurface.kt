package com.mountaya.geo

/**
 * BadgeSurface is where an amenity's badge stands, which decides what is written in its place where the
 * installed set cannot draw its glyph (`badgeFallback`).
 */
public enum class BadgeSurface(
    /** The surface as this SDK names it. */
    public val id: String,
) {
    /** A table's cell, with room for the amenity's name. */
    Cell("cell"),

    /** A profile's narrow stack of badges, with room for a letter. */
    Stack("stack"),

    /** A place beside a label of its own, which names the amenity already. */
    Labelled("labelled"),
}
