package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ItinerarySegmentsTest {
    private val climb = lineString(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.001, 1_100.0))
    private val drop = lineString(listOf(6.0, 45.001, 1_100.0), listOf(6.0, 45.002, 900.0))

    @Test
    fun theLegsReadInTheOrderTheChainRunsNotTheOrderItArrivedIn() {
        val legs =
            itinerarySegments(
                listOf(
                    restSegment("b", position = 1, geojson = drop),
                    restSegment("c", position = 2),
                    restSegment("a", position = 0, geojson = climb),
                ),
                emptyList(),
            )
        assertEquals(listOf("a", "b", "c"), legs.map { it.id })
    }

    /**
     * The figures were computed once over the full track, where an element would read a sampled copy, so
     * they are carried as the leg's own section.
     */
    @Test
    fun eachLegStatesTheServersFiguresAsItsOwnSection() {
        val segment =
            restSegment(
                "s1",
                geojson = climb,
                distance = 34_369.94,
                duration = 15_170.16,
                ascent = 1_531,
                descent = 1_408,
                altitude = 810 to 1_811,
                name = "Tour du Mont Blanc, stage 1",
                color = "#e9501e",
            )
        assertEquals(
            listOf(
                ItineraryLeg(
                    id = "s1",
                    name = "Tour du Mont Blanc, stage 1",
                    color = "#e9501e",
                    geometry = climb,
                    sectionDistance = 34_369.94,
                    sectionAscent = 1_531,
                    sectionDescent = 1_408,
                    duration = 15_170.16,
                    altitudeMin = 810,
                    altitudeMax = 1_811,
                ),
            ),
            itinerarySegments(listOf(segment), emptyList()),
        )
    }

    @Test
    fun aFigureTheServerDoesNotStateIsNoneAndSoIsAGeometry() {
        val leg = itinerarySegments(listOf(restSegment("s1")), emptyList()).single()
        assertNull(leg.geometry)
        assertNull(leg.sectionDistance)
        assertNull(leg.sectionAscent)
        assertNull(leg.sectionDescent)
        assertNull(leg.duration)
        assertNull(leg.altitudeMin)
        assertNull(leg.altitudeMax)
        assertNull(leg.color)
        assertTrue(leg.memberIds.isEmpty())
    }

    /** A zero is a figure the server stated: it never reads as none. */
    @Test
    fun aFigureOfZeroIsStatedAsZero() {
        val leg =
            itinerarySegments(
                listOf(restSegment("s1", distance = 0.0, ascent = 0, descent = 0)),
                emptyList(),
            ).single()
        assertEquals(0.0, leg.sectionDistance)
        assertEquals(0, leg.sectionAscent)
        assertEquals(0, leg.sectionDescent)
    }

    @Test
    fun aListedGroupCollapsesIntoOneLegCarryingItsNameItsColourAndItsSums() {
        val legs =
            itinerarySegments(
                listOf(
                    restSegment(
                        "a",
                        position = 0,
                        geojson = climb,
                        distance = 1_000.0,
                        duration = 600.0,
                        ascent = 100,
                        descent = 0,
                        altitude = 1_000 to 1_300,
                        groupId = "g",
                    ),
                    restSegment(
                        "b",
                        position = 1,
                        geojson = drop,
                        distance = 500.0,
                        duration = 300.0,
                        ascent = 50,
                        descent = 20,
                        altitude = 1_200 to 1_450,
                        groupId = "g",
                    ),
                    restSegment("c", position = 2, geojson = climb, distance = 200.0),
                ),
                listOf(restGroup("g", name = "The col", color = "#7048e8")),
            )
        assertEquals(2, legs.size)
        assertEquals(
            ItineraryLeg(
                id = "group:g",
                memberIds = listOf("a", "b"),
                name = "The col",
                color = "#7048e8",
                geometry = collectionOf(climb, drop),
                sectionDistance = 1_500.0,
                sectionAscent = 150,
                sectionDescent = 20,
                duration = 900.0,
                altitudeMin = 1_000,
                altitudeMax = 1_450,
            ),
            legs[0],
        )
        assertEquals("c", legs[1].id)
        assertTrue(legs[1].memberIds.isEmpty())
    }

    @Test
    fun aCollapsedLegNamesItsMembersInChainOrderAndNoOtherLegNamesAMember() {
        val legs =
            itinerarySegments(
                listOf(
                    restSegment("b", position = 2, geojson = drop, groupId = "g"),
                    restSegment("plain", position = 0, geojson = climb),
                    restSegment("a", position = 1, geojson = climb, groupId = "g"),
                    restSegment("orphan", position = 3, geojson = drop, groupId = "unlisted"),
                ),
                listOf(restGroup("g", name = "The col")),
            )
        assertEquals(listOf("plain", "group:g", "orphan"), legs.map { it.id })
        assertEquals(listOf(emptyList(), listOf("a", "b"), emptyList()), legs.map { it.memberIds })
    }

    @Test
    fun aSegmentOfAGroupTheItineraryDoesNotListIsALegOfItsOwn() {
        assertEquals(
            listOf(ItineraryLeg(id = "s1", name = "s1", geometry = climb, sectionDistance = 1_000.0)),
            itinerarySegments(
                listOf(restSegment("s1", geojson = climb, distance = 1_000.0, groupId = "g9")),
                emptyList(),
            ),
        )
    }

    /**
     * A collapsed run lays its members' lines end to end. Losing them would give the leg no span and
     * slide every later leg's stretch back.
     */
    @Test
    fun aCollapsedLegKeepsItsMembersLinesSoItsSpanMeasuresItsOwnStretch() {
        val legs =
            itinerarySegments(
                listOf(
                    restSegment("a", position = 0, geojson = climb, groupId = "g"),
                    restSegment("b", position = 1, geojson = drop, groupId = "g"),
                ),
                listOf(restGroup("g")),
            )
        assertTrue(buildElevationSeries(legs.first().geometry).totalDistance > 0)
    }

    /**
     * A real segment may carry an id that reads like a group's: only a listed group's members are
     * collapsed, so its `memberIds` is empty.
     */
    @Test
    fun aSegmentWhoseOwnIdReadsLikeAGroupsNamesNoMember() {
        val legs =
            itinerarySegments(
                listOf(restSegment("group:g", position = 0), restSegment("a", position = 1, groupId = "g")),
                listOf(restGroup("g")),
            )
        assertEquals(listOf("group:g", "group:g"), legs.map { it.id })
        assertEquals(listOf(emptyList(), listOf("a")), legs.map { it.memberIds })
    }

    @Test
    fun anItineraryOfNoSegmentHasNoLegWithOrWithoutGroups() {
        assertTrue(itinerarySegments(emptyList(), emptyList()).isEmpty())
        assertTrue(itinerarySegments(emptyList(), listOf(restGroup("g"))).isEmpty())
    }
}
