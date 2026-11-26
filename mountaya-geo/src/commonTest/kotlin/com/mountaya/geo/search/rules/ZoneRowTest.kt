package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ZoneRowTest {
    @Test
    fun aZoneStatesItsOwnDescription() {
        val row =
            zoneRow(
                DataSchema.ZoneSearchResult {
                    id = "zone-1"
                    name = "Couloir closure"
                    description = "Wind slab above the couloir"
                    point = listOf(6.5, 45.5)
                },
                "couloir",
            )
        assertEquals("zone:zone-1", row?.id)
        assertEquals(SearchRowSecondary.Text("Wind slab above the couloir"), row?.secondary)
        assertEquals(SearchRowPayload.Zone("zone-1", listOf(6.5, 45.5), null), row?.payload)
    }

    @Test
    fun aZoneWithNoDescriptionIsItsTitleAlone() {
        assertNull(zoneRow(DataSchema.ZoneSearchResult { id = "z" }, "")?.secondary)
        assertNull(zoneRow(DataSchema.ZoneSearchResult { name = "z" }, ""))
    }

    @Test
    fun aZonesAnchorDropsTheAltitudeItsPointCarries() {
        val row =
            zoneRow(
                DataSchema.ZoneSearchResult {
                    id = "zone-1"
                    point = listOf(6.5, 45.5, 2_000.0)
                },
                "",
            )
        assertEquals(SearchRowPayload.Zone("zone-1", listOf(6.5, 45.5), null), row?.payload)
    }
}
