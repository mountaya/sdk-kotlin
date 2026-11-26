package com.mountaya.client

import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class SystemDefaultsTest {
    @Test
    fun theClockIsTheWallClock() {
        val earliest = Instant.parse("2026-01-01T00:00:00Z")

        assertTrue(SystemDefaults.clock.now() > earliest)
    }

    @Test
    fun theRandomSourceDrawsFractionsFromZeroToOneAndVersionFourKeys() {
        val fractions = List(1_000) { SystemDefaults.random.unitDraw() }
        val keys = List(100) { SystemDefaults.random.cryptographicUuid().toString() }

        assertTrue(fractions.all { it >= 0.0 && it < 1.0 })
        assertTrue(fractions.toSet().size > 1)
        assertTrue(
            keys.all { Regex("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}").matches(it) },
        )
        assertEquals(100, keys.toSet().size)
    }

    @Test
    fun theDispatcherIsTheDefaultOne() {
        assertEquals(Dispatchers.Default, SystemDefaults.dispatcher)
    }
}
