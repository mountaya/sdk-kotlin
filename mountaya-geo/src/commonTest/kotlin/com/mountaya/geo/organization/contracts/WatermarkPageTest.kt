package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WatermarkPageTest {
    @Test
    fun aPageHoldsItsOriginAndWhetherAFrameHoldsIt() {
        val framed = WatermarkPage("https://app.mountaya.com", isTopLevel = false)

        assertEquals("https://app.mountaya.com", framed.origin)
        assertFalse(framed.isTopLevel)
    }
}
