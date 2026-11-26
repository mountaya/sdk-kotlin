package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.time.Clock

class SystemClockTest {
    @Test
    fun theElementsReadTheSystemsWallClockWhereNoHostHandsAnother() {
        assertSame<Clock>(Clock.System, systemClock)
    }
}
