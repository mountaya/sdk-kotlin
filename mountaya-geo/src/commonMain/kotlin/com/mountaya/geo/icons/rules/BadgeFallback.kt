package com.mountaya.geo

/**
 * badgeFallback answers the text written in an amenity's badge on [surface] where the installed set
 * cannot draw its glyph: the amenity's [name], in the reader's language, in a table's cell, that name's
 * first letter in capitals in a profile's stack, and null, nothing, beside a label of its own, which says
 * it already (`geo.icons.glyph-fallback`).
 *
 * A badge stands for a service the itinerary carries, so a set with no `amenity-` glyphs never leaves it
 * empty. The first letter is the first character a reader sees, an extended grapheme cluster as the
 * platform's segmentation reads it, never the first code point or code unit: a letter written with a
 * combining accent keeps its accent, and an emoji is never cut in half. It is written in capitals by
 * Unicode's own case mapping, which reads no locale, so `ß` is `SS` for every reader; a name with no
 * character answers the empty text.
 */
public fun badgeFallback(
    name: String,
    surface: BadgeSurface,
): String? =
    when (surface) {
        BadgeSurface.Cell -> name
        BadgeSurface.Stack -> firstGraphemeCluster(name).uppercase()
        BadgeSurface.Labelled -> null
    }
