package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PoiRowTest {
    @Test
    fun aPoiCarriesItsCategoryToThePayload() {
        val row = poiRow(SearchEntity.poi(distance = 850.0), "gouter")
        assertEquals("poi:NODE:9", row?.id)
        assertEquals(listOf(10 until 16), row?.matched)
        assertEquals(SearchRowSecondary.Text("Hut · 850 m"), row?.secondary)
        assertEquals(SearchRowPayload.Poi(listOf(6.5, 45.8), DataSchema.POICategory("HUT")), row?.payload)
    }

    @Test
    fun aPoiWithNoNameIsTitledByItsCategoryAndStatesItOnce() {
        val row = poiRow(SearchEntity.poi(name = "", category = "CAFE"), "")
        assertEquals("Cafe", row?.title)
        assertNull(row?.secondary)
        assertEquals(emptyList(), row?.matched)
    }

    @Test
    fun aPoiWhosePointNamesNoPlaceIsNoRowAndOneWithAnAltitudeDropsIt() {
        assertNull(poiRow(SearchEntity.poi(point = emptyList()), ""))
        assertEquals(
            SearchRowPayload.Poi(listOf(6.5, 45.8), DataSchema.POICategory("HUT")),
            poiRow(SearchEntity.poi(point = listOf(6.5, 45.8, 3_817.0)), "")?.payload,
        )
    }
}
