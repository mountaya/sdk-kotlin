package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationStatusTest {
    /** The order the contract lists them in, from idle, rather than the order of their ids. */
    @Test
    fun theStatusesAreTheContractsSevenInItsOrder() {
        assertEquals(
            listOf("idle", "routing", "navigating", "off-route", "rerouting", "arrived", "failed"),
            NavigationStatus.entries.map { it.id },
        )
    }
}
