package com.mountaya.elements

/**
 * CURSOR_MARKER_GLYPH is the glyph an element's cursor marker is drawn with where its caller names none, the
 * `glyph` `map/profile-cursor-marker.json` declares: the filled pin, which stands in the control set alone, so on
 * a map drawing the bundled marker set the marker is the pin's dot (`map.marker.pin`).
 */
public const val CURSOR_MARKER_GLYPH: String = "pin-filled"

/**
 * PROFILE_CURSOR_MARKER_ID is the id a profile pins its cursor marker under where its caller names none, one of
 * the `markerIds` `map/profile-cursor-marker.json` declares, so a profile and two tables over one map never move
 * or take down each other's marker.
 */
public const val PROFILE_CURSOR_MARKER_ID: String = "mtya-profile-cursor"

/** SEGMENT_TABLE_CURSOR_MARKER_ID is the id a segment table pins its cursor marker under where its caller names none. */
public const val SEGMENT_TABLE_CURSOR_MARKER_ID: String = "mtya-segment-table-cursor"

/** WAYPOINT_TABLE_CURSOR_MARKER_ID is the id a waypoint table pins its cursor marker under where its caller names none. */
public const val WAYPOINT_TABLE_CURSOR_MARKER_ID: String = "mtya-waypoint-table-cursor"

/** The numbers of a position a marker is pinned at: a longitude and a latitude. */
private const val MARKER_POSITION_NUMBERS = 2

/**
 * showCursorMarker pins an element's cursor marker on [map] where the cursor stands, [point], and answers what the
 * marker is drawn with (`map.profile.cursor-marker`).
 *
 * A marker the element added, [pinned], drawn with the point's colour and glyph, is moved by editing its position,
 * so a label opened on it stays open; one whose colour or glyph changed is added again, since an edit cannot take
 * a colour off, and so is one something else took down, the edit finding no marker. It throws what the map
 * throws for a write it refuses, but a missing marker on an edit.
 */
public fun showCursorMarker(
    map: CursorMarkerTarget,
    pinned: CursorMarkerPin?,
    point: CursorMarkerPoint,
): CursorMarkerPin {
    val position = point.lngLat.take(MARKER_POSITION_NUMBERS)
    val drawn = CursorMarkerPin(point.color, point.glyph)
    if (pinned == drawn) {
        try {
            map.editMarker(point.id, position = position)
            return drawn
        } catch (refused: MapException) {
            // The marker is gone, and is added again below.
            if (refused.kind != MapException.Kind.MarkerNotFound) throw refused
        }
    }
    map.addMarker(MapMarker(point.id, position, point.color, point.glyph))
    return drawn
}
