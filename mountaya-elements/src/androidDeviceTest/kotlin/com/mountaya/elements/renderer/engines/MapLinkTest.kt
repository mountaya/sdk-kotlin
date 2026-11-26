package com.mountaya.elements

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** What a data element points at, written to a map that has no renderer and no network. */
class MapLinkTest {
    private val stretch = GeoJsonLineString(listOf(listOf(6.0, 45.0), listOf(6.0, 45.01)))
    private val target = MapTarget(listOf(6.0, 45.005), stretch)

    @Test
    fun pointingWritesTheTargetToTheMapAndLettingGoTakesDownWhatItWrote() {
        val map = Controls.engine()
        val link = MapLink<String>(PROFILE_CURSOR_MARKER_ID)
        link.point("climb", "climb", target, true, null, map, "#e03131", "#2f9e44")

        val marker =
            map.state.value.markers
                .single()
        assertEquals("climb", link.pointed)
        assertEquals(PROFILE_CURSOR_MARKER_ID, marker.id)
        assertEquals(listOf(6.0, 45.005), marker.position)
        assertEquals("#e03131", marker.color)
        assertEquals(CURSOR_MARKER_GLYPH, marker.glyph, "the filled pin, the pin's dot on the marker set")
        assertEquals(SlotGeometry(stretch, "#2f9e44"), map.geometry(GeometrySlot.Emphasis))
        assertTrue(link.isLit)

        link.point<String>(null, null, MapTarget(), true, null, map, null, null)
        assertNull(link.pointed)
        assertTrue(
            map.state.value.markers
                .isEmpty(),
        )
        assertNull(map.geometry(GeometrySlot.Emphasis).geometry)
        assertFalse(link.isLit)
        assertNull(link.pinned)
    }

    @Test
    fun pointingWhereItAlreadyPointsTellsNothingAndADisabledElementOnlyLetsGo() {
        val map = Controls.engine()
        val told = mutableListOf<String?>()
        val tell: (String?) -> Unit = { told += it }
        val link = MapLink<String>("mine")

        link.point("a", "a", target, true, tell, map, null, null)
        link.point("a", "a", target, true, tell, map, null, null)
        assertEquals(listOf<String?>("a"), told)

        link.point("b", "b", target, false, tell, map, null, null)
        assertEquals(listOf<String?>("a"), told, "a disabled element points at nothing new")
        assertEquals("a", link.pointed)

        link.point(null, null, MapTarget(), false, tell, map, null, null)
        assertEquals(listOf("a", null), told, "the row pointed at is let go, and the leave is told once")
        link.point(null, null, MapTarget(), false, tell, map, null, null)
        assertEquals(listOf("a", null), told)
    }

    @Test
    fun anOnHoverHandlerTakesTheMapOverAndNoMapIsWrittenNothing() {
        val map = Controls.engine()
        val told = mutableListOf<String?>()
        val handed = MapLink<String>("mine")
        handed.point("a", "a", target, true, { told += it }, map, null, null)

        assertEquals(listOf<String?>("a"), told)
        assertTrue(
            map.state.value.markers
                .isEmpty(),
        )
        assertNull(map.geometry(GeometrySlot.Emphasis).geometry)

        val mapless = MapLink<String>("mine")
        mapless.point("a", "a", target, true, null, null, null, null)
        assertEquals("a", mapless.pointed, "the row is lit all the same")
    }

    @Test
    fun aMoveTakesTheMarkerAlongAndAStretchNamedNoColourIsTheSlotsOwn() {
        val map = Controls.engine()
        val link = MapLink<Int>("mine")
        link.write(MapTarget(listOf(6.0, 45.0)), map, null, null)
        link.write(MapTarget(listOf(6.0, 45.01), stretch), map, null, null)

        assertEquals(
            listOf("mine"),
            map.state.value.markers
                .map { it.id },
        )
        assertEquals(
            listOf(6.0, 45.01),
            map.state.value.markers
                .first()
                .position,
        )
        assertEquals(GeometrySlot.Emphasis.color, map.geometry(GeometrySlot.Emphasis).color)
    }

    @Test
    fun anElementTakesDownOnlyWhatItWroteAnothersMarkerAndAnothersStretchStay() {
        val map = Controls.engine()
        map.addMarker(MapMarker("other", listOf(6.1, 45.0)))
        map.setGeometry(stretch, GeometrySlot.Emphasis)
        val link = MapLink<Int>("mine")
        link.write(MapTarget(), map, null, null)

        assertEquals(
            listOf("other"),
            map.state.value.markers
                .map { it.id },
        )
        assertEquals(stretch, map.geometry(GeometrySlot.Emphasis).geometry)
    }

    @Test
    fun aMarkerSomethingElseTookDownIsPinnedAgainAtTheNextMove() {
        val map = Controls.engine()
        val link = MapLink<Int>("mine")
        link.write(MapTarget(listOf(6.0, 45.0)), map, null, null)
        map.removeMarker("mine")
        link.write(MapTarget(listOf(6.0, 45.01)), map, null, null)

        assertEquals(
            listOf(listOf(6.0, 45.01)),
            map.state.value.markers
                .map { it.position },
        )
    }

    @Test
    fun aPositionTheMapRefusesLeavesTheMarkerWhereItStood() {
        val map = Controls.engine()
        val link = MapLink<Int>("mine")
        link.write(MapTarget(listOf(6.0, 45.0)), map, null, null)
        link.write(MapTarget(listOf(6.0, 95.0)), map, null, null)

        assertEquals(
            listOf(listOf(6.0, 45.0)),
            map.state.value.markers
                .map { it.position },
        )
    }
}
