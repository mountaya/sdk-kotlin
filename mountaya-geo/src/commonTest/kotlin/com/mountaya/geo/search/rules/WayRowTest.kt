package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WayRowTest {
    @Test
    fun aWayAdvertisesTheActivitiesItSuits() {
        val way =
            DataSchema.Way {
                osm = SearchEntity.osm("WAY", "3")
                name = "GR 5"
                highway = "path"
                point = listOf(6.5, 45.5)
                bounds = listOf(6.0, 45.0, 7.0, 46.0)
                coordinates = listOf(listOf(6.0, 45.0), listOf(7.0, 46.0))
                activityCompatible = listOf(DataSchema.ActivitySlug("HIKING_AND_TRAIL"))
            }
        val row = wayRow(way, "gr")
        assertEquals("way:WAY:3", row?.id)
        assertEquals(listOf(DataSchema.ActivitySlug("HIKING_AND_TRAIL")), row?.activities)
        assertEquals(
            SearchRowPayload.Way(
                listOf(6.5, 45.5),
                listOf(6.0, 45.0, 7.0, 46.0),
                listOf(listOf(6.0, 45.0), listOf(7.0, 46.0)),
            ),
            row?.payload,
        )
    }

    @Test
    fun aWayWithNoActivityAdvertisesNoneAndOneWithNoPointIsNoRow() {
        assertEquals(emptyList(), wayRow(SearchEntity.way(), "")?.activities)
        assertNull(wayRow(SearchEntity.way(point = null), ""))
    }

    @Test
    fun aWaysAnchorDropsTheAltitudeItsPointCarriesAndItsLineIsTracedAsSent() {
        val way =
            DataSchema.Way {
                osm = SearchEntity.osm("WAY", "3")
                name = "GR 5"
                point = listOf(6.5, 45.5, 1_200.0)
                coordinates = listOf(listOf(6.0, 45.0, 1_000.0), listOf(7.0, 46.0, 1_100.0))
            }
        assertEquals(
            SearchRowPayload.Way(
                listOf(6.5, 45.5),
                null,
                listOf(listOf(6.0, 45.0, 1_000.0), listOf(7.0, 46.0, 1_100.0)),
            ),
            wayRow(way, "")?.payload,
        )
    }
}
