package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LayersPanelHeadingTest {
    @Test
    fun twoHeadingsAreEqualWhereTheyReadTheSameWordByItsPath() {
        assertEquals(LayersPanelHeading("layers.terrain"), LayersPanelHeading("layers.terrain"))
        assertEquals(LayersPanelHeading("layers.time").hashCode(), LayersPanelHeading("layers.time").hashCode())
        assertNotEquals(LayersPanelHeading("layers.terrain"), LayersPanelHeading("terrain"))
        assertEquals("layers.overlays", LayersPanelHeading("layers.overlays").word)
    }
}
