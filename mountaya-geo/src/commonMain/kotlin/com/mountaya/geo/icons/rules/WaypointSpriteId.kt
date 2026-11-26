package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * WAYPOINT_ICON_PREFIX is the namespace every waypoint's sprite id opens with. A renderer answering a
 * missing image matches on it to tell one of these ids from any other before it reads the rest.
 */
public const val WAYPOINT_ICON_PREFIX: String = "section-icon-"

/**
 * DEFAULT_WAYPOINT_COLOR is the colour a waypoint is drawn in until its author gives it one, a hex colour a
 * paint takes as it is.
 */
public const val DEFAULT_WAYPOINT_COLOR: String = "#3b82f6"

/**
 * WAYPOINT_FALLBACK_GLYPH is the glyph a waypoint is drawn with where it states no icon, or one no icon set
 * answers.
 */
public const val WAYPOINT_FALLBACK_GLYPH: String = "map-pin"

/** defaultWaypointIcon is the icon a new waypoint carries, the pin, which suits every activity. */
public val defaultWaypointIcon: RestSchema.WaypointIcon = RestSchema.WaypointIcon.MapPin

/**
 * waypointSpriteId answers the id of the sprite a waypoint drawn with [icon] in [color] is, as the
 * tile server writes it in its vector tiles: `section-icon-`, the icon as the wire spells it, a
 * hyphen, then the colour with its leading `#` dropped and its letters small, so `hut` in `#3B82F6`
 * is `section-icon-hut-3b82f6`. A pure function of the two, so one waypoint always resolves to one
 * image and two surfaces never disagree about it.
 *
 * One leading `#` is dropped and the ASCII capitals are written small; every other character of the
 * colour is kept as written, so a colour that is no hex colour names a sprite nothing registers
 * rather than another waypoint's.
 */
public fun waypointSpriteId(
    icon: RestSchema.WaypointIcon,
    color: String,
): String = WAYPOINT_ICON_PREFIX + icon.value + "-" + asciiLowercase(color.removePrefix("#"))
