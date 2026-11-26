package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class GeolocationRefusalTest {
    @Test
    fun eachReasonIsSpelledAsTheContractSpellsIt() {
        assertEquals(listOf("denied", "no_api", "unavailable"), GeolocationRefusal.entries.map { it.id })
    }
}
