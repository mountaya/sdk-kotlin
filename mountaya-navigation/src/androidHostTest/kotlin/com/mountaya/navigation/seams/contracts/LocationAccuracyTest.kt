package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationAccuracyTest {
    @Test
    fun aLocationIsReadAtFullAccuracyOrReduced() {
        assertEquals(listOf(LocationAccuracy.Full, LocationAccuracy.Reduced), LocationAccuracy.entries)
    }
}
