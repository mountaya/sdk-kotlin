package com.mountaya.elements

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals

class MapCornerTest {
    @Test
    fun aCornerStandsWhereTheReaderSeesItWhicheverWayTheTextRuns() {
        val stack = IntSize(10, 10)
        val map = IntSize(100, 100)
        val placed =
            mapOf(
                MapCorner.TopRight to IntOffset(90, 0),
                MapCorner.TopLeft to IntOffset(0, 0),
                MapCorner.BottomRight to IntOffset(90, 90),
                MapCorner.BottomLeft to IntOffset(0, 90),
            )
        for ((corner, offset) in placed) {
            assertEquals(offset, corner.alignment.align(stack, map, LayoutDirection.Ltr), "$corner, left to right")
            assertEquals(offset, corner.alignment.align(stack, map, LayoutDirection.Rtl), "$corner, right to left")
        }
        assertEquals(
            listOf(MapCorner.TopRight, MapCorner.TopLeft, MapCorner.BottomRight, MapCorner.BottomLeft),
            MapCorner.entries,
        )
    }
}
