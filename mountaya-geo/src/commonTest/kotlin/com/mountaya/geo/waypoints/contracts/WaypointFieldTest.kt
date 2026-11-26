package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class WaypointFieldTest {
    @Test
    fun twoFieldsAreEqualWhereEveryTextIsWrittenAlikeOrAbsentOnBoth() {
        val field = WaypointField("page", "Page", "https://a.example", "https://a.example")

        assertEquals(WaypointField("page", "Page", "https://a.example", "https://a.example"), field)
        assertNotEquals(WaypointField("page", "Page", "https://a.example"), field)
        assertNull(WaypointField("k", "l", "v").href)
        assertEquals(
            WaypointField("page", "Page", "https://a.example", "https://a.example").hashCode(),
            field.hashCode(),
        )
    }
}
