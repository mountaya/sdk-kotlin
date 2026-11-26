package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AmenityFilterTest {
    private val table = rows(amenities("water", "food"), amenities("water"), amenities("toilets", "water", "food"))

    private fun amenities(vararg values: String): List<RestSchema.WaypointAmenity> =
        values.map { RestSchema.WaypointAmenity(it) }

    /** One row per place, each offering the services given, a place offering none stating no details. */
    private fun rows(vararg places: List<RestSchema.WaypointAmenity>): List<ItineraryWaypointRow> =
        places.mapIndexed { index, services ->
            ItineraryWaypointRow(
                key = "row#$index",
                place =
                    ProfilePlace(
                        ProfileWaypoint.AtDistance("row", index.toDouble()),
                        details = if (services.isEmpty()) null else WaypointDetails(amenities = services),
                    ),
                index = index,
                from = 0.0,
                to = index.toDouble(),
                metrics = ItineraryWaypointRowMetrics(index.toDouble()),
            )
        }

    @Test
    fun aRowStaysOnlyWhereItOffersEveryServicePickedEveryRowWithNonePicked() {
        assertEquals(
            listOf(0, 2),
            amenityFilter(table, amenities("water", "food"), ElementsScope.standard).shown.map { it.index },
        )
        assertEquals(listOf(0, 1, 2), amenityFilter(table, emptyList(), ElementsScope.standard).shown.map { it.index })
        assertTrue(amenityFilter(table, amenities("showers"), ElementsScope.standard).shown.isEmpty())
    }

    @Test
    fun theChipsSortByTheReadersNameInTheReadersLanguageAServiceWithNoneByItsValue() {
        assertEquals(
            amenities("food", "toilets", "water"),
            amenityFilter(table, emptyList(), ElementsScope.standard).available,
        )
        val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Fr))

        assertEquals(amenities("water", "food", "toilets"), amenityFilter(table, emptyList(), french).available)
        val withSauna = rows(amenities("water"), amenities("sauna", "food"), emptyList())
        assertEquals(
            amenities("food", "sauna", "water"),
            amenityFilter(withSauna, emptyList(), ElementsScope.standard).available,
        )
    }
}
