package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * amenityFilter is the chips a waypoint table offers and the [rows] a choice of them, [picked], leaves
 * (`geo.profile.amenity-filter`).
 *
 * A chip stands for each service any row's place offers, once, sorted by the reader's name for it ([amenityName])
 * as the reader's locale collates it, [ElementsScope.locale] of [scope], so a filter row does not reshuffle
 * between itineraries, two names the locale sorts as one keeping the order the services were first offered in.
 * A row stays only where its place offers every service picked, since a reader picking water and a hot meal asks
 * where they can have both, and with none picked every row stays.
 */
public fun amenityFilter(
    rows: List<ItineraryWaypointRow>,
    picked: List<RestSchema.WaypointAmenity>,
    scope: ElementsScope,
): AmenityFilter {
    val offered = LinkedHashSet<RestSchema.WaypointAmenity>()
    for (row in rows) {
        offered +=
            row.place.details
                ?.amenities
                .orEmpty()
    }
    val named = offered.map { it to amenityName(it, scope) }
    // A stable sort: two the locale sorts as one keep the order they were first offered in.
    val available =
        named
            .sortedWith { a, b ->
                when {
                    localeCollatedBefore(a.second, b.second, scope.locale) -> -1
                    localeCollatedBefore(b.second, a.second, scope.locale) -> 1
                    else -> 0
                }
            }.map { it.first }
    val shown =
        if (picked.isEmpty()) {
            rows
        } else {
            rows.filter { row ->
                val services =
                    row.place.details
                        ?.amenities
                        .orEmpty()
                picked.all { it in services }
            }
        }
    return AmenityFilter(available, shown)
}
