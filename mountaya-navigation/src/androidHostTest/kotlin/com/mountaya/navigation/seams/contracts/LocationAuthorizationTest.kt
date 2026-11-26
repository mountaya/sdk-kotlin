package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class LocationAuthorizationTest {
    @Test
    fun theFiveAuthorizationsAreSdkDartsInItsOrder() {
        assertEquals(
            listOf("NotDetermined", "Restricted", "Denied", "WhenInUse", "Always"),
            LocationAuthorization.entries.map { it.name },
        )
    }
}
