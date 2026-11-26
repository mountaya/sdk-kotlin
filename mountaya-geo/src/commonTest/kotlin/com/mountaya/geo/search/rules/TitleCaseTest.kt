package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TitleCaseTest {
    @Test
    fun aRawValueReadsWithSpacesAndACapital() {
        assertEquals("Demanding mountain hiking", titleCase("demanding_mountain_hiking"))
        assertEquals("Path", titleCase("path"))
        assertEquals("", titleCase(""))
    }

    @Test
    fun aWordIsTheHostsElseTheFallback() {
        val label = SearchLabel.Highway("path")
        assertEquals("Path", wordFor(label, "Path", SearchWording()))
        assertEquals("Sentier", wordFor(label, "Path", SearchWording(resolveLabel = { "Sentier" })))
        assertEquals("Path", wordFor(label, "Path", SearchWording(resolveLabel = { "" })))
    }

    @Test
    fun tokensJoinWithAMiddleDotAndATokenRepeatingTheTitleIsLeftOut() {
        assertEquals("Hut · 1.2 km", joinTokens("Hut", "", null, "1.2 km"))
        assertEquals("", suppressIfMatchesName("Train station", "train STATION"))
        assertEquals("Hut", suppressIfMatchesName("Hut", "Refuge"))
    }

    @Test
    fun aCountryIsItsNameAndItsFlag() {
        assertEquals("France 🇫🇷", countryToken("fr", "France"))
        assertEquals("🇨🇭", countryToken("CH", null))
        assertEquals("Suisse", countryToken("C1", "Suisse"))
        assertEquals("", countryToken("ßa", null))
        assertEquals("", countryToken(null, ""))
    }

    /** An altitude is no part of the place a payload moves a map to, in every SDK. */
    @Test
    fun aPositionIsItsLongitudeAndLatitudeAndAPositionOrABoxNamesAPlaceOrNone() {
        assertEquals(listOf(6.0, 45.0), placeOrNull(listOf(6.0, 45.0)))
        assertEquals(listOf(6.0, 45.0), placeOrNull(listOf(6.0, 45.0, 1200.0)))
        assertEquals(listOf(6.0, 45.0), placeOrNull(listOf(6.0, 45.0, Double.NaN)))
        assertNull(placeOrNull(listOf(6.0)))
        assertNull(placeOrNull(listOf(Double.NaN, 45.0, 1200.0)))
        assertNull(boxOrNull(listOf(6.0, 45.0, 7.0)))
        assertNull(boxOrNull(listOf(6.0, 45.0, 7.0, Double.NaN)))
        assertEquals(SearchRowSecondary.Text("x"), textSecondary("x"))
        assertNull(textSecondary(""))
    }
}
