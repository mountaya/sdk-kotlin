package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * unclassifiedColor is the neutral grey a stretch no classification names paints a profile's classification bar
 * in, the `unclassified` `geo/profile-classification-colors.json` declares, the one [surfaceColors] and
 * [wayTypeColors] give `unknown` alike.
 */
public val unclassifiedColor: HexColor = HexColor("#94a3b8")

/**
 * surfaceColors is the colour each surface paints a profile's surface bar in, the `surfaces`
 * `geo/profile-classification-colors.json` declares: slate and stone greys for hard surfaces, browns and ambers
 * for loose ground and greens for vegetation, a fixed scale rather than theme tokens, so a legend reads the same
 * under every brand.
 */
public val surfaceColors: Map<RestSchema.SurfaceType, HexColor> =
    mapOf(
        RestSchema.SurfaceType("asphalt") to HexColor("#334155"),
        RestSchema.SurfaceType("cobblestone") to HexColor("#a8a29e"),
        RestSchema.SurfaceType("compacted-gravel") to HexColor("#b45309"),
        RestSchema.SurfaceType("concrete") to HexColor("#64748b"),
        RestSchema.SurfaceType("dirt") to HexColor("#92400e"),
        RestSchema.SurfaceType("fine-gravel") to HexColor("#ca8a04"),
        RestSchema.SurfaceType("grass") to HexColor("#22c55e"),
        RestSchema.SurfaceType("grass-paver") to HexColor("#65a30d"),
        RestSchema.SurfaceType("gravel") to HexColor("#d4a574"),
        RestSchema.SurfaceType("ground") to HexColor("#78716c"),
        RestSchema.SurfaceType("ice") to HexColor("#bfdbfe"),
        RestSchema.SurfaceType("metal") to HexColor("#71717a"),
        RestSchema.SurfaceType("paved") to HexColor("#475569"),
        RestSchema.SurfaceType("paving-stones") to HexColor("#6b7280"),
        RestSchema.SurfaceType("sand") to HexColor("#fbbf24"),
        RestSchema.SurfaceType("unknown") to unclassifiedColor,
        RestSchema.SurfaceType("unpaved") to HexColor("#d97706"),
        RestSchema.SurfaceType("wood") to HexColor("#a16207"),
        RestSchema.SurfaceType("woodchips") to HexColor("#854d0e"),
    )

/**
 * surfaceColor is the colour a stretch of [surface] paints a profile's surface bar in
 * (`geo.profile.classification-colors`), [unclassifiedColor] for one the table does not name, as it is for
 * `unknown`, since the vocabulary is open and a surface a later contract adds still paints.
 */
public fun surfaceColor(surface: RestSchema.SurfaceType): HexColor = surfaceColors[surface] ?: unclassifiedColor
