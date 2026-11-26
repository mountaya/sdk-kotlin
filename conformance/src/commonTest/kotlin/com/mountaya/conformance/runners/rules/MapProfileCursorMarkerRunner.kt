package com.mountaya.conformance

import com.mountaya.elements.CURSOR_MARKER_GLYPH
import com.mountaya.elements.CursorMarkerPin
import com.mountaya.elements.CursorMarkerPoint
import com.mountaya.elements.CursorMarkerTarget
import com.mountaya.elements.InertGeolocation
import com.mountaya.elements.MapEngine
import com.mountaya.elements.MapMarker
import com.mountaya.elements.PROFILE_CURSOR_MARKER_ID
import com.mountaya.elements.SEGMENT_TABLE_CURSOR_MARKER_ID
import com.mountaya.elements.WAYPOINT_TABLE_CURSOR_MARKER_ID
import com.mountaya.elements.hideCursorMarker
import com.mountaya.elements.showCursorMarker
import kotlinx.coroutines.test.TestScope

/**
 * `map.profile.cursor-marker`: plays the case's steps over a real map. Each element, a profile and two tables, pins
 * its cursor through `showCursorMarker` and takes it down through `hideCursorMarker`, holding what it pinned, and
 * writes through a map that records each write the engine took, an edit the engine refused writing nothing; an
 * element's `style` takes effect at once where its cursor stands, and `leave` and `unmount` both hide. `remove` and
 * `pin` are another holder of the id, writing to the engine directly, `pin` with the glyph `map-pin`. The glyph and
 * the ids are held to the published constants.
 */
object MapProfileCursorMarkerRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "glyph" to FixtureValue.Text(CURSOR_MARKER_GLYPH),
                    "markerIds" to
                        objectValue(
                            "profile" to FixtureValue.Text(PROFILE_CURSOR_MARKER_ID),
                            "segmentTable" to FixtureValue.Text(SEGMENT_TABLE_CURSOR_MARKER_ID),
                            "waypointTable" to FixtureValue.Text(WAYPOINT_TABLE_CURSOR_MARKER_ID),
                        ),
                ),
        )
        val map = MapEngine(MapFixtures.client(), geolocation = InertGeolocation())
        val recorder = RecordedMarkers(map)
        val ids =
            mapOf(
                "profile" to PROFILE_CURSOR_MARKER_ID,
                "segmentTable" to SEGMENT_TABLE_CURSOR_MARKER_ID,
                "waypointTable" to WAYPOINT_TABLE_CURSOR_MARKER_ID,
            )
        val elements = HashMap<String, CursorElement>()
        for (item in case.fields.items("steps")) {
            val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
            val kind = step.text("step")
            if (kind == "remove") {
                map.removeMarker(step.text("id"))
                continue
            }
            if (kind == "pin") {
                map.addMarker(MapMarker(step.text("id"), fixturePosition(step["lngLat"], "lngLat"), glyph = "map-pin"))
                continue
            }
            val name = step.text("element")
            val id = ids[name] ?: throw AssertionError("`$name` is no element")
            val element = elements.getOrPut(name) { CursorElement(id) }
            recorder.element = name
            when (kind) {
                "point" -> {
                    element.cursor = fixturePosition(step["lngLat"], "lngLat")
                    element.show(recorder)
                }

                "style" -> {
                    step.textOrNull("color")?.let { element.color = it }
                    step.textOrNull("glyph")?.let { element.glyph = it }
                    if (element.cursor != null) element.show(recorder)
                }

                "leave", "unmount" -> {
                    element.cursor = null
                    hideCursorMarker(recorder, element.pinned, element.id)
                    element.pinned = null
                }

                else -> {
                    throw AssertionError("`$kind` is no step")
                }
            }
        }
        return ConformanceOutcome.Value(
            objectValue(
                "markers" to
                    FixtureValue.Array(
                        map.state.value.markers.map { marker ->
                            objectValue(
                                "color" to textOrNull(marker.color),
                                "glyph" to textOrNull(marker.glyph),
                                "id" to FixtureValue.Text(marker.id),
                                "lngLat" to numbersValue(marker.position),
                            )
                        },
                    ),
                "writes" to FixtureValue.Array(recorder.writes),
            ),
        )
    }

    /** One element's cursor in a script: its marker's id, where its cursor stands, how its marker is drawn, and what it pinned. */
    private class CursorElement(
        val id: String,
    ) {
        var cursor: List<Double>? = null
        var color: String? = null
        var glyph: String = CURSOR_MARKER_GLYPH
        var pinned: CursorMarkerPin? = null

        /** Pins the marker where the cursor stands. */
        fun show(map: CursorMarkerTarget) {
            val at = cursor ?: return
            pinned = showCursorMarker(map, pinned, CursorMarkerPoint(id, at, color, glyph))
        }
    }

    /** A map that hands each marker write to a real engine and records the ones it took, under the element writing them. */
    private class RecordedMarkers(
        val map: MapEngine,
    ) : CursorMarkerTarget {
        var element = ""
        val writes = ArrayList<FixtureValue>()

        override fun addMarker(marker: MapMarker) {
            map.addMarker(marker)
            writes +=
                objectValue(
                    "color" to textOrNull(marker.color),
                    "element" to FixtureValue.Text(element),
                    "glyph" to textOrNull(marker.glyph),
                    "id" to FixtureValue.Text(marker.id),
                    "lngLat" to numbersValue(marker.position),
                    "write" to FixtureValue.Text("add"),
                )
        }

        override fun editMarker(
            id: String,
            position: List<Double>?,
            color: String?,
            glyph: String?,
            label: String?,
        ) {
            map.editMarker(id, position, color, glyph, label)
            writes +=
                objectValue(
                    "element" to FixtureValue.Text(element),
                    "id" to FixtureValue.Text(id),
                    "lngLat" to (position?.let(::numbersValue) ?: FixtureValue.Null),
                    "write" to FixtureValue.Text("edit"),
                )
        }

        override fun removeMarker(id: String) {
            map.removeMarker(id)
            writes +=
                objectValue(
                    "element" to FixtureValue.Text(element),
                    "id" to FixtureValue.Text(id),
                    "write" to FixtureValue.Text("remove"),
                )
        }
    }
}
