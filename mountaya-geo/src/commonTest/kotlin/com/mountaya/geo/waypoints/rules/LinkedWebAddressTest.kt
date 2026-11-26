package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LinkedWebAddressTest {
    @Test
    fun aStoredAddressIsReadPastTheWhiteSpaceAtItsEnds() {
        assertEquals("https://refuge.example/topo", linkedWebAddress("  https://refuge.example/topo "))
        assertEquals("https://refuge.example/topo", linkedWebAddress("https://refuge.example/topo\n"))
        assertEquals("https://refuge.example/topo", linkedWebAddress("﻿https://refuge.example/topo"))
    }

    @Test
    fun anAddressThatIsNoWebAddressIsNeverLinked() {
        assertNull(linkedWebAddress(" javascript:alert(1)"))
        assertNull(linkedWebAddress("ask at the bar"))
        assertNull(linkedWebAddress(""))
        assertNull(linkedWebAddress(null))
    }
}
