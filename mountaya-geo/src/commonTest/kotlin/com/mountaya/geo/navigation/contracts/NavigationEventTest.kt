package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationEventTest {
    @Test
    fun theEventsAreTheContractsEightInItsOrder() {
        assertEquals(
            listOf("start", "route-received", "route-failed", "on-route", "off-route", "reroute", "arrived", "stop"),
            NavigationEvent.entries.map { it.id },
        )
    }
}
