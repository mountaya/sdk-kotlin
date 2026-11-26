package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * wayTypeColors is the colour each way type paints a profile's way-type bar in, the `wayTypes`
 * `geo/profile-classification-colors.json` declares: blues for cycle ways, greens for natural paths, oranges and
 * reds for roads and purples for pedestrian ways, a fixed scale rather than theme tokens, so a legend reads the
 * same under every brand.
 */
public val wayTypeColors: Map<RestSchema.WayType, HexColor> =
    mapOf(
        RestSchema.WayType("construction") to HexColor("#f43f5e"),
        RestSchema.WayType("cycleway") to HexColor("#3b82f6"),
        RestSchema.WayType("ferry") to HexColor("#06b6d4"),
        RestSchema.WayType("footway") to HexColor("#8b5cf6"),
        RestSchema.WayType("path") to HexColor("#22c55e"),
        RestSchema.WayType("road") to HexColor("#f97316"),
        RestSchema.WayType("state-road") to HexColor("#ef4444"),
        RestSchema.WayType("steps") to HexColor("#6366f1"),
        RestSchema.WayType("street") to HexColor("#eab308"),
        RestSchema.WayType("track") to HexColor("#a16207"),
        RestSchema.WayType("unknown") to unclassifiedColor,
    )

/**
 * wayTypeColor is the colour a stretch of [wayType] paints a profile's way-type bar in
 * (`geo.profile.classification-colors`), [unclassifiedColor] for one the table does not name, as it is for
 * `unknown`.
 */
public fun wayTypeColor(wayType: RestSchema.WayType): HexColor = wayTypeColors[wayType] ?: unclassifiedColor
