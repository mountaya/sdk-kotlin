package com.mountaya.geo

import com.mountaya.client.GeoJsonLineString
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveWaypointsTest {
    private val loop =
        GeoJsonLineString(
            listOf(
                listOf(6.8652, 45.9237),
                listOf(6.8852, 45.9337),
                listOf(6.9052, 45.9237),
                listOf(6.8852, 45.9137),
                listOf(6.8652, 45.92372),
            ),
        )
    private val outAndBack =
        GeoJsonLineString(
            listOf(
                listOf(6.8652, 45.9237),
                listOf(6.8752, 45.9237),
                listOf(6.8852, 45.9237),
                listOf(6.8952, 45.9287),
                listOf(6.9052, 45.9237),
                listOf(6.8952, 45.9187),
                listOf(6.8852, 45.9237),
                listOf(6.8752, 45.9237),
                listOf(6.8652, 45.9237),
            ),
        )

    private fun placed(
        geometry: GeoJsonLineString,
        vararg waypoints: ProfileWaypoint,
    ) = resolveWaypoints(geometry, waypoints.toList()).map { it.key to it.distance }

    @Test
    fun aLoopsStartAndFinishStandAtZeroAndAtTheTotal() {
        val start = ProfileWaypoint.AtPosition("start", listOf(6.8652, 45.9237))
        val summit = ProfileWaypoint.AtPosition("summit", listOf(6.9052, 45.9237))
        val finish = ProfileWaypoint.AtPosition("finish", listOf(6.8652, 45.9237))
        val result = placed(loop, start, summit, finish)

        assertEquals(listOf("start#0", "summit#0", "finish#0"), result.map { it.first })
        assertEquals(0.0, result[0].second)
        assertEquals(7_620.575694388079, result[2].second, 1e-6)
    }

    @Test
    fun aPlacePassedTwiceAndClaimedOnceIsMarkedAtEachPass() {
        val result = placed(outAndBack, ProfileWaypoint.AtPosition("hut", listOf(6.8752, 45.9237)))

        assertEquals(listOf("hut#0", "hut#1"), result.map { it.first })
        assertEquals(773.4893547445723, result[0].second, 1e-6)
        assertEquals(6_130.75639425873, result[1].second, 1e-6)
    }

    @Test
    fun aDistanceIsOnTheAxisAlreadyAndOneThatIsNoNumberPlacesNothing() {
        assertEquals(listOf("marker#0" to 1_200.0), placed(loop, ProfileWaypoint.AtDistance("marker", 1_200.0)))
        assertEquals(emptyList(), placed(loop, ProfileWaypoint.AtDistance("marker", Double.NaN)))
    }

    @Test
    fun aPlaceOffTheLineStandsAtItsNearestApproachAndOneFarOffOrNowhereIsNotPlaced() {
        assertEquals(
            1_676.7740764172786,
            placed(outAndBack, ProfileWaypoint.AtPosition("village", listOf(6.8852, 45.9257))).single().second,
            1e-6,
        )
        assertEquals(emptyList(), placed(outAndBack, ProfileWaypoint.AtPosition("elsewhere", listOf(7.5, 46.5))))
        assertEquals(
            emptyList(),
            placed(outAndBack, ProfileWaypoint.AtPosition("nowhere", listOf(Double.NaN, 45.9237))),
        )
        assertEquals(
            emptyList(),
            resolveWaypoints(null, listOf(ProfileWaypoint.AtPosition("hut", listOf(6.8752, 45.9237)))),
        )
    }

    @Test
    fun aPlaceListedAfterALaterOneFallsBackToItsLastPass() {
        val result =
            placed(
                outAndBack,
                ProfileWaypoint.AtPosition("turn", listOf(6.9052, 45.9237)),
                ProfileWaypoint.AtPosition("start", listOf(6.8652, 45.9237)),
                ProfileWaypoint.AtPosition("fork", listOf(6.8752, 45.9237)),
            )

        assertEquals(listOf("start#0", "fork#0", "turn#0", "fork#1", "start#1"), result.map { it.first })
    }

    /** Two waypoints of one id are two waypoints, and their keys count that id's passes across both. */
    @Test
    fun twoWaypointsGivenOneIdAreTwoAndTheirKeysStayUnique() {
        val result = placed(loop, ProfileWaypoint.AtDistance("km", 1_000.0), ProfileWaypoint.AtDistance("km", 2_000.0))

        assertEquals(listOf("km#0" to 1_000.0, "km#1" to 2_000.0), result)
        assertEquals(60.0 to 2_000.0, WAYPOINT_PROXIMITY_METERS to WAYPOINT_BELONGING_METERS)
    }

    @Test
    fun eachPlacementSaysWhetherItIsItsWaypointsPrimaryPass() {
        val passes = waypointPasses(outAndBack, listOf(ProfileWaypoint.AtPosition("hut", listOf(6.8752, 45.9237))))

        assertEquals(listOf("hut#0", "hut#1"), passes.map { it.key })
        assertEquals(listOf(true, false), passes.map { it.isPrimary })
        assertEquals(listOf(0, 0), passes.map { it.listed })

        val loopPasses =
            waypointPasses(
                loop,
                listOf(
                    ProfileWaypoint.AtPosition("start", listOf(6.8652, 45.9237)),
                    ProfileWaypoint.AtPosition("summit", listOf(6.9052, 45.9237)),
                    ProfileWaypoint.AtPosition("finish", listOf(6.8652, 45.9237)),
                ),
            )

        assertEquals(listOf("start#0", "summit#0", "finish#0"), loopPasses.map { it.key })
        assertEquals(listOf(true, true, true), loopPasses.map { it.isPrimary })
        assertEquals(listOf(0, 1, 2), loopPasses.map { it.listed })
    }
}
