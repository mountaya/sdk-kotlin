package com.mountaya.elements

import com.mountaya.geo.CameraPose
import com.mountaya.geo.ZoomBounds
import kotlin.test.Test
import kotlin.test.assertEquals

class ControlMoveTargetTest {
    private val world = ZoomBounds(-2.0, 22.0)

    private fun camera(
        zoom: Double,
        bearing: Double = 0.0,
        pitch: Double = 0.0,
    ): CameraPose = CameraPose(listOf(6.86, 45.83), zoom, pitch, bearing)

    @Test
    fun aZoomStepMovesOneLevelFromTheZoomTheCameraStandsAtUnrounded() {
        assertEquals(13.4, controlMoveTarget(camera(12.4), world, ControlMove.ZoomIn).zoom)
        assertEquals(11.4, controlMoveTarget(camera(12.4), world, ControlMove.ZoomOut).zoom)
        assertEquals(-0.5, controlMoveTarget(camera(0.5), world, ControlMove.ZoomOut).zoom)
    }

    @Test
    fun aStepPastAnEndOfTheWindowStopsAtItAndOneAtAnEndStays() {
        assertEquals(22.0, controlMoveTarget(camera(21.6), world, ControlMove.ZoomIn).zoom)
        assertEquals(14.0, controlMoveTarget(camera(14.0), ZoomBounds(8.0, 14.0), ControlMove.ZoomIn).zoom)
        assertEquals(2.0, controlMoveTarget(camera(2.5), ZoomBounds(2.0, 22.0), ControlMove.ZoomOut).zoom)
    }

    @Test
    fun eachMoveKeepsEveryOtherMemberAndAResetTurnsTheBearingAndThePitchTo0() {
        val tilted = camera(9.0, bearing = -20.0, pitch = 30.0)

        assertEquals(
            CameraPose(listOf(6.86, 45.83), 10.0, 30.0, -20.0),
            controlMoveTarget(tilted, world, ControlMove.ZoomIn),
        )
        assertEquals(
            camera(13.2),
            controlMoveTarget(camera(13.2, bearing = 37.0, pitch = 45.0), world, ControlMove.ResetNorth),
        )
        assertEquals(camera(13.2), controlMoveTarget(camera(13.2), world, ControlMove.ResetNorth))
    }
}
