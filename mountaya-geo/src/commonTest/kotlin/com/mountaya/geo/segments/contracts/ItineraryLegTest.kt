package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItineraryLegTest {
    private fun leg(
        id: String = "s1",
        distance: Double? = 1_000.0,
        duration: Double? = 600.0,
        geometry: GeoJsonGeometry? = lineString(listOf(6.0, 45.0), listOf(6.01, 45.0)),
    ) = ItineraryLeg(
        id = id,
        memberIds = listOf("a", "b"),
        name = "Approach",
        color = "#e9501e",
        geometry = geometry,
        sectionDistance = distance,
        sectionAscent = 120,
        sectionDescent = 20,
        duration = duration,
        altitudeMin = 1_000,
        altitudeMax = 1_300,
    )

    @Test
    fun twoLegsAreEqualWhenEveryMemberIsAndHashAlike() {
        assertEquals(leg(), leg())
        assertEquals(leg().hashCode(), leg().hashCode())
        assertEquals(leg(distance = Double.NaN), leg(distance = Double.NaN))
        assertEquals(leg(distance = Double.NaN).hashCode(), leg(distance = Double.NaN).hashCode())
        assertNotEquals(leg(distance = -0.0), leg(distance = 0.0))
        assertNotEquals(leg(distance = 0.0), leg(distance = null))
        assertNotEquals(leg(), leg(duration = null))
        assertNotEquals(leg(), leg(geometry = null))
        assertEquals(
            leg(geometry = lineString(listOf(6.0, 45.0), listOf(Double.NaN, 45.0))),
            leg(geometry = lineString(listOf(6.0, 45.0), listOf(Double.NaN, 45.0))),
        )
    }

    /**
     * A Kotlin string compares by its code units, so `é` and `e` with a combining acute are two ids, as in
     * every SDK.
     */
    @Test
    fun anIdIsComparedCodeUnitForCodeUnit() {
        assertNotEquals(leg(id = "café"), leg(id = "café"))
    }

    @Test
    fun aLegBuiltWithNoFiguresStatesNoneAndStandsForItselfAlone() {
        val bare = ItineraryLeg(id = "s1", name = "s1")
        assertTrue(bare.memberIds.isEmpty())
        assertNull(bare.color)
        assertNull(bare.geometry)
        assertNull(bare.sectionDistance)
        assertNull(bare.sectionAscent)
        assertNull(bare.sectionDescent)
        assertNull(bare.duration)
        assertNull(bare.altitudeMin)
        assertNull(bare.altitudeMax)
    }
}
