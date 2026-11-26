package com.mountaya.geo

import com.mountaya.client.generated.TileSchema
import kotlin.time.Instant

/**
 * LayersSelection is what a layers selection holds (`geo.layers.selection`): the layers catalogue the map it
 * drives answered, the terrain, the classic overlay and the enrichments asked for, the instant pinned
 * (`geo.layers.selection-instant`), and the refusal of the latest command or catalogue read to settle, where it
 * failed.
 *
 * The selection is what was asked for and the map is what is drawn: a command the map refuses keeps what it
 * asked for, and reports the refusal until a later command settles (`geo.layers.selection-outcome`), so the
 * two disagree openly rather than a picker snapping back with no word said.
 *
 * Two are equal where they hold the same catalogue, choices and instant and the same refusal, the very throwable,
 * so two refusals are two states.
 */
public class LayersSelection(
    /** The layers catalogue the map answered, or null before it lands. */
    public val catalogue: TileSchema.Layers? = null,
    /**
     * The slug of the terrain selected, or, once the catalogue lands with none selected, of the terrain the map
     * reports it shows, null where it reports none.
     */
    public val terrain: String? = null,
    /** The slug of the classic overlay selected, or null for none. */
    public val overlay: String? = null,
    /** The slugs of the enrichments turned on, in the order they were turned on. */
    public val enrichments: List<String> = emptyList(),
    /**
     * What the map threw for the latest command or catalogue read to settle, or null where it succeeded, or none
     * has settled since the selection began driving its map.
     */
    public val refusal: Throwable? = null,
    /**
     * The instant pinned last, which the time-aware overlays show, or null with none pinned, which is the clock's
     * reading (`overlayInstant`).
     */
    public val instant: Instant? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is LayersSelection &&
            other.catalogue == catalogue &&
            other.terrain == terrain &&
            other.overlay == overlay &&
            other.enrichments == enrichments &&
            other.refusal === refusal &&
            other.instant == instant

    override fun hashCode(): Int {
        var hash = catalogue.hashCode()
        hash = 31 * hash + terrain.hashCode()
        hash = 31 * hash + overlay.hashCode()
        hash = 31 * hash + enrichments.hashCode()
        hash = 31 * hash + refusal.hashCode()
        return 31 * hash + instant.hashCode()
    }

    override fun toString(): String =
        "LayersSelection(terrain=$terrain, overlay=$overlay, enrichments=$enrichments, instant=$instant, " +
            "refusal=$refusal, catalogue=${if (catalogue == null) "none" else "held"})"
}
