package com.mountaya.conformance

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ACTIVITY_MARKER_PREFIX
import com.mountaya.geo.DEFAULT_WAYPOINT_COLOR
import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.WAYPOINT_FALLBACK_GLYPH
import com.mountaya.geo.WAYPOINT_ICON_PREFIX
import com.mountaya.geo.activityMarkerGlyph
import com.mountaya.geo.amenityGlyph
import com.mountaya.geo.defaultWaypointIcon
import com.mountaya.geo.roadAccessGlyph
import com.mountaya.geo.waypointAmenities
import com.mountaya.geo.waypointSpriteId
import kotlinx.coroutines.test.TestScope

/**
 * `geo.icons.resolution`: the glyph name of the case's `amenity`, of its `activity` in a set holding the
 * case's `held` glyphs, or of its `roadAccessKind`, or the sprite id of its `icon` in its `color`,
 * whichever member the case states, a sprite case leaving either out reading a new waypoint's default,
 * `defaultWaypointIcon` or `DEFAULT_WAYPOINT_COLOR`. Every prefix, default and the amenity vocabulary are
 * held to the ones Geo publishes.
 */
object GeoIconsResolutionRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "activityMarkerPrefix" to FixtureValue.Text(ACTIVITY_MARKER_PREFIX),
                    "defaultWaypointColor" to FixtureValue.Text(DEFAULT_WAYPOINT_COLOR),
                    "defaultWaypointIcon" to FixtureValue.Text(defaultWaypointIcon.value),
                    "waypointAmenities" to textArray(waypointAmenities.map { it.value }),
                    "waypointFallbackGlyph" to FixtureValue.Text(WAYPOINT_FALLBACK_GLYPH),
                    "waypointIconPrefix" to FixtureValue.Text(WAYPOINT_ICON_PREFIX),
                ),
        )
        val input = case.fields
        val amenity = input.textOrNull("amenity")
        val activity = input.textOrNull("activity")
        val kind = input.textOrNull("roadAccessKind")
        val answer =
            when {
                amenity != null -> {
                    amenityGlyph(RestSchema.WaypointAmenity(amenity))
                }

                activity != null -> {
                    val held = glyphSetHolding(fixtureTexts(input.items("held"), "`held`"))
                    activityMarkerGlyph(RestSchema.ActivitySlug(activity), held)
                }

                kind != null -> {
                    roadAccessGlyph(
                        RoadAccessKind.entries.firstOrNull { it.id == kind }
                            ?: throw AssertionError("`$kind` is no kind of restricted stretch"),
                    )
                }

                else -> {
                    // A new waypoint states neither, and is drawn in the defaults Geo publishes for one.
                    val icon = input.textOrNull("icon")?.let(RestSchema::WaypointIcon) ?: defaultWaypointIcon
                    waypointSpriteId(icon, input.textOrNull("color") ?: DEFAULT_WAYPOINT_COLOR)
                }
            }
        return ConformanceOutcome.Value(answer?.let { FixtureValue.Text(it) } ?: FixtureValue.Null)
    }
}
