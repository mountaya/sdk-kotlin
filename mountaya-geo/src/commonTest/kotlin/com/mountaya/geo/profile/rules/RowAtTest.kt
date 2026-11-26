package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RowAtTest {
    private val rows = listOf(ProfileRange(0.0, 500.0), ProfileRange(500.0, 1_200.0), ProfileRange(1_200.0, 1_800.0))

    @Test
    fun theRowUnderACursorTheLaterAtASharedMeterAndTheLastAtTheEnd() {
        assertEquals(rows[1], rowAt(rows, 800.0))
        assertEquals(rows[1], rowAt(rows, 500.0))
        assertEquals(rows[2], rowAt(rows, 1_800.0))
        assertNull(rowAt(rows, 1_900.0))
        assertNull(rowAt(emptyList<ProfileRange>(), 0.0))
    }

    @Test
    fun aRowOfNoLengthIsNeverUnderACursorAndARowIsAnsweredAsGiven() {
        val withTransfer =
            listOf(
                ProfileClass(0.0, 500.0, "", "s1"),
                ProfileClass(500.0, 500.0, "", "transfer"),
                ProfileClass(500.0, 900.0, "", "s2"),
            )

        assertEquals("s2", rowAt(withTransfer, 500.0)?.label)
        val overlapping = listOf(ProfileRange(0.0, 600.0), ProfileRange(400.0, 1_000.0))
        assertEquals(ProfileRange(400.0, 1_000.0), rowAt(overlapping, 700.0))
    }
}
