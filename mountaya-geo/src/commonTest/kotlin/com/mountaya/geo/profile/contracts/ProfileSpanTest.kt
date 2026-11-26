package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertTrue

class ProfileSpanTest {
    @Test
    fun aWindowAColouredStretchAndAClassifiedStretchAreSpansTheRulesReadAlike() {
        val spans: List<ProfileSpan<*>> =
            listOf(
                ProfileRange(0.0, 100.0),
                CurveColor(0.0, 100.0, "#f00"),
                ProfileClass(0.0, 100.0, "#f00", "Path"),
            )

        assertTrue(spans.all { it.from == 0.0 && it.to == 100.0 })
    }
}
