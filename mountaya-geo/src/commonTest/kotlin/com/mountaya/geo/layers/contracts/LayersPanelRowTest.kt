package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LayersPanelRowTest {
    @Test
    fun twoRowsAreEqualWhereEveryMemberIs() {
        val row = LayersPanelRow("slopes", "Pente", isChecked = true, mark = LayersPanelMark.Tick)

        assertEquals(row, LayersPanelRow("slopes", "Pente", isChecked = true, mark = LayersPanelMark.Tick))
        assertEquals(
            row.hashCode(),
            LayersPanelRow("slopes", "Pente", isChecked = true, mark = LayersPanelMark.Tick).hashCode(),
        )
        assertNotEquals(row, LayersPanelRow("slopes", "Pente", isChecked = true, mark = LayersPanelMark.Lock))
        assertNotEquals(row, LayersPanelRow("slopes", "Pente", isChecked = false, mark = LayersPanelMark.Tick))
        assertNotEquals(
            LayersPanelRow("a", "é", isChecked = false, mark = null),
            LayersPanelRow("a", "é", isChecked = false, mark = null),
        )
    }
}
