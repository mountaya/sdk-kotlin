package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimeZoneViewportTest {
    @Test
    fun aZoneOpensOnItsViewport() {
        assertEquals(MapViewport(listOf(8.2275, 46.8182), 6.0), timeZoneViewport("Europe/Zurich"))
        assertEquals(MapViewport(listOf(2.3522, 46.2276), 5.0), timeZoneViewport("Europe/Paris"))
        assertEquals(MapViewport(listOf(-104.9903, 39.7392), 5.0), timeZoneViewport("America/Denver"))
        assertEquals(MapViewport(listOf(138.2529, 36.2048), 4.0), timeZoneViewport("Asia/Tokyo"))
        assertEquals(MapViewport(listOf(174.886, -40.9006), 4.0), timeZoneViewport("Pacific/Auckland"))
        assertEquals(MapViewport(listOf(37.9062, -0.0236), 5.0), timeZoneViewport("Africa/Nairobi"))
    }

    @Test
    fun aLinkARuntimeReportsAnswersTheZoneItNames() {
        assertEquals(MapViewport(listOf(78.9629, 20.5937), 4.0), timeZoneViewport("Asia/Kolkata"))
        assertEquals(timeZoneViewport("Asia/Kolkata"), timeZoneViewport("Asia/Calcutta"))
        assertEquals(MapViewport(listOf(31.1656, 48.3794), 4.0), timeZoneViewport("Europe/Kiev"))
        assertEquals(timeZoneViewport("Europe/Kyiv"), timeZoneViewport("Europe/Kiev"))
    }

    @Test
    fun aMergedZoneAnswersTheZoneThatHoldsItToday() {
        assertEquals(timeZoneViewport("Europe/Kyiv"), timeZoneViewport("Europe/Zaporozhye"))
        assertEquals(MapViewport(listOf(-79.3832, 43.6532), 5.0), timeZoneViewport("America/Montreal"))
        assertEquals(timeZoneViewport("America/Toronto"), timeZoneViewport("America/Montreal"))
    }

    @Test
    fun macquarieIslandIsAustralias() {
        assertEquals(MapViewport(listOf(133.7751, -25.2744), 3.0), timeZoneViewport("Antarctica/Macquarie"))
    }

    @Test
    fun aZoneOfNoCountryALinkNoRuntimeReportsAndANameThatIsNoZoneAnswerNothing() {
        assertNull(timeZoneViewport("Antarctica/Casey"))
        assertNull(timeZoneViewport("Etc/UTC"))
        assertNull(timeZoneViewport("US/Pacific"))
        assertNull(timeZoneViewport("Not/AZone"))
        assertNull(timeZoneViewport(""))
        assertNull(timeZoneViewport("constructor"))
    }

    /** A name is the platform's own, matched exactly. */
    @Test
    fun aNameIsMatchedExactlyLetterCaseIncluded() {
        assertNull(timeZoneViewport("europe/zurich"))
        assertNull(timeZoneViewport(" Europe/Zurich"))
        assertNull(timeZoneViewport("Europe/Zurich "))
    }

    /** The table holds the zones the fixture proves, and no other until the contract publishes one. */
    @Test
    fun everyZoneOfTheContractsTableOpensOnItsOwnViewport() {
        assertEquals(MapViewport(listOf(-5.5471, 7.54), 5.0), timeZoneViewport("Africa/Abidjan"))
        assertEquals(MapViewport(listOf(-177.1562, -13.7687), 10.0), timeZoneViewport("Pacific/Wallis"))
        assertEquals(timeZoneViewport("America/Toronto"), timeZoneViewport("America/Thunder_Bay"))
        assertEquals(timeZoneViewport("Asia/Ho_Chi_Minh"), timeZoneViewport("Asia/Saigon"))
    }
}
