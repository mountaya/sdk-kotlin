package com.mountaya.geo

/**
 * LayerRank is a band of the stacking order the layers Mountaya draws above a base map take, bottom
 * to top, so a layer's place never depends on the order it was added in: an overlay added after an
 * itinerary never covers it, and a hillshade never shades the line it lies under.
 *
 * A rank is a number, and the bands are spaced by ten but for the two that stand over an itinerary's
 * line, the geometry a map draws over it from data and its restricted stretches, so a band a consumer
 * adds between two neighbours takes a number between them, `Poi.rank + 2` between the points of
 * interest and the itinerary's waypoints. A base map's own layers take no rank and lie beneath every
 * band. The twelve are the contract's, `geo/layer-order.json`'s `layerRankBands`, each named there in
 * kebab-case.
 */
public enum class LayerRank(
    /** The band's number: a layer of a greater rank draws above one of a lesser. */
    public val rank: Int,
) {
    /** The shading of the terrain, at the bottom whatever its tiles' source layer. */
    Hillshade(10),

    /** An overlay, and any tile layer whose source layer the rank table does not know. */
    Overlay(20),

    /** The coverage of street-level imagery. */
    StreetviewCoverage(30),

    /** A zone's area and its outline. */
    Zone(40),

    /** An itinerary's line. */
    ItineraryLine(50),

    /**
     * The geometry a map draws over its itineraries' lines from data, an itinerary being planned and
     * the leg a reader points at, just above the lines and beneath their restricted stretches, so the
     * order the calls arrive in never stacks it under the route it lights (`map.geometry.layers`).
     */
    ItineraryFocus(52),

    /**
     * The stretches of an itinerary's line that follow a way restricting who may pass, drawn over the
     * line they mark and beneath its direction, so a restriction is never painted under the line it
     * qualifies and the direction still reads across it.
     */
    ItineraryRestriction(55),

    /** The direction drawn along an itinerary's line. */
    ItineraryDirection(60),

    /** The corridor a street-level image may be dropped in. */
    StreetviewCorridor(70),

    /** A point of interest, above every line so a route never hides one. */
    Poi(80),

    /** An itinerary's waypoint symbols. */
    ItineraryPoint(90),

    /** What the reader is doing: a marker being dragged, a result highlighted from a list. */
    Chrome(100),
}
