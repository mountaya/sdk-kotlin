package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LayersPanelCategoryTest {
    @Test
    fun twoCategoriesAreEqualWhereTheirSlugAndTheirNameAre() {
        val hazards = LayersPanelCategory("hazards", "Hazards")

        assertEquals(hazards, LayersPanelCategory("hazards", "Hazards"))
        assertEquals(hazards.hashCode(), LayersPanelCategory("hazards", "Hazards").hashCode())
        assertNotEquals(hazards, LayersPanelCategory("hazards", "Dangers"))
        assertNotEquals(hazards, LayersPanelCategory("weather", "Hazards"))
        assertEquals("hazards" to "Hazards", hazards.slug to hazards.name)
    }
}
