package com.mountaya.elements

/** PointQuery is a map's point query, named by what it answers (`map.query.layers`). */
internal enum class PointQuery {
    /** Every feature the map drew, in every band. */
    Features,

    /** The points of interest, the POI band. */
    Pois,

    /** The zones, the zone band. */
    Zones,

    /** An itinerary's routed lines and waypoints, the itinerary line and point bands. */
    Itineraries,
}
