package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ElevationSourceTest {
    @Test
    fun twoSourcesAreEqualWhereEveryMemberIs() {
        val source = ElevationSource(ElevationEncoding.Mapbox, 512, 14)

        assertEquals(source, ElevationSource(ElevationEncoding("mapbox"), 512, 14))
        assertEquals(source.hashCode(), ElevationSource(ElevationEncoding("mapbox"), 512, 14).hashCode())
        assertNotEquals(source, ElevationSource(null, 512, 14))
        assertNotEquals(source, ElevationSource(ElevationEncoding.Mapbox, 256, 14))
        assertNotEquals(source, ElevationSource(ElevationEncoding.Mapbox, 512, 15))
    }
}
