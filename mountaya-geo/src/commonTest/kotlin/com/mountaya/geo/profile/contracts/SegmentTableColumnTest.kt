package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SegmentTableColumnTest {
    @Test
    fun eachColumnIsSpelledAsTheContractSpellsIt() {
        assertEquals(
            listOf("segment", "profile", "distance", "elevation", "altitude", "duration", "difficulty"),
            SegmentTableColumn.entries.map { it.id },
        )
    }
}
