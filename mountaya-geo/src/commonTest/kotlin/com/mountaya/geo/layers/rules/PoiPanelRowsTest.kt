package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PoiPanelRowsTest {
    /** Each row as `id:keys:on`. */
    private fun shown(offer: PoiPanelRows): List<String> =
        offer.rows.map { "${it.id}:${it.keys.joinToString("+")}:${it.isVisible}" }

    /** A key shown unless it is among [hidden]. */
    private fun hiding(vararg hidden: String): (String) -> Boolean = { it !in hidden }

    @Test
    fun aRowPerConceptDrawnInTheOrderDrawnEachOnWhileShown() {
        val offer = poiPanelRows(listOf("parking", "poi_food"), hiding("parking"))

        assertEquals(listOf("parking:parking:false", "poi_food:poi_food:true"), shown(offer))
        assertEquals(listOf("parking", "poi_food"), offer.rows.map { it.label })
        assertTrue(offer.isAnyVisible)
    }

    @Test
    fun theMasterRowIsOffOnlyWhileEveryRowIs() {
        assertFalse(poiPanelRows(listOf("parking", "poi_food"), hiding("parking", "poi_food")).isAnyVisible)
        assertFalse(poiPanelRows(emptyList(), { true }).isAnyVisible)
    }

    @Test
    fun aCuratedKeyListOffersWhatIsDrawnInItsOwnOrderEachLabelledAsHanded() {
        val offer =
            poiPanelRows(
                listOf("poi_food", "parking"),
                { true },
                keys = listOf("parking", "toilets", "poi_food"),
                labels = mapOf("parking" to "Parking"),
            )

        assertEquals(listOf("parking:parking:true", "poi_food:poi_food:true"), shown(offer))
        assertEquals(listOf("Parking", "poi_food"), offer.rows.map { it.label })
    }

    @Test
    fun aGroupDrivesItsDrawnKeysAloneOnWhileAnyIsShownAndNoneDrawnIsNoRow() {
        val groups =
            listOf(
                PoiPanelGroup("water", "Water", listOf("spring", "drinking_water", "fountain"), glyph = "pin"),
                PoiPanelGroup("parking", "Parking", listOf("parking")),
                PoiPanelGroup("huts", "Huts", listOf("hut")),
            )
        val drawn = listOf("spring", "fountain", "parking")
        val some = poiPanelRows(drawn, hiding("fountain"), groups = groups)
        val none = poiPanelRows(drawn, hiding("spring", "fountain"), groups = groups)

        assertEquals(listOf("water:spring+fountain:true", "parking:parking:true"), shown(some))
        assertEquals(listOf("Water", "Parking"), some.rows.map { it.label })
        assertEquals(listOf("pin", null), some.rows.map { it.glyph })
        assertEquals(listOf("water:spring+fountain:false", "parking:parking:true"), shown(none))
    }

    @Test
    fun groupsSupersedeAKeyListAndGroupsTheAppHoldsReadItsWord() {
        val groups =
            listOf(
                PoiPanelGroup("water", "Water", listOf("spring")),
                PoiPanelGroup("parking", "Parking", listOf("parking")),
            )
        val superseded =
            poiPanelRows(listOf("spring", "parking"), { true }, keys = listOf("parking"), groups = listOf(groups[0]))
        val owned =
            poiPanelRows(listOf("spring", "parking"), { true }, groups = groups, activeGroups = listOf("parking"))

        assertEquals(listOf("water:spring:true"), shown(superseded))
        assertEquals(listOf("water:spring:false", "parking:parking:true"), shown(owned))
        assertTrue(owned.isAnyVisible)
    }

    @Test
    fun keysAreReadExactly() {
        val offer = poiPanelRows(listOf("café"), { true }, keys = listOf("café", "café"))

        assertEquals(listOf("café"), offer.rows.map { it.id })
    }

    @Test
    fun theNotesZoomIsTheOverlaysFloorAndNoneForAFloorLeftOutOrNoNumber() {
        assertEquals(12.0, poiPanelNoteZoom(12.0))
        assertEquals(Double.NEGATIVE_INFINITY, poiPanelNoteZoom(null))
        assertEquals(Double.NEGATIVE_INFINITY, poiPanelNoteZoom(Double.NaN))
        assertEquals(Double.NEGATIVE_INFINITY, poiPanelNoteZoom(Double.NEGATIVE_INFINITY))
        // The note stands below the floor, never at it.
        assertFalse(11.9 >= poiPanelNoteZoom(12.0))
        assertTrue(12.0 >= poiPanelNoteZoom(12.0))
        assertTrue(3.0 >= poiPanelNoteZoom(Double.NaN))
    }
}
