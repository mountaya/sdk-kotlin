package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AnalysisHandleTest {
    @Test
    fun aHandleCarriesTheIdAResultIsDrawnUnderAndTheResultEqualWhereBothAre() {
        val handle = AnalysisHandle("analysis:isochrones:0", 3)
        assertEquals("analysis:isochrones:0", handle.id)
        assertEquals(3, handle.result)
        assertEquals(AnalysisHandle("analysis:isochrones:0", 3), handle)
        assertEquals(AnalysisHandle("analysis:isochrones:0", 3).hashCode(), handle.hashCode())
        assertNotEquals(AnalysisHandle("analysis:isochrones:1", 3), handle)
        assertNotEquals(AnalysisHandle("analysis:isochrones:0", 4), handle)
    }
}
