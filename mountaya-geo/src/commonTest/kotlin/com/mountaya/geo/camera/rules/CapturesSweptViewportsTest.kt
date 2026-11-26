package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CapturesSweptViewportsTest {
    private val swept =
        listOf(
            CameraPose(listOf(6.86, 45.92), zoom = 12.0, pitch = 60.0, bearing = 30.0),
            CameraPose(listOf(6.87, 45.93), zoom = 12.5, pitch = 60.0, bearing = 35.0),
        )

    @Test
    fun theSameFramesInFreshObjectsAreCaptured() {
        val copy = swept.map { CameraPose(it.center.toList(), it.zoom, it.pitch, it.bearing) }

        assertTrue(capturesSweptViewports(swept, copy))
        assertTrue(capturesSweptViewports(emptyList(), emptyList()))
    }

    @Test
    fun aFrameMoreOrLessIsNotCaptured() {
        assertFalse(capturesSweptViewports(swept, swept + swept[0]))
        assertFalse(capturesSweptViewports(swept, swept.take(1)))
        assertFalse(capturesSweptViewports(swept, emptyList()))
    }

    /** A clearance pass that lowered a frame shows ground the sweep never framed. */
    @Test
    fun aFrameWithAnyFieldChangedIsNotCaptured() {
        val second = swept[1]
        val changes =
            listOf(
                CameraPose(second.center, second.zoom, 52.0, second.bearing),
                CameraPose(second.center, 12.4, second.pitch, second.bearing),
                CameraPose(second.center, second.zoom, second.pitch, 36.0),
                CameraPose(listOf(6.87, 45.931), second.zoom, second.pitch, second.bearing),
            )
        for (changed in changes) {
            assertFalse(capturesSweptViewports(swept, listOf(swept[0], changed)), "for $changed")
        }
    }

    @Test
    fun theFramesAreComparedInOrder() {
        assertFalse(capturesSweptViewports(swept, swept.reversed()))
    }
}
