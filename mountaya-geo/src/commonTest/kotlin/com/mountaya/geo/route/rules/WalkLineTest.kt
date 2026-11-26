package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class WalkLineTest {
    private val unit: (List<Double>, List<Double>) -> Double = { a, b -> b[0] - a[0] }

    @Test
    fun eachLegIsAddedFromTheStart() {
        val told = mutableListOf<Pair<Double, Int>>()
        val end =
            walkLine(listOf(listOf(0.0, 0.0), listOf(2.0, 0.0), listOf(5.0, 0.0)), 10.0, unit) { at, _, index ->
                told +=
                    at to index
            }

        assertEquals(15.0, end)
        assertEquals(listOf(10.0 to 0, 12.0 to 1, 15.0 to 2), told)
    }

    @Test
    fun aVertexThatNamesNoPlaceIsToldAsNoneAtTheLastDistanceAndBridged() {
        val told = mutableListOf<Pair<Double, Boolean>>()
        val end =
            walkLine(
                listOf(listOf(Double.NaN, 0.0), listOf(1.0, 0.0), listOf(4.0), listOf(4.0, 0.0)),
                0.0,
                unit,
            ) { at, vertex, _ ->
                told += at to (vertex != null)
            }

        assertEquals(3.0, end)
        assertEquals(listOf(0.0 to false, 0.0 to true, 0.0 to false, 3.0 to true), told)
    }

    @Test
    fun aLineWithNoLegEndsWhereItStarts() {
        assertEquals(7.0, walkLine(emptyList(), 7.0, unit))
        assertEquals(7.0, walkLine(listOf(listOf(1.0, 1.0)), 7.0, unit))
    }
}
