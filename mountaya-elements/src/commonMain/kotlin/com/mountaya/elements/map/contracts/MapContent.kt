package com.mountaya.elements

/**
 * MapContent is what one slot of a map draws: the sources and the layers of the specification it asked
 * for, in the order the specification lists them, and the restricted-stretch layers the map draws of its
 * own beside an itinerary line (`map.road-access.layers`).
 *
 * A map holds its contents in the order each slot was first asked for, which is the order the concepts
 * they draw are named in (`map.layers.keys`).
 */
internal data class MapContent(
    /** The slot that drew it. */
    val slot: ContentSlot,
    /** The id of every source it drew, in the order the specification lists them. */
    val sourceIds: List<String>,
    /** The id of every layer the specification declares, in the order it lists them. */
    val layerIds: List<String>,
    /**
     * The id of every restricted-stretch layer the map drew beside the specification's itinerary lines, the
     * stripes then the icons of each source.
     */
    val restrictionLayerIds: List<String>,
)
