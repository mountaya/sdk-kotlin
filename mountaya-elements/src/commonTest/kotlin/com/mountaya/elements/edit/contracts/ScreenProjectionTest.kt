package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenProjectionTest {
    @Test
    fun aProjectionIsThePairOfFunctionsItIsMadeOfToTheScreenAndBack() {
        val projection =
            ScreenProjection(point = { listOf(it[0] * 2, it[1] * 2) }, position = {
                listOf(
                    it[0] / 2,
                    it[1] / 2,
                )
            })

        assertEquals(listOf(12.0, 90.0), projection.point(listOf(6.0, 45.0)))
        assertEquals(listOf(6.0, 45.0), projection.position(listOf(12.0, 90.0)))
    }
}
