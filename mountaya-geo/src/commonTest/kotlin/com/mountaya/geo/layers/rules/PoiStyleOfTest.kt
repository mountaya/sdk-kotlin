package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PoiStyleOfTest {
    /** Every category the Data API declares, as `DataSchema.POICategory` names them. */
    private val declared =
        listOf(
            "HUT",
            "PEAK",
            "PASS",
            "VIEWPOINT",
            "PARKING",
            "WATER_SOURCE",
            "CAFE",
            "RESTAURANT",
            "SUPERMARKET",
            "PHARMACY",
            "TOILETS",
            "CAMPSITE",
            "SHELTER",
            "CABLE_CAR",
            "BUS_STATION",
            "TRAIN_STATION",
            "AIRPORT",
            "ACCOMMODATION",
            "CARAVAN_SITE",
            "HOSPITAL",
        ).map(DataSchema::POICategory)

    @Test
    fun everyCategoryTheDataApiDeclaresHasAStyleAndTheTableHoldsNoOther() {
        for (category in declared) assertNotNull(poiStyleOf(category), category.value)
        assertEquals(declared.size, poiCategoryStyles.size)
        assertEquals(declared.map { it.value }.toSet(), poiCategoryStyles.keys.map { it.value }.toSet())
    }

    @Test
    fun eachCategoryIsDrawnWithTheContractsGlyphAndColour() {
        val table =
            mapOf(
                "ACCOMMODATION" to ("apartment-building" to "#7b1fa2"),
                "AIRPORT" to ("airport" to "#455a64"),
                "BUS_STATION" to ("bus" to "#455a64"),
                "CABLE_CAR" to ("tram" to "#455a64"),
                "CAFE" to ("cutlery" to "#e53935"),
                "CAMPSITE" to ("camping" to "#2e7d32"),
                "CARAVAN_SITE" to ("camper-van" to "#6d4c41"),
                "HOSPITAL" to ("medical-cross" to "#d81b60"),
                "HUT" to ("hut" to "#ff9800"),
                "PARKING" to ("parking-sign" to "#1565c0"),
                "PASS" to ("pass" to "#795548"),
                "PEAK" to ("mountain" to "#5d4037"),
                "PHARMACY" to ("medical-cross" to "#4caf50"),
                "RESTAURANT" to ("cutlery" to "#e53935"),
                "SHELTER" to ("campground" to "#ff9800"),
                "SUPERMARKET" to ("cart" to "#8e24aa"),
                "TOILETS" to ("toilet" to "#0288d1"),
                "TRAIN_STATION" to ("train" to "#455a64"),
                "VIEWPOINT" to ("eye" to "#00897b"),
                "WATER_SOURCE" to ("droplet" to "#2196f3"),
            )
        assertEquals(poiCategoryStyles.size, table.size)
        for ((category, expected) in table) {
            assertEquals(
                PoiStyle(expected.first, HexColor(expected.second)),
                poiStyleOf(DataSchema.POICategory(category)),
                category,
            )
        }
    }

    @Test
    fun severalCategoriesShareAGlyphEachToldApartByItsColour() {
        val pharmacy = assertNotNull(poiStyleOf(DataSchema.POICategory.Pharmacy), "the table styles the category")
        val hospital = assertNotNull(poiStyleOf(DataSchema.POICategory.Hospital), "the table styles the category")
        assertEquals(hospital.glyph, pharmacy.glyph)
        assertNotEquals(hospital.color, pharmacy.color)
    }

    /** A pin no set can draw would be drawn as the glyph fallback rather than as the category's own. */
    @Test
    fun everyGlyphIsOneTheMarkerSetHoldsInAnOpaqueColourOfSixSmallHexDigits() {
        for ((category, style) in poiCategoryStyles) {
            assertNotNull(GlyphSet.marker.markup[style.glyph], category.value)
            assertEquals(1.0, style.color.opacity, category.value)
            assertTrue(Regex("#[0-9a-f]{6}").matches(style.color.hex), category.value)
        }
    }

    /**
     * Matched exactly, letter case included: `PARKING` is written with the Kelvin sign, which a
     * Kotlin string compares by its code units, so it names another category.
     */
    @Test
    fun aCategoryTheTableDoesNotNameInAnotherCaseOrAnotherSpellingHasNoStyle() {
        for (name in listOf("hut", "Hut", "SKI_LIFT", "", " HUT", "PARKING")) {
            assertNull(poiStyleOf(DataSchema.POICategory(name)), name)
        }
    }
}
