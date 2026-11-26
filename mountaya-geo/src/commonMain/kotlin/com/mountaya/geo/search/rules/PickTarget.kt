package com.mountaya.geo

/**
 * POINT_ZOOM is the zoom a camera flies to on a picked result that states a point and no extent, close enough
 * to read the place it lands on (`geo.search.pick`).
 */
public const val POINT_ZOOM: Double = 13.0

/**
 * pickTarget answers what picking a search result does to a map (`geo.search.pick`): it frames the result's extent
 * where it states one and otherwise flies to its point at [POINT_ZOOM], then marks it with the map's search
 * highlight (`map.search.result-highlight`).
 *
 * A place, a point of interest and a way are marked as themselves, a way with its extent and its line where it
 * states them; an itinerary is marked at its start and a zone at its point, each as a place is. A result with
 * neither an extent nor a point, which a collection always is, a set of itineraries with no place of its own, moves
 * nothing and marks nothing. An extent is framed as the result writes it, across the antimeridian where its west
 * edge lies east of its east one.
 *
 * The mark is what the map's search highlight is handed, so a surface that marks every pick hands it even where it
 * is null, which takes the mark of the pick before down.
 */
public fun pickTarget(payload: SearchRowPayload): SearchPick {
    fun pick(
        bounds: List<Double>?,
        point: List<Double>?,
        mark: SearchHighlight?,
    ): SearchPick =
        when {
            bounds != null -> SearchPick(SearchPickCamera.Fit(bounds), mark)
            point != null -> SearchPick(SearchPickCamera.Fly(point, POINT_ZOOM), mark)
            else -> SearchPick(null, mark)
        }

    fun place(point: List<Double>?): SearchHighlight? = point?.let { SearchHighlight.Place(it) }
    return when (payload) {
        is SearchRowPayload.Place -> {
            pick(payload.bounds, payload.point, place(payload.point))
        }

        is SearchRowPayload.Poi -> {
            pick(null, payload.point, SearchHighlight.Poi(payload.point, payload.category))
        }

        is SearchRowPayload.Way -> {
            pick(
                payload.bounds,
                payload.anchor,
                SearchHighlight.Way(payload.anchor, payload.bounds, payload.coordinates),
            )
        }

        is SearchRowPayload.Itinerary -> {
            pick(payload.bounds, payload.start, place(payload.start))
        }

        is SearchRowPayload.Zone -> {
            pick(payload.bounds, payload.point, place(payload.point))
        }

        is SearchRowPayload.Collection -> {
            pick(null, null, null)
        }
    }
}
