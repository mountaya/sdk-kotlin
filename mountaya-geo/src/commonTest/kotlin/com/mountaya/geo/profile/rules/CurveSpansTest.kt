package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CurveSpansTest {
    @Test
    fun stretchesAreReadInOrderTheEarlierOfTwoOverlappingKeepingTheSharedGround() {
        val spans = curveSpans(listOf(CurveColor(400.0, 1_000.0, "#00f"), CurveColor(600.0, 0.0, "#f00")))

        assertEquals(listOf(CurveColor(0.0, 600.0, "#f00"), CurveColor(600.0, 1_000.0, "#00f")), spans)
    }

    @Test
    fun aStretchOfNoLengthOrSwallowedByAnEarlierOneIsLeftOut() {
        val spans =
            curveSpans(
                listOf(
                    ProfileRange(300.0, 300.0),
                    ProfileRange(0.0, 600.0),
                    ProfileRange(100.0, 500.0),
                    ProfileRange(Double.NaN, 10.0),
                ),
            )

        assertEquals(listOf(ProfileRange(0.0, 600.0)), spans)
    }

    @Test
    fun stretchesStartingAtOneDistanceKeepTheOrderGiven() {
        val spans =
            curveSpans(
                listOf(
                    ProfileClass(0.0, 100.0, "#f00", "a"),
                    ProfileClass(0.0, 200.0, "#00f", "b"),
                ),
            )

        assertEquals(listOf("a", "b"), spans.map { it.label })
        assertEquals(100.0, spans.lastOrNull()?.from)
    }
}
