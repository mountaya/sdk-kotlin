package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MapEventTest {
    @Test
    fun twoEventsAreEqualWhereTheyAreOfOneKindAndHoldTheSame() {
        val tap = MapTap(listOf(6.86, 45.83), listOf(1.0, 2.0))

        assertEquals<MapEvent>(MapEvent.Click(tap), MapEvent.Click(tap))
        assertEquals(MapEvent.Click(tap).hashCode(), MapEvent.Click(tap).hashCode())
        assertNotEquals<MapEvent>(MapEvent.Click(tap), MapEvent.LongClick(tap))
        assertNotEquals<MapEvent>(
            MapEvent.FeatureHover(listOf(170.0, 45.0), emptyList()),
            MapEvent.FeatureClick(listOf(170.0, 45.0), emptyList()),
        )
        assertEquals<MapEvent>(
            MapEvent.FeatureHover(listOf(Double.NaN, 45.0), emptyList()),
            MapEvent.FeatureHover(listOf(Double.NaN, 45.0), emptyList()),
        )
        assertNotEquals<MapEvent>(
            MapEvent.AccessRefused(402, "https://tiles.test/v1/tile-layers"),
            MapEvent.AccessRefused(403, "https://tiles.test/v1/tile-layers"),
        )
        assertEquals<MapEvent>(MapEvent.AccessRestored, MapEvent.AccessRestored)
        assertNotEquals<MapEvent>(MapEvent.Failure("u", null), MapEvent.Failure("u", 404))
        val read = IllegalStateException("The token store is locked.")
        assertEquals<MapEvent>(MapEvent.Failure("u", null, read), MapEvent.Failure("u", null, read))
        assertEquals(MapEvent.Failure("u", null, read).hashCode(), MapEvent.Failure("u", null, read).hashCode())
        assertNotEquals<MapEvent>(MapEvent.Failure("u", null, read), MapEvent.Failure("u", null))
        assertNotEquals<MapEvent>(
            MapEvent.Failure("u", null, read),
            MapEvent.Failure("u", null, IllegalStateException("The token store is locked.")),
            "a cause is the very failure, as a throwable is equal to itself alone",
        )
        assertEquals<MapEvent>(MapEvent.LayersChange(null), MapEvent.LayersChange(null))
        assertNotEquals<MapEvent>(MapEvent.LayersChange(null), MapEvent.AccessRestored)
    }
}
