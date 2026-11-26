package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PlacedLayerTest {
    @Test
    fun aPlacedLayerIsItsIdAndTheRankItWasPlacedAt() {
        assertEquals(PlacedLayer("hut", 80), PlacedLayer("hut", 80))
        assertNotEquals(PlacedLayer("hut", 80), PlacedLayer("hut", 90))
    }
}
