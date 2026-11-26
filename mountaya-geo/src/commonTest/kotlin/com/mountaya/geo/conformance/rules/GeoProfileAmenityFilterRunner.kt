package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.profile.amenity-filter`: the chips a table of the case's `places` offers in its reader's `language`, the
 * choice after a press of its `press` where it states one, and the rows that stay by their place in `places`,
 * each row carrying its place's index as its own.
 */
object GeoProfileAmenityFilterRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val input = case.fields
        val reader =
            scopeOf(
                ElementsScope.standard,
                ElementsScopeStatement(language = RestSchema.Language(input.text("language"))),
            )
        val rows =
            input.items("places").mapIndexed { index, services ->
                val amenities = profileOptionalTexts(services).map { RestSchema.WaypointAmenity(it) }
                ItineraryWaypointRow(
                    key = "row#$index",
                    place =
                        ProfilePlace(
                            ProfileWaypoint.AtDistance("row", index.toDouble()),
                            details = WaypointDetails(amenities = amenities),
                        ),
                    index = index,
                    from = 0.0,
                    to = index.toDouble(),
                    metrics = ItineraryWaypointRowMetrics(index.toDouble()),
                )
            }
        var picked = profileOptionalTexts(input["picked"]).map { RestSchema.WaypointAmenity(it) }
        input.textOrNull("press")?.let { picked = toggleAmenity(picked, RestSchema.WaypointAmenity(it)) }
        val filter = amenityFilter(rows, picked, reader)
        return ConformanceOutcome.Value(
            profileObject(
                "chips" to profileTexts(filter.available.map { it.value }),
                "picked" to profileTexts(picked.map { it.value }),
                "rows" to FixtureValue.Array(filter.shown.map { FixtureValue.Number(it.index.toDouble()) }),
            ),
        )
    }
}
