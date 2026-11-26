package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class PointQueryTest {
    @Test
    fun aMapAnswersFourPointQueries() {
        assertEquals(
            listOf(PointQuery.Features, PointQuery.Pois, PointQuery.Zones, PointQuery.Itineraries),
            PointQuery.entries,
        )
    }
}
