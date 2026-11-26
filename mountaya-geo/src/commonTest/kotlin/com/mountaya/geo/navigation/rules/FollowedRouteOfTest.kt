package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(InternalMountayaApi::class)
class FollowedRouteOfTest {
    private val line = listOf(listOf(6.8652, 45.9237), listOf(6.8752, 45.9287), listOf(6.9271, 45.9682))

    @Test
    fun aRouteWithNoStepsDepartsOverItsWholeLineAndArrivesOnItsLastVertex() {
        val followed = followedRouteOf(NavigationRoute(line, duration = 6_300.0))

        assertEquals(
            listOf(
                NavigationStep(
                    0,
                    2,
                    RestSchema.ManeuverType.Depart,
                    null,
                    routeLengthMeters(line),
                    6_300.0,
                ),
                NavigationStep(2, 2, RestSchema.ManeuverType.Goal, null, 0.0, 0.0),
            ),
            followed?.steps,
        )
        assertSame(line, followed?.line)
    }

    /**
     * The departure's length is read off the measure the route holds, so a stepless route is measured once, as one
     * with steps is, and never once for its departure and again as it is followed.
     */
    @Test
    fun aRouteIsMeasuredOnceWhetherItsStepsTileItsLineOrItHasNone() {
        val stepless = MeasureProbe.measuring { followedRouteOf(NavigationRoute(line, duration = 6_300.0)) }
        val steps =
            listOf(
                NavigationStep(0, 1, RestSchema.ManeuverType.Depart, null, 1.0, 1.0),
                NavigationStep(1, 2, RestSchema.ManeuverType.Goal, null, 1.0, 1.0),
            )
        val stepped = MeasureProbe.measuring { followedRouteOf(NavigationRoute(line, steps)) }

        assertEquals(1, stepless.count { it === line }, "a stepless route")
        assertEquals(1, stepped.count { it === line }, "a route whose steps tile its line")
        assertEquals(cumulativeMeters(line).last(), followedRouteOf(NavigationRoute(line))?.steps?.first()?.distance)
    }

    @Test
    fun aRouteThatStatesNoDurationDepartsWithNone() {
        assertNull(followedRouteOf(NavigationRoute(line))?.steps?.first()?.duration)
    }

    @Test
    fun aRouteWithStepsThatTileItsLineIsFollowedByThemUnchanged() {
        val steps =
            listOf(
                NavigationStep(
                    0,
                    1,
                    RestSchema.ManeuverType.Depart,
                    "Route des Chavants",
                    8_400.0,
                    7_200.0,
                ),
                NavigationStep(1, 2, RestSchema.ManeuverType.Goal, null, 9_000.0, 7_560.0),
            )

        assertSame(steps, followedRouteOf(NavigationRoute(line, steps, 14_760.0))?.steps)
    }

    /** A list a host wrote by hand is held to the tiling rule too, and is refused rather than repaired. */
    @Test
    fun stepsThatDoNotTileTheLineAreRefusedAndTheRouteDepartsAndArrives() {
        val broken = listOf(NavigationStep(0, 1, RestSchema.ManeuverType.Depart, "Route des Chavants"))

        assertEquals(
            listOf(RestSchema.ManeuverType.Depart, RestSchema.ManeuverType.Goal),
            followedRouteOf(NavigationRoute(line, broken, 600.0))?.steps?.map { it.maneuver },
        )
        assertNull(followedRouteOf(NavigationRoute(line, broken, 600.0))?.steps?.first()?.name)
    }

    @Test
    fun aRouteOfOneVertexDepartsAndArrivesOnIt() {
        val followed = followedRouteOf(NavigationRoute(listOf(listOf(6.8652, 45.9237)), duration = 0.0))

        assertEquals(
            listOf(
                NavigationStep(0, 0, RestSchema.ManeuverType.Depart, null, 0.0, 0.0),
                NavigationStep(0, 0, RestSchema.ManeuverType.Goal, null, 0.0, 0.0),
            ),
            followed?.steps,
        )
    }

    @Test
    fun aRouteOfNoVertexIsNotFollowed() {
        assertNull(followedRouteOf(NavigationRoute(emptyList(), duration = 600.0)))
    }

    /** The departure's length is the line's as every walk of it reads it, a vertex naming no place bridged. */
    @Test
    fun theDeparturesLengthBridgesAVertexThatNamesNoPlace() {
        val bridged = listOf(line[0], listOf(Double.NaN, 45.0), line[2])

        assertEquals(
            routeLengthMeters(listOf(line[0], line[2])),
            followedRouteOf(NavigationRoute(bridged))?.steps?.first()?.distance,
        )
    }
}
