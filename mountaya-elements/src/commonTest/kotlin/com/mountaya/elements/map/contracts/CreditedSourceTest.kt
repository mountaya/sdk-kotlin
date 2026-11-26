package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CreditedSourceTest {
    @Test
    fun aSourceIsItsCreditWhichMayBeAbsentAndWhetherALayerDrawsIt() {
        assertNotEquals(CreditedSource(null, isDrawn = true), CreditedSource("", isDrawn = true))
        assertNotEquals(CreditedSource("a", isDrawn = false), CreditedSource("a", isDrawn = true))
        assertEquals(CreditedSource("a", isDrawn = true), CreditedSource("a", isDrawn = true))
    }
}
