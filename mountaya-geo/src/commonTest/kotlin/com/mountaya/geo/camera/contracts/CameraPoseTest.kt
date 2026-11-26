package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CameraPoseTest {
    private val pose = CameraPose(listOf(6.86, 45.92), zoom = 12.0, pitch = 60.0, bearing = 30.0)

    @Test
    fun twoPosesAreEqualWhereEveryFieldIs() {
        assertEquals(pose, CameraPose(listOf(6.86, 45.92), 12.0, 60.0, 30.0))
        assertEquals(pose.hashCode(), CameraPose(listOf(6.86, 45.92), 12.0, 60.0, 30.0).hashCode())
        assertNotEquals(pose, CameraPose(listOf(6.86, 45.921), 12.0, 60.0, 30.0))
        assertNotEquals(pose, CameraPose(listOf(6.86, 45.92), 11.8, 60.0, 30.0))
        assertNotEquals(pose, CameraPose(listOf(6.86, 45.92), 12.0, 52.0, 30.0))
        assertNotEquals(pose, CameraPose(listOf(6.86, 45.92), 12.0, 60.0, 31.0))
    }

    @Test
    fun twoPosesCompareTheirNumbersAsTheSameNumbers() {
        assertEquals(
            CameraPose(listOf(0.0, 0.0), Double.NaN, 0.0, 0.0),
            CameraPose(listOf(0.0, 0.0), Double.NaN, 0.0, 0.0),
        )
        assertNotEquals(CameraPose(listOf(0.0, 0.0), 12.0, 0.0, 0.0), CameraPose(listOf(0.0, 0.0), 12.0, 0.0, -0.0))
    }

    @Test
    fun eachFieldIsNamedByWhatItHolds() {
        assertEquals("CameraPose(center=[6.86, 45.92], zoom=12.0, pitch=60.0, bearing=30.0)", pose.toString())
    }
}
