package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SettableTest {
    @Test
    fun nullSetIsNotNothingSet() {
        assertNotEquals<Settable<String?>>(Settable.Present(null), Settable.Absent)
        assertNull(Settable.Present(null).valueOrNull)
        assertNull(Settable.Absent.valueOrNull)
    }

    @Test
    fun aValueSetIsThatValue() {
        assertEquals("Alps", Settable.Present("Alps").valueOrNull)
        assertEquals(Settable.Present("Alps"), Settable.Present("Alps"))
        assertEquals("Present(Alps)", Settable.Present("Alps").toString())
        assertEquals("Absent", Settable.Absent.toString())
    }

    @Test
    fun whatWasSetIsStatedNullIncludedAndWhatWasNotIsNot() {
        assertEquals(Stated("Alps"), Settable.Present("Alps").stated)
        assertEquals(Stated(null), Settable.Present<String?>(null).stated)
        assertNull(Settable.Absent.stated)
    }
}
