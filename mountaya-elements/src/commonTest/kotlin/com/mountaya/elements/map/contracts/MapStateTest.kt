package com.mountaya.elements

import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.CameraPose
import com.mountaya.geo.ZoomBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapStateTest {
    private val camera = CameraPose(listOf(6.86, 45.83), 12.0, 0.0, 0.0)

    @Test
    fun aStateHoldsWhatAMapShowsDrawingNothingAboveItsBaseMapUnlessTold() {
        val state = MapState(null, camera, RestSchema.Language.Fr)

        assertNull(state.style)
        assertEquals(camera, state.camera)
        assertEquals(RestSchema.Language.Fr, state.labelLanguage)
        assertTrue(state.sources.isEmpty())
        assertTrue(state.layers.isEmpty())
        assertTrue(state.markers.isEmpty())
        assertNull(state.maxBounds)
        assertEquals(ZoomBounds(0.0, 25.5), state.zoomBounds, "MapLibre Native's own")
        assertTrue(state.activeSegments.isEmpty())
        assertNull(state.waypointEdit)
        assertFalse(state.isAccessRequired)
        assertNull(state.terrain)
        assertEquals(
            TileSchema.TerrainVariant.Light,
            state.terrainVariant,
            "light until the app asks for dark",
        )
    }

    @Test
    fun twoStatesAreEqualWhereEveryPartIsTheSame() {
        val state = MapState(null, camera, RestSchema.Language.Fr)

        assertEquals(MapState(null, camera, RestSchema.Language.Fr), state)
        assertEquals(state.hashCode(), MapState(null, camera, RestSchema.Language.Fr).hashCode())
        assertNotEquals(state.copy(camera = CameraPose(listOf(6.86, 45.83), 13.0, 0.0, 0.0)), state)
        assertNotEquals(state.copy(labelLanguage = RestSchema.Language.De), state)
        assertNotEquals(state.copy(style = Url("https://example.com/style.json")), state)
        assertNotEquals(state.copy(markers = listOf(MapMarker("hut", listOf(6.86, 45.83)))), state)
        assertNotEquals(state.copy(maxBounds = listOf(6.0, 45.0, 7.0, 46.0)), state)
        assertNotEquals(state.copy(zoomBounds = ZoomBounds(5.0, 12.0)), state)
        assertNotEquals(state.copy(activeSegments = listOf(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0)))), state)
        assertNotEquals(state.copy(waypointEdit = WaypointEdit("w1")), state)
        assertNotEquals(
            state.copy(isAccessRequired = true),
            state,
            "a refusal of access standing is part of what a map shows",
        )
        assertNotEquals(state.copy(terrain = "outdoor"), state)
        assertNotEquals(state.copy(terrainVariant = TileSchema.TerrainVariant.Dark), state)
        assertEquals(
            state.copy(maxBounds = listOf(Double.NaN, 45.0, 7.0, 46.0)),
            state.copy(maxBounds = listOf(Double.NaN, 45.0, 7.0, 46.0)),
            "a NaN the same as a NaN",
        )
    }
}
