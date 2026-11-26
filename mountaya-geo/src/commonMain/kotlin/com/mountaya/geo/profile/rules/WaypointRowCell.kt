package com.mountaya.geo

import kotlin.time.Instant

/**
 * defaultWaypointTableColumns is the columns a waypoint table draws where its caller names none, the
 * `defaultColumns` `geo/profile-waypoint-table-cells.json` declares: the place, its shape, how far, how much
 * climb, and what is there.
 */
public val defaultWaypointTableColumns: List<WaypointTableColumn> =
    listOf(
        WaypointTableColumn.Waypoint,
        WaypointTableColumn.Profile,
        WaypointTableColumn.Distance,
        WaypointTableColumn.Elevation,
        WaypointTableColumn.Amenities,
    )

/**
 * waypointRowCell is what [column] of a waypoint table states about [row]'s place
 * (`geo.profile.waypoint-table-cells`): its glyph, its own where [glyphs], the set the table marks places with,
 * draws it and [WAYPOINT_FALLBACK_GLYPH] otherwise, and its name; the whole itinerary's sparkline with the
 * stretch reaching it lit; its running distance; the climb and loss since the previous place; its altitude; its
 * services in the order it states them; its cut-off only through [formatCutoffAt], since an instant needs a time
 * zone only the caller holds; and its fields, each written by its declared format for [units]
 * ([waypointPropertyValue]), with the address a link field is anchored by.
 */
public fun waypointRowCell(
    row: ItineraryWaypointRow,
    column: WaypointTableColumn,
    glyphs: GlyphSet,
    units: UnitPreferences,
    formatCutoffAt: ((Instant) -> String)? = null,
): WaypointRowCell {
    val details = row.place.details
    val metrics = row.metrics
    return when (column) {
        WaypointTableColumn.Waypoint -> {
            val own = row.place.glyph?.value
            val glyph = own?.takeIf { it in glyphs.markup } ?: WAYPOINT_FALLBACK_GLYPH
            WaypointRowCell.Waypoint(glyph, row.place.name)
        }

        WaypointTableColumn.Profile -> {
            WaypointRowCell.Profile(ProfileRange(row.from, row.to))
        }

        WaypointTableColumn.Distance -> {
            WaypointRowCell.Distance(metrics.distance)
        }

        WaypointTableColumn.Elevation -> {
            WaypointRowCell.Elevation(metrics.sectionAscent, metrics.sectionDescent)
        }

        WaypointTableColumn.Altitude -> {
            WaypointRowCell.Altitude(metrics.altitude)
        }

        WaypointTableColumn.Amenities -> {
            WaypointRowCell.Amenities(details?.amenities.orEmpty())
        }

        WaypointTableColumn.Cutoff -> {
            val cutoffAt = details?.timing?.cutoffAt
            WaypointRowCell.Cutoff(if (cutoffAt != null && formatCutoffAt != null) formatCutoffAt(cutoffAt) else null)
        }

        WaypointTableColumn.Properties -> {
            WaypointRowCell.Properties(
                details?.properties.orEmpty().map { property ->
                    WaypointField(
                        property.key,
                        property.label,
                        waypointPropertyValue(property, units, formatCutoffAt),
                        waypointPropertyLink(property),
                    )
                },
            )
        }
    }
}
