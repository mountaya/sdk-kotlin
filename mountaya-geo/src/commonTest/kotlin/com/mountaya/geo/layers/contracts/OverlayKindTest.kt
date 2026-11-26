package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayKindTest {
    @Test
    fun anOverlayIsClassicOrAnEnrichment() {
        assertEquals(listOf(OverlayKind.Classic, OverlayKind.Enrichment), OverlayKind.entries)
    }
}
