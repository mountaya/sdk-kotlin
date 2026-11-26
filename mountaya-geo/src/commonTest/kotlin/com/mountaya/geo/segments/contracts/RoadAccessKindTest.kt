package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoadAccessKindTest {
    @Test
    fun theKindsAreSpelledByTheirIdsMostSevereFirst() {
        assertEquals(
            listOf("forbidden", "private", "permit", "service", "local-access", "restricted"),
            RoadAccessKind.entries.map { it.id },
        )
    }

    @Test
    fun ofTwoKindsTheMoreSevereIsTheLesser() {
        assertTrue(RoadAccessKind.Forbidden < RoadAccessKind.Private)
        assertTrue(RoadAccessKind.LocalAccess < RoadAccessKind.Restricted)
        assertEquals(RoadAccessKind.Private, minOf(RoadAccessKind.LocalAccess, RoadAccessKind.Private))
    }
}
