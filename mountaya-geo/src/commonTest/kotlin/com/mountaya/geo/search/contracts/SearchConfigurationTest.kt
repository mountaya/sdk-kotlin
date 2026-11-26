package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SearchConfigurationTest {
    @Test
    fun aConfigurationAsksEveryBranchInTheSectionOrderAndNothingElseByDefault() {
        val configuration = SearchConfiguration()

        assertNull(configuration.fetch)
        assertNull(configuration.language)
        assertNull(configuration.limit)
        assertEquals(searchSectionOrder, configuration.targets)
        assertNull(configuration.wording.units)
        assertNull(configuration.onError)
    }

    @Test
    fun theSectionOrderIsTheItinerariesPlacesWaysPointsOfInterestZonesCollections() {
        assertEquals(
            listOf("ITINERARIES", "PLACES", "WAYS", "POIS", "ZONES", "COLLECTIONS"),
            searchSectionOrder.map { it.value },
        )
    }

    @Test
    fun twoConfigurationsAreEqualWhereTheyHoldOneTransportAndOneHandlerByIdentity() {
        val fetch = SearchFetch { error("never asked") }
        val wording = SearchWording()

        assertEquals(SearchConfiguration(fetch, wording = wording), SearchConfiguration(fetch, wording = wording))
        assertNotEquals(
            SearchConfiguration(fetch, wording = wording),
            SearchConfiguration(
                SearchFetch {
                    error("")
                },
                wording = wording,
            ),
        )
    }
}
