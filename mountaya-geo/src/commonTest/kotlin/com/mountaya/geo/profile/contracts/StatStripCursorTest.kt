package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class StatStripCursorTest {
    @Test
    fun twoCursorsAreEqualWhereEveryNumberIsAMissingGradeOnlyToAnother() {
        val cursor = StatStripCursor(5_000.0, 1_400.0, 6.2)

        assertEquals(StatStripCursor(5_000.0, 1_400.0, 6.2), cursor)
        assertEquals(StatStripCursor(5_000.0, 1_400.0, 6.2).hashCode(), cursor.hashCode())
        assertNotEquals(StatStripCursor(5_000.0, 1_400.0, null), cursor)
    }
}
