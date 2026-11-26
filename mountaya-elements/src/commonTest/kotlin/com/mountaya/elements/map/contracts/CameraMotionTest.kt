package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class CameraMotionTest {
    @Test
    fun aMotionIsAJumpAnEaseOfADurationOrTheRenderersOwnOrAFlight() {
        assertNotEquals<CameraMotion>(CameraMotion.Ease(null), CameraMotion.Ease(Duration.ZERO))
        assertEquals<CameraMotion>(CameraMotion.Ease(250.milliseconds), CameraMotion.Ease(250.milliseconds))
        assertEquals(CameraMotion.Ease(250.milliseconds).hashCode(), CameraMotion.Ease(250.milliseconds).hashCode())
        assertEquals<CameraMotion>(CameraMotion.Ease(), CameraMotion.Ease(null))
        assertNotEquals<CameraMotion>(CameraMotion.Jump, CameraMotion.Fly)
    }
}
