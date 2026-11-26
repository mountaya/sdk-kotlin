package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnalysisKindTest {
    @Test
    fun eachKindOfResultIsNamedAsTheContractsFixturesNameIt() {
        assertEquals(
            listOf("directions", "suggestions", "isochrones", "places", "matrix", "pois"),
            AnalysisKind.entries.map { it.id },
        )
        assertNull(AnalysisKind.entries.firstOrNull { it.id == "geocode" }, "a place is a result of kind `places`")
    }
}
