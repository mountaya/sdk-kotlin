package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class LayerRankTest {
    @Test
    fun theBandsStackBottomToTopTenApartButTheTwoThatStandOverTheLine() {
        assertEquals(listOf(10, 20, 30, 40, 50, 52, 55, 60, 70, 80, 90, 100), LayerRank.entries.map { it.rank })
        assertEquals(10, LayerRank.Hillshade.rank)
        assertEquals(20, LayerRank.Overlay.rank)
    }

    @Test
    fun theRestrictedStretchesLieOverTheLineTheyMarkAndBeneathItsDirection() {
        assertEquals(55, LayerRank.ItineraryRestriction.rank)
        assertEquals(LayerRank.ItineraryFocus.ordinal + 1, LayerRank.ItineraryRestriction.ordinal)
        assertEquals(LayerRank.ItineraryDirection.ordinal - 1, LayerRank.ItineraryRestriction.ordinal)
    }

    @Test
    fun theFocusAMapDrawsFromDataLiesOverTheItinerarysLineBeneathItsRestrictions() {
        assertEquals(52, LayerRank.ItineraryFocus.rank)
        assertEquals(LayerRank.ItineraryLine.ordinal + 1, LayerRank.ItineraryFocus.ordinal)
        assertEquals(LayerRank.ItineraryRestriction.ordinal - 1, LayerRank.ItineraryFocus.ordinal)
    }
}
