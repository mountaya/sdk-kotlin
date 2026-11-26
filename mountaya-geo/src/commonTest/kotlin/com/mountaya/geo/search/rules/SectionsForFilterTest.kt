package com.mountaya.geo

import com.mountaya.client.generated.DataSchema.SearchTarget
import kotlin.test.Test
import kotlin.test.assertEquals

class SectionsForFilterTest {
    private val sections =
        listOf(
            SearchTarget.Places,
            SearchTarget.Pois,
            SearchTarget.Ways,
            SearchTarget.Itineraries,
            SearchTarget.Collections,
            SearchTarget.Zones,
        )

    @Test
    fun aFilterShowsItsSectionsInTheOrderGiven() {
        assertEquals(sections, sectionsForFilter(sections, SearchFilter.All))
        assertEquals(
            listOf(SearchTarget.Itineraries, SearchTarget.Collections),
            sectionsForFilter(sections, SearchFilter.Itineraries),
        )
        assertEquals(
            listOf(SearchTarget.Places, SearchTarget.Pois, SearchTarget.Ways),
            sectionsForFilter(sections, SearchFilter.Places),
        )
        assertEquals(listOf(SearchTarget.Zones), sectionsForFilter(sections.reversed(), SearchFilter.Zones))
        assertEquals(
            listOf(SearchTarget.Collections, SearchTarget.Itineraries),
            sectionsForFilter(sections.reversed(), SearchFilter.Itineraries),
        )
    }

    @Test
    fun aTargetThisBuildDoesNotKnowIsShownByNoFilter() {
        assertEquals(emptyList(), sectionsForFilter(listOf(SearchTarget("PEAKS")), SearchFilter.All))
    }
}
