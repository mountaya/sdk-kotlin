package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HighlightPartTest {
    @Test
    fun twoPartsAreEqualWhereTheyHoldTheSameRunAndMatch() {
        assertEquals(HighlightPart("Mont", match = true), HighlightPart("Mont", match = true))
        assertNotEquals(HighlightPart("Mont", match = true), HighlightPart("Mont", match = false))
    }
}
