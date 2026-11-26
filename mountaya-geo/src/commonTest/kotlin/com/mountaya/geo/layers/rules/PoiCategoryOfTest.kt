package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PoiCategoryOfTest {
    @Test
    fun aTilesCategoryReadsAsTheApisThroughTheTableSeveralNamingOne() {
        assertEquals(DataSchema.POICategory.Hut, poiCategoryOf("alpine_hut"))
        assertEquals(DataSchema.POICategory.Hut, poiCategoryOf("wilderness_hut"))
        assertEquals(DataSchema.POICategory.Campsite, poiCategoryOf("camp_site"))
        assertEquals(DataSchema.POICategory.Restaurant, poiCategoryOf("restaurant"))
        assertEquals(DataSchema.POICategory.Restaurant, poiCategoryOf("fast_food"))
        assertEquals(DataSchema.POICategory.Restaurant, poiCategoryOf("food_court"))
        assertEquals(DataSchema.POICategory.WaterSource, poiCategoryOf("spring"))
        assertEquals(DataSchema.POICategory.WaterSource, poiCategoryOf("drinking_water"))
        assertEquals(DataSchema.POICategory.WaterSource, poiCategoryOf("water_well"))
    }

    @Test
    fun theTableIsTheContractsEveryEntryReadThroughTheRule() {
        val table =
            mapOf(
                "alpine_hut" to "HUT",
                "cafe" to "CAFE",
                "camp_site" to "CAMPSITE",
                "caravan_site" to "CARAVAN_SITE",
                "chalet" to "ACCOMMODATION",
                "drinking_water" to "WATER_SOURCE",
                "fast_food" to "RESTAURANT",
                "food_court" to "RESTAURANT",
                "guest_house" to "ACCOMMODATION",
                "hospital" to "HOSPITAL",
                "hostel" to "ACCOMMODATION",
                "hotel" to "ACCOMMODATION",
                "parking" to "PARKING",
                "pharmacy" to "PHARMACY",
                "restaurant" to "RESTAURANT",
                "shelter" to "SHELTER",
                "spring" to "WATER_SOURCE",
                "toilets" to "TOILETS",
                "viewpoint" to "VIEWPOINT",
                "water_well" to "WATER_SOURCE",
                "wilderness_hut" to "HUT",
            )
        assertEquals(table, poiTileCategories.mapValues { it.value.value })
        for ((tile, category) in table) assertEquals(category, poiCategoryOf(tile)?.value, tile)
    }

    /**
     * The two tables are written apart, so a tile category could name a category with no style, which
     * would draw no pin.
     */
    @Test
    fun everyTileCategoryReadsAsACategoryThatHasAStyle() {
        for ((tile, category) in poiTileCategories) assertNotNull(poiStyleOf(category), tile)
    }

    @Test
    fun aCategoryReadsWithoutRegardToTheCaseOfItsAsciiLetters() {
        assertEquals(DataSchema.POICategory.Restaurant, poiCategoryOf("Restaurant"))
        assertEquals(DataSchema.POICategory.WaterSource, poiCategoryOf("DRINKING_WATER"))
        assertEquals(DataSchema.POICategory.Hut, poiCategoryOf("ALPINE_HUT"))
    }

    /**
     * Only A to Z fold: a capital outside ASCII, a full-width letter and the Kelvin sign, which
     * Unicode's lowercasing reads as `k`, name another category.
     */
    @Test
    fun onlyTheAsciiCapitalsFoldAndNothingIsTrimmed() {
        for (name in listOf("ALPİNE_HUT", "ＣＡＦＥ", "PARKING", " cafe", "cafe ")) {
            assertNull(poiCategoryOf(name), name)
        }
    }

    @Test
    fun aCategoryTheTableDoesNotNameReadsAsNoneANameEveryObjectInheritsIncluded() {
        assertNull(poiCategoryOf("nuclear_power_plant"))
        assertNull(poiCategoryOf(""))
        for (name in listOf("constructor", "toString", "hasOwnProperty", "__proto__", "valueOf")) {
            assertNull(poiCategoryOf(name), name)
        }
    }
}
