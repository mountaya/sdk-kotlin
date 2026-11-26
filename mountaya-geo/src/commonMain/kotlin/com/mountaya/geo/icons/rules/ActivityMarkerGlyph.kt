package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * ACTIVITY_MARKER_PREFIX is the namespace an activity's silhouette opens with in an icon set, so an
 * activity's slug never names a waypoint's icon and one marker is never drawn as another.
 */
public const val ACTIVITY_MARKER_PREFIX: String = "activity-"

/**
 * activityMarkerGlyph answers the name of the glyph that draws [activity]'s silhouette inside a marker
 * in [set], `activity-` then its slug, `hiking-and-trail` as `activity-hiking-and-trail`, or null where
 * the set holds no such glyph or the slug is empty (`geo.icons.resolution`).
 *
 * An activity is looked up under its prefix alone, so a glyph the set holds under the bare slug is no
 * activity's. Null rather than a fallback: the caller knows what its own marker means and chooses a
 * pin, and an activity whose artwork a set does not carry takes the same path.
 */
public fun activityMarkerGlyph(
    activity: RestSchema.ActivitySlug,
    set: GlyphSet,
): String? {
    if (activity.value.isEmpty()) return null
    val glyph = ACTIVITY_MARKER_PREFIX + activity.value
    return glyph.takeIf { it in set.markup }
}
