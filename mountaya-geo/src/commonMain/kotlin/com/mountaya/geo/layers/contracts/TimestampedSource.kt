package com.mountaya.geo

/**
 * TimestampedSource is a tile source of the selected classic overlay asked again for an instant
 * (`geo.layers.tile-timestamp`): its id, and its tile addresses, each carrying the instant as its `timestamp`
 * parameter, in the order the catalogue lists them. A map swaps the source's tiles for these, its layers standing where
 * they stood.
 *
 * Two are equal where both members are.
 */
public class TimestampedSource(
    /** The id of the source, as the catalogue spells it. */
    public val sourceId: String,
    /** The source's tile addresses, each asking for the instant. */
    public val tiles: List<String>,
) {
    override fun equals(other: Any?): Boolean =
        other is TimestampedSource && other.sourceId == sourceId && other.tiles == tiles

    override fun hashCode(): Int = sourceId.hashCode() * 31 + tiles.hashCode()

    override fun toString(): String = "TimestampedSource(sourceId=$sourceId, tiles=$tiles)"
}
