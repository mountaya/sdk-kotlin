package com.mountaya.elements

import com.mountaya.geo.GlyphSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ShowCursorMarkerTest {
    /** Markers is a map recording each write it took, as the elements' fixtures do. */
    class Markers : CursorMarkerTarget {
        val held = mutableMapOf<String, MapMarker>()
        val writes = mutableListOf<String>()

        override fun addMarker(marker: MapMarker) {
            held[marker.id] = marker
            writes += "add ${marker.color ?: "own"} ${marker.glyph ?: "dot"} ${marker.position}"
        }

        override fun editMarker(
            id: String,
            position: List<Double>?,
            color: String?,
            glyph: String?,
            label: String?,
        ) {
            val marker = held[id] ?: throw MapException(MapException.Kind.MarkerNotFound, id)
            val moved = MapMarker(marker.id, position ?: marker.position, marker.color, marker.glyph, marker.label)
            held[id] = moved
            writes += "edit ${moved.position}"
        }

        override fun removeMarker(id: String) {
            held.remove(id)
            writes += "remove"
        }
    }

    /** MapEngineDouble refuses every position, as a map refuses a latitude past the poles. */
    private class MapEngineDouble : CursorMarkerTarget {
        override fun addMarker(marker: MapMarker): Unit =
            throw MapException(MapException.Kind.PositionRefused, marker.id)

        override fun editMarker(
            id: String,
            position: List<Double>?,
            color: String?,
            glyph: String?,
            label: String?,
        ): Unit = throw MapException(MapException.Kind.PositionRefused, id)

        override fun removeMarker(id: String) = Unit
    }

    @Test
    fun theGlyphAndTheThreeIdsAreTheContracts() {
        assertEquals("pin-filled", CURSOR_MARKER_GLYPH)
        assertEquals("mtya-profile-cursor", PROFILE_CURSOR_MARKER_ID)
        assertEquals("mtya-segment-table-cursor", SEGMENT_TABLE_CURSOR_MARKER_ID)
        assertEquals("mtya-waypoint-table-cursor", WAYPOINT_TABLE_CURSOR_MARKER_ID)
    }

    /** C40 in SPEC.md. */
    @Test
    fun onAMapDrawingTheBundledMarkerSetTheFilledPinIsThePinsDot() {
        assertNotNull(GlyphSet.control.markup[CURSOR_MARKER_GLYPH], "the control set draws the cursor marker")
        assertNull(GlyphSet.marker.markup[CURSOR_MARKER_GLYPH])
        assertEquals(
            describeMarkerPin(null, null, GlyphSet.marker),
            describeMarkerPin(null, CURSOR_MARKER_GLYPH, GlyphSet.marker),
        )
    }

    @Test
    fun aCursorIsAddedOnceThenMovedByEditingItsPosition() {
        val map = Markers()
        var pinned = showCursorMarker(map, null, CursorMarkerPoint("c", listOf(6.0, 45.0, 1_200.0)))
        pinned = showCursorMarker(map, pinned, CursorMarkerPoint("c", listOf(7.0, 46.0)))

        assertEquals(listOf("add own pin-filled [6.0, 45.0]", "edit [7.0, 46.0]"), map.writes)
        assertEquals(CursorMarkerPin(null, "pin-filled"), pinned)
    }

    @Test
    fun aChangeOfColourOrGlyphAndAMarkerTakenDownElsewhereAddItAgain() {
        val map = Markers()
        var pinned = showCursorMarker(map, null, CursorMarkerPoint("c", listOf(6.0, 45.0)))
        pinned = showCursorMarker(map, pinned, CursorMarkerPoint("c", listOf(6.0, 45.0), "#e8590c"))
        map.removeMarker("c")
        pinned = showCursorMarker(map, pinned, CursorMarkerPoint("c", listOf(7.0, 46.0), "#e8590c"))

        assertEquals(
            listOf(
                "add own pin-filled [6.0, 45.0]",
                "add #e8590c pin-filled [6.0, 45.0]",
                "remove",
                "add #e8590c pin-filled [7.0, 46.0]",
            ),
            map.writes,
        )
        assertEquals("#e8590c", pinned.color)
    }

    @Test
    fun aRefusalOtherThanAMissingMarkerIsTheMapsOwn() {
        assertFailsWith<MapException> {
            showCursorMarker(MapEngineDouble(), null, CursorMarkerPoint("c", listOf(6.0, 95.0)))
        }
    }
}
