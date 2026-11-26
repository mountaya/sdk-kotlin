package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RefreshDelayTest {
    @Test
    fun aDelayCarriesWhetherATimerIsArmedForIt() {
        val armed = RefreshDelay(30.seconds, isArmed = true)
        val spent = RefreshDelay((-2).seconds, isArmed = false)

        assertEquals(30.seconds, armed.delay)
        assertTrue(armed.isArmed)
        assertEquals((-2).seconds, spent.delay)
        assertFalse(spent.isArmed)
    }
}
