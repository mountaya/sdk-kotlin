package com.mountaya.geo

/** The namespace a restricted stretch's glyph opens with in the marker set. */
internal const val ROAD_ACCESS_GLYPH_PREFIX: String = "road-access-"

/**
 * roadAccessGlyph answers the name of the glyph a restricted stretch of [kind] is marked with, the
 * warning its icon carries: `road-access-` then the kind as the contract spells it, so `permit` is
 * `road-access-permit` and `local-access` is `road-access-local-access` (`geo.icons.resolution`). The
 * marker set holds one for every kind.
 */
public fun roadAccessGlyph(kind: RoadAccessKind): String = ROAD_ACCESS_GLYPH_PREFIX + kind.id
