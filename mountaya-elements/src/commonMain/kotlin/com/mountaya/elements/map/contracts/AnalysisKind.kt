package com.mountaya.elements

/**
 * AnalysisKind is a kind of Data API result a map draws, each from a source of its own through the layers
 * its kind names, at its kind's rank (`map.analysis.layers`), each named by [id] as the fixtures name it.
 */
internal enum class AnalysisKind(
    /** The kind as the contract's fixtures and a result's id spell it. */
    val id: String,
) {
    /** A routed itinerary between waypoints. */
    Directions("directions"),

    /** A set of suggested itineraries, the chosen one solid and the rest dashed. */
    Suggestions("suggestions"),

    /** The areas reached within each range of an origin. */
    Isochrones("isochrones"),

    /** The answer of a place search, `geocode`. */
    Places("places"),

    /** The costs between locations, drawn as connectors between them. */
    Matrix("matrix"),

    /** The answer of a points of interest query, one at a time. */
    Pois("pois"),
}
