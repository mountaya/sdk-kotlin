package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapExceptionTest {
    @Test
    fun aRefusalNamesWhatTheCommandNamedAndSaysWhatToDo() {
        val refusal = MapException(MapException.Kind.LayerNotFound, "route")

        assertEquals(MapException.Kind.LayerNotFound, refusal.kind)
        assertEquals("route", refusal.name)
        assertTrue(refusal.message.orEmpty().contains("\"route\""))
        assertTrue(
            MapException(MapException.Kind.TerrainNotFound, "moon").message.orEmpty().contains("getTileLayers"),
        )
        assertTrue(MapException(MapException.Kind.MarkerNotFound, "hut").message.orEmpty().contains("\"hut\""))
        assertTrue(MapException(MapException.Kind.PositionRefused, "hut").message.orEmpty().contains("-90 to 90"))
        assertTrue(
            MapException(MapException.Kind.MaxBoundsRefused, "[5.0, 45.0, 7.0, 45.0]")
                .message
                .orEmpty()
                .contains("null to lift the lock"),
        )
        assertTrue(
            MapException(
                MapException.Kind.ZoomBoundsRefused,
                "13.0...12.0",
            ).message.orEmpty().contains("renderer's floor"),
        )
        assertTrue(MapException(MapException.Kind.NumberRefused, "zoom").message.orEmpty().contains("finite number"))
        assertTrue(
            MapException(
                MapException.Kind.FixRefused,
                "locate",
                GeolocationRefusal.Denied,
            ).message.orEmpty().contains("(denied)"),
        )
        assertTrue(MapException(MapException.Kind.Closed, "locate").message.orEmpty().contains("closed map"))
        assertEquals("layerNotFound", MapException.Kind.LayerNotFound.toString())
        assertNull(refusal.reason)
    }

    @Test
    fun theRefusalsOfContentNameWhatTheyRefusedAndSayWhatToDoEachKindItsOwn() {
        assertTrue(MapException(MapException.Kind.TypeRefused, "geojson").message.orEmpty().contains("\"geojson\""))
        assertTrue(MapException(MapException.Kind.OverlayNotFound, "snow").message.orEmpty().contains("getTileLayers"))
        assertTrue(MapException(MapException.Kind.KindMismatch, "slope").message.orEmpty().contains("addEnrichment"))
        assertTrue(MapException(MapException.Kind.AnalysisNotFound, "a").message.orEmpty().contains("\"a\""))
        assertTrue(MapException(MapException.Kind.NoInput, "a").message.orEmpty().contains("no input"))
        val kinds =
            listOf(
                MapException.Kind.TypeRefused,
                MapException.Kind.OverlayNotFound,
                MapException.Kind.KindMismatch,
                MapException.Kind.AnalysisNotFound,
                MapException.Kind.NoInput,
                MapException.Kind.LayerNotFound,
                MapException.Kind.Closed,
            )
        assertEquals(kinds.size, kinds.toSet().size, "each kind is its own")
    }

    @Test
    fun aRefusedFixNamesTheReasonTheDeviceGaveAndEachReasonIsItsOwn() {
        assertNotEquals(
            MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.Denied),
            MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.Unavailable),
        )
        assertEquals(
            MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.NoApi),
            MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.NoApi),
        )
        assertEquals(
            MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.NoApi).hashCode(),
            MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.NoApi).hashCode(),
        )
        assertNotEquals(
            MapException(MapException.Kind.Closed, "locate"),
            MapException(MapException.Kind.NumberRefused, "locate"),
        )
    }

    @Test
    fun twoRefusalsAreEqualWhereTheyAreOfOneKindAndNameTheSameAsWritten() {
        assertEquals(
            MapException(MapException.Kind.LayerNotFound, "a"),
            MapException(MapException.Kind.LayerNotFound, "a"),
        )
        assertNotEquals(
            MapException(MapException.Kind.LayerNotFound, "a"),
            MapException(MapException.Kind.TerrainNotFound, "a"),
        )
        assertNotEquals(
            MapException(MapException.Kind.LayerNotFound, "café"),
            MapException(MapException.Kind.LayerNotFound, "café"),
        )
    }
}
