package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchHighlightTest {
    @Test
    fun twoHighlightsAreEqualWhereTheyHoldTheSameValuesEachNumberTheSameNumber() {
        assertEquals(SearchHighlight.Place(listOf(6.0, Double.NaN)), SearchHighlight.Place(listOf(6.0, Double.NaN)))
        assertNotEquals(SearchHighlight.Place(listOf(0.0, 45.0)), SearchHighlight.Place(listOf(-0.0, 45.0)))
        assertEquals(
            SearchHighlight.Poi(listOf(6.0, 45.0), DataSchema.POICategory.Hut),
            SearchHighlight.Poi(listOf(6.0, 45.0), DataSchema.POICategory.Hut),
        )
        assertNotEquals(
            SearchHighlight.Poi(listOf(6.0, 45.0), DataSchema.POICategory.Hut),
            SearchHighlight.Poi(listOf(6.0, 45.0), null),
        )
        assertNotEquals<SearchHighlight>(
            SearchHighlight.Poi(listOf(6.0, 45.0), null),
            SearchHighlight.Place(listOf(6.0, 45.0)),
        )
        assertEquals(
            SearchHighlight.Way(listOf(6.0, 45.0), listOf(6.0, 45.0, 7.0, 46.0), listOf(listOf(6.0, Double.NaN))),
            SearchHighlight.Way(listOf(6.0, 45.0), listOf(6.0, 45.0, 7.0, 46.0), listOf(listOf(6.0, Double.NaN))),
        )
        assertNotEquals(
            SearchHighlight.Way(listOf(6.0, 45.0), null, null),
            SearchHighlight.Way(listOf(6.0, 45.0), null, emptyList()),
        )
        assertEquals(
            SearchHighlight.Way(listOf(6.0, Double.NaN), null, null).hashCode(),
            SearchHighlight.Way(listOf(6.0, Double.NaN), null, null).hashCode(),
        )
    }

    @Test
    fun aCategoryIsToldApartByItsCodePoints() {
        assertNotEquals(
            SearchHighlight.Poi(listOf(6.0, 45.0), DataSchema.POICategory("CAFÉ")),
            SearchHighlight.Poi(listOf(6.0, 45.0), DataSchema.POICategory("CAFÉ")),
        )
    }
}
