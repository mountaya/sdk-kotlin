package com.mountaya.geo

/**
 * placeGlyph answers the glyph a place is marked with in [set]: its own [glyph] where the set draws it,
 * the fallback pin, [WAYPOINT_FALLBACK_GLYPH], where the set cannot draw it or the place names none, and
 * null, nothing at all, where the set cannot draw the pin either (`geo.icons.glyph-fallback`).
 *
 * A host chooses the set, and a set may hold less than the vocabulary, a licensed one built before an
 * icon was added, so a place is still marked rather than left blank. An amenity's badge falls back to
 * words instead (`badgeFallback`), and a control's glyph to nothing.
 */
public fun placeGlyph(
    glyph: String?,
    set: GlyphSet,
): String? {
    if (glyph != null && glyph in set.markup) return glyph
    return WAYPOINT_FALLBACK_GLYPH.takeIf { it in set.markup }
}
