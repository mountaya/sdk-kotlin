package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoadAccessColorTest {
    @Test
    fun eachKindIsDrawnInItsOwnOpaqueColour() {
        assertEquals(
            listOf("#e03131", "#e8590c", "#9c36b5", "#1971c2", "#0c8599", "#495057"),
            RoadAccessKind.entries.map { roadAccessColor(it).hex },
        )
        assertTrue(RoadAccessKind.entries.all { roadAccessColor(it).opacity == 1.0 })
        assertEquals(
            6,
            RoadAccessKind.entries
                .map { roadAccessColor(it).hex }
                .toSet()
                .size,
        )
    }
}
