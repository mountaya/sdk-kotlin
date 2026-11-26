package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PanelLayoutTest {
    @Test
    fun twoAreEqualWhereEveryMemberIs() {
        val layout = PanelLayout(340.0, false, 428.0, 52.0, 3, 2)

        assertEquals(layout, PanelLayout(340.0, false, 428.0, 52.0, 3, 2))
        assertEquals(layout.hashCode(), PanelLayout(340.0, false, 428.0, 52.0, 3, 2).hashCode())
        assertNotEquals(layout, PanelLayout(null, true, 412.0, 40.0, 3, 2))
    }
}
