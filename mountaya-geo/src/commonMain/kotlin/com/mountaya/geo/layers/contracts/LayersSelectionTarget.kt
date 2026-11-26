package com.mountaya.geo

import com.mountaya.client.generated.TileSchema
import kotlin.time.Instant

/**
 * LayersSelectionTarget is the map a layers selection drives (`geo.layers.selection`): the layers catalogue
 * it reads, the terrain, classic overlay and enrichments it draws, the instant its time-aware overlays show, and the
 * terrain it reports it shows. `MapEngine` is one; a map of an app's own, or a test's double, is another.
 *
 * A command answers once the map did what it asked for, and throws where the map refused it, which the
 * selection reports. Every command suspends, the two that take something off, [clearOverlay] and
 * [removeEnrichment], as much as those that draw, as the other SDKs' are asynchronous, so a map of the app's
 * own may wait on its renderer to take a layer off as it waits to draw one. It is driven from the thread its
 * selection is, and compared by identity, so a selection handed the map it drives already changes nothing.
 */
public interface LayersSelectionTarget {
    /**
     * The slug of the catalogue terrain the map shows, or null where it shows none: before one is up, or under
     * a style of the app's own.
     */
    public val terrain: String?

    /** Answers the layers catalogue: every terrain and overlay the credential draws. */
    public suspend fun loadLayers(): TileSchema.Layers

    /** Switches the base map to the terrain [slug] names, the catalogue's first where it names none. */
    public suspend fun setTerrain(slug: String? = null)

    /** Draws the classic overlay [slug] names in place of the one drawn before. */
    public suspend fun setOverlay(slug: String)

    /** Takes the classic overlay drawn off. */
    public suspend fun clearOverlay()

    /** Draws the enrichment [slug] names on top of what is drawn. */
    public suspend fun addEnrichment(slug: String)

    /** Takes the enrichment [slug] names off. */
    public suspend fun removeEnrichment(slug: String)

    /**
     * Has the map's time-aware overlays show [instant], or, where it is null, the clock's reading
     * (`geo.layers.selection-instant`): `MapEngine` asks the classic overlay the server stamps for it
     * (`map.overlays.instant`). A map of the app's own with no time-aware overlay answers at once, drawing nothing anew.
     */
    public suspend fun setInstant(instant: Instant?)
}
