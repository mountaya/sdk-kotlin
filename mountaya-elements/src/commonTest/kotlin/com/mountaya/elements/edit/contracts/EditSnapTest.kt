package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class EditSnapTest {
    @Test
    fun aSnapHoldsThePositionThePointTakesAndWhetherTheMagnetMovedIt() {
        val snap = EditSnap(listOf(6.86, 45.83, 1035.0), isSnapped = false)

        assertEquals(listOf(6.86, 45.83, 1035.0), snap.position)
        assertFalse(snap.isSnapped)
    }
}
