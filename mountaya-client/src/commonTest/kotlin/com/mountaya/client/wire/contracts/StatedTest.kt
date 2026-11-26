package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class StatedTest {
    @Test
    fun aStatedNullIsAValueAndNotAnAbsence() {
        val held: Stated<String?>? = Stated(null)
        val unmoved: Stated<String?>? = null
        assertNull(held?.value)
        assertNotEquals(unmoved, held)
        assertEquals("Stated(null)", Stated(null).toString())
    }

    @Test
    fun twoStatementsOfOneValueAreEqual() {
        assertEquals(Stated("Alps"), Stated("Alps"))
        assertEquals(Stated("Alps").hashCode(), Stated("Alps").hashCode())
        assertNotEquals(Stated("Alps"), Stated("Jura"))
        assertEquals(0, Stated(null).hashCode())
    }
}
