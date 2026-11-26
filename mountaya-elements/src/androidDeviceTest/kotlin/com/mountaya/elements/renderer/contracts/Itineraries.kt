package com.mountaya.elements

import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ItineraryResourceCache
import com.mountaya.geo.ItinerarySource
import com.mountaya.geo.ResolvedItinerary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Instant

/**
 * Itineraries builds the itinerary a data element's test draws, read through a cache that answers on the main thread
 * with no network: two legs, a climb from 1,000 m to a col at 1,400 m and a descent to 1,100 m, each about 1.1 km,
 * and three places along them, a hut with services and a cut-off, the col, and an unnamed spring.
 */
internal object Itineraries {
    /** The first leg's line, due north from 6 E 45 N, its altitude every 555.97 m. */
    val climb: List<List<Double>> =
        listOf(listOf(6.0, 45.0, 1_000.0), listOf(6.0, 45.005, 1_200.0), listOf(6.0, 45.01, 1_400.0))

    /** The second leg's line, on north from the col. */
    val descent: List<List<Double>> =
        listOf(listOf(6.0, 45.01, 1_400.0), listOf(6.0, 45.015, 1_250.0), listOf(6.0, 45.02, 1_100.0))

    /** The itinerary's three places, as the REST API answers them. */
    private val places: List<RestSchema.ItineraryWaypoint> =
        listOf(
            place(
                "hut",
                position = 0,
                at = listOf(6.0, 45.005),
                name = "Refuge",
                icon = RestSchema.WaypointIcon.Hut,
                amenities =
                    listOf(
                        RestSchema.WaypointAmenity.Water,
                        RestSchema.WaypointAmenity.HotMeal,
                        RestSchema.WaypointAmenity.Toilets,
                        RestSchema.WaypointAmenity.FirstAid,
                        RestSchema.WaypointAmenity.Showers,
                    ),
                cutoff = Instant.fromEpochSeconds(1_788_000_000),
            ),
            place(
                "col",
                position = 1,
                at = listOf(6.0, 45.01),
                name = "Col",
                icon = RestSchema.WaypointIcon("mountain"),
            ),
            place("spring", position = 2, at = listOf(6.0, 45.015), name = null, icon = null),
        )

    /** The itinerary the REST API answers. */
    val itinerary: RestSchema.ItineraryWithDetails =
        answered(
            listOf(
                segment("climb", position = 0, name = "Climb", line = climb, color = "#e8590c"),
                segment("descent", position = 1, name = "Descent", line = descent, color = null),
            ),
        )

    /** The same itinerary with no altitude: each vertex a longitude and a latitude alone. */
    val flat: RestSchema.ItineraryWithDetails =
        answered(
            listOf(
                segment("climb", position = 0, name = "Climb", line = climb.map { it.take(2) }, color = "#e8590c"),
                segment("descent", position = 1, name = "Descent", line = descent.map { it.take(2) }, color = null),
            ),
        )

    /** The same itinerary with no segment, its places kept. */
    val bare: RestSchema.ItineraryWithDetails = answered(emptyList())

    /** The itinerary's projections, as an element reads them. */
    val resolved: ResolvedItinerary get() = ResolvedItinerary(itinerary)

    /** Unreadable is what a source that cannot read the itinerary throws. */
    class Unreadable : Exception("unreadable")

    /**
     * A scope a cache's reads run in, on the main thread, where the elements drive their engines; a test cancels it
     * once it ends. Its dispatcher is `viewModelScope`'s, which runs a read at once on the main thread, so a source
     * that answers without suspending, as these do, lands while the engine that asked for it is still asking.
     */
    fun reads(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** A cache over a source answering [answered] for every id, its reads in [scope]. */
    fun cache(
        scope: CoroutineScope,
        answered: RestSchema.ItineraryWithDetails = itinerary,
    ): ItineraryResourceCache = ItineraryResourceCache(scope, ItinerarySource { _, _ -> answered })

    /** A cache over a source that throws [Unreadable] for every id, its reads in [scope]. */
    fun unreadable(scope: CoroutineScope): ItineraryResourceCache =
        ItineraryResourceCache(scope, ItinerarySource { _, _ -> throw Unreadable() })

    /** The itinerary of [segments] and the three places. */
    private fun answered(segments: List<RestSchema.ItinerarySegment>): RestSchema.ItineraryWithDetails =
        RestSchema.ItineraryWithDetails(
            activity_id = "hiking",
            ascent = null,
            count = RestSchema.ItineraryCount(groups = 0, segments = segments.size, waypoints = places.size),
            created_at = Instant.fromEpochMilliseconds(0),
            created_by = RestSchema.RelationToUser(id = "user"),
            description = null,
            distance = null,
            id = "itin_test",
            is_private = false,
            is_verified = false,
            name = "Col",
            organization_id = null,
            etag = "etag",
            revision = 1,
            status = RestSchema.ItineraryStatus.Draft,
            updated_at = Instant.fromEpochMilliseconds(0),
            groups = emptyList(),
            has_unpublished_changes = false,
            published_at = null,
            segments = segments,
            waypoints = places,
        )

    /** A leg as the REST API answers one, along [line], and a placeholder for every member the elements never read. */
    private fun segment(
        id: String,
        position: Int,
        name: String,
        line: List<List<Double>>,
        color: String?,
    ): RestSchema.ItinerarySegment =
        RestSchema.ItinerarySegment(
            altitude_max = null,
            altitude_min = null,
            ascent = null,
            color = color,
            created_at = Instant.fromEpochMilliseconds(0),
            created_by = RestSchema.RelationToUser(id = "user"),
            descent = null,
            description = null,
            distance = null,
            duration = null,
            geojson = GeoJsonLineString(line),
            gradient_max = null,
            gradient_min = null,
            group_id = null,
            id = id,
            itinerary_id = "itin_test",
            mode = RestSchema.SegmentMode.Linestring,
            name = name,
            position = position,
            revision = 1,
            road_access_info = null,
            slope_info = null,
            speed = null,
            speed_info = null,
            start_at = null,
            steps = null,
            surface_info = null,
            trail_difficulty_info = null,
            way_type_info = null,
        )

    /** A place as the REST API answers one, standing [at] a point, and a placeholder for every other member. */
    private fun place(
        id: String,
        position: Int,
        at: List<Double>,
        name: String?,
        icon: RestSchema.WaypointIcon?,
        amenities: List<RestSchema.WaypointAmenity> = emptyList(),
        cutoff: Instant? = null,
    ): RestSchema.ItineraryWaypoint =
        RestSchema.ItineraryWaypoint(
            amenities = amenities,
            color = null,
            contact = null,
            created_at = Instant.fromEpochMilliseconds(0),
            created_by = RestSchema.RelationToUser(id = "user"),
            description = null,
            geojson = GeoJsonPoint(at),
            icon = icon,
            id = id,
            itinerary_id = "itin_test",
            media = null,
            mode = RestSchema.WaypointMode.Poi,
            name = name,
            position = position,
            properties = null,
            revision = 1,
            timing = cutoff?.let { instant -> RestSchema.WaypointTiming { cutoff_at = instant } },
        )
}
