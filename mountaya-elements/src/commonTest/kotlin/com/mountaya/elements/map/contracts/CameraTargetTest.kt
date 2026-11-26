package com.mountaya.elements

import com.mountaya.geo.CameraPose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class CameraTargetTest {
    private val camera = CameraPose(listOf(6.86, 45.83), 12.0, 30.0, 15.0)

    @Test
    fun aTargetStatesItsMembersEachNullKeepingTheCamerasOwn() {
        val target = CameraTarget(center = listOf(7.0, 46.0), pitch = 0.0)

        assertNull(target.zoom)
        assertNull(target.bearing)
        assertEquals(CameraPose(listOf(7.0, 46.0), 12.0, 0.0, 15.0), target.applied(camera))
        assertEquals(camera, CameraTarget().applied(camera))
        assertEquals(
            listOf(197.5, 0.0),
            CameraTarget(center = listOf(197.5, 0.0)).applied(camera).center,
            "a centre past the antimeridian is kept as written",
        )
    }

    @Test
    fun twoTargetsAreEqualWhereTheyStateTheSameMembersEachTheSameNumber() {
        assertEquals(CameraTarget(zoom = Double.NaN), CameraTarget(zoom = Double.NaN))
        assertEquals(CameraTarget(zoom = Double.NaN).hashCode(), CameraTarget(zoom = Double.NaN).hashCode())
        assertNotEquals(CameraTarget(zoom = 0.0), CameraTarget(zoom = -0.0))
        assertNotEquals(CameraTarget(zoom = 0.0), CameraTarget())
        assertNotEquals(CameraTarget(center = listOf(6.0, 45.0)), CameraTarget(center = listOf(6.0, 45.0, 0.0)))
        assertEquals(CameraTarget(bearing = 1.0, pitch = 2.0), CameraTarget(bearing = 1.0, pitch = 2.0))
    }
}
