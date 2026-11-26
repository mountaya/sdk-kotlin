package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TimestampedSourceTest {
    @Test
    fun twoSourcesAreEqualWhereTheirIdAndTheirTilesAre() {
        val source = TimestampedSource("snow-depth", listOf("https://t/{z}?timestamp=a"))

        assertEquals(source, TimestampedSource("snow-depth", listOf("https://t/{z}?timestamp=a")))
        assertEquals(source.hashCode(), TimestampedSource("snow-depth", listOf("https://t/{z}?timestamp=a")).hashCode())
        assertNotEquals(source, TimestampedSource("snow", listOf("https://t/{z}?timestamp=a")))
        assertNotEquals(source, TimestampedSource("snow-depth", listOf("https://t/{z}?timestamp=b")))
    }
}
