package com.mountaya.geo

import com.mountaya.client.generated.TileSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

/**
 * LayersSelectionEngine is the terrain, classic overlay, enrichments and pinned instant behind a layers picker
 * (`geo.layers.selection`, `geo.layers.selection-outcome`, `geo.layers.selection-instant`): it reads the layers
 * catalogue of the map it drives, holds what the reader asked for, and drives the map by the catalogue's rules.
 *
 * - **One classic overlay.** A slope, a snow depth and an avalanche risk each repaint the same ground, so
 *   selecting one replaces the last, and a second press on the one selected clears it.
 * - **Enrichments stack.** A hillshade under a slope overlay is a composition, so each toggles on and off by
 *   itself, kept in the order it was turned on, one turned on again going last.
 * - **The terrain the map shows.** Once the catalogue lands, a selection with no terrain selected states the
 *   one the map reports it shows, and none for a map that reports none, so a picker marks what the map draws
 *   and never a guess; a terrain selected first is kept, and nothing asks the map to switch to the one it
 *   reports.
 * - **One pinned instant.** The moment the time-aware overlays show is the instant pinned last, or the clock's
 *   reading with none pinned ([setInstant]); it stands through every other command.
 *
 * The selection is what was asked for and the map is what is drawn, neither mirrored into the other: each
 * command drives the map as it is made, whether or not anyone reads the state, and a command the map refuses
 * keeps what it asked for and reports the refusal, `state.value.refusal`, until a later command or catalogue
 * read settles. A map handed in place of another, or none, starts an empty selection, the same map brought
 * back included, and no catalogue or outcome owed to a map it drove before lands. With no map a command is held
 * and drives nothing.
 *
 * ```kotlin
 * val layers = LayersSelectionEngine(scope, map)
 * layers.setOverlay("slopes")
 * layers.toggleEnrichment("hillshade")
 * ```
 *
 * It reads the catalogue as soon as it is handed a map, by its constructor as by [configure]; a map shares one
 * catalogue read among every reader (`map.layers.manifest`), so an engine made and dropped costs no read. A
 * read that fails is kept by nobody, and [configure] handed the same map asks again. It keeps no time.
 *
 * Each read and each command runs in a coroutine of its own in [scope], the caller's, whose dispatcher is the
 * thread the engine and its map are driven from, the main thread in a composition; a command is called on the
 * map once the caller's turn ends, in the order made. [state] holds a state only where it differs from the one
 * before it. Closing the engine cancels nothing it sent, so a command made still reaches the map, and lands
 * nothing more; a closed engine is inert. It is an `AutoCloseable`, as every engine is, so `use` closes it.
 */
public class LayersSelectionEngine(
    private val scope: CoroutineScope,
    map: LayersSelectionTarget? = null,
) : AutoCloseable {
    private val held = MutableStateFlow(LayersSelection())

    /** What the selection holds. */
    public val state: StateFlow<LayersSelection> = held.asStateFlow()

    /** The map the selection drives, or null for none. */
    private var map: LayersSelectionTarget? = map

    /**
     * How many maps the engine was handed, which numbers the selection a command is made under, so an outcome
     * owed to an earlier one lands nowhere, the same map brought back included.
     */
    private var drives = 0

    /** The number of the catalogue read the selection waits for, or null for none. */
    private var catalogueRead: Int? = null
    private var nextRead = 0
    private var closed = false

    init {
        readCatalogue()
    }

    /**
     * Hands the selection the map it drives. The map it drives already changes nothing, unless its catalogue read
     * failed, which it asks again; another one, null included, starts an empty selection and reads its catalogue.
     */
    public fun configure(map: LayersSelectionTarget?) {
        if (closed) return
        if (map === this.map) {
            if (held.value.catalogue == null && catalogueRead == null) readCatalogue()
            return
        }
        this.map = map
        drives += 1
        catalogueRead = null
        held.value = LayersSelection()
        readCatalogue()
    }

    /**
     * Selects the terrain [slug] names and switches the map's base map to it. The map is asked even for the terrain
     * already selected, which is how a switch it refused is tried again.
     */
    public fun setTerrain(slug: String) {
        if (closed) return
        if (held.value.terrain != slug) held.value = held.value.with(terrain = slug)
        drive { it.setTerrain(slug) }
    }

    /**
     * Selects the classic overlay [slug] names in place of the one selected before, and draws it; the slug already
     * selected clears it, which is what a second press means, and null clears whatever is selected.
     */
    public fun setOverlay(slug: String?) {
        if (closed) return
        val selected = if (held.value.overlay == slug) null else slug
        if (held.value.overlay != selected) held.value = held.value.with(overlay = selected)
        drive { map -> if (selected == null) map.clearOverlay() else map.setOverlay(selected) }
    }

    /**
     * Turns the enrichment [slug] names on where it is off, last among those on, and off where it is on, leaving every
     * other overlay as it is.
     */
    public fun toggleEnrichment(slug: String) {
        if (closed) return
        val selection = held.value
        val isOn = slug in selection.enrichments
        held.value =
            selection.with(enrichments = if (isOn) selection.enrichments - slug else selection.enrichments + slug)
        drive { map -> if (isOn) map.removeEnrichment(slug) else map.addEnrichment(slug) }
    }

    /**
     * Pins [instant] as the moment the map's time-aware overlays show, or pins none where it is null, which is the
     * clock's reading (`geo.layers.selection-instant`), and drives the map to it at once, as `setInstant`. A pin is a
     * command like any other: the map is asked again for the instant the selection already holds, which is how a pin it
     * refused is tried again, a refusal is kept and reported until a later command settles, and the pin stands through
     * every other command. A replaced map, or none, starts with none pinned, and with no map a pin is held and drives
     * nothing.
     */
    public fun setInstant(instant: Instant?) {
        if (closed) return
        if (held.value.instant != instant) held.value = held.value.with(instant = instant)
        drive { it.setInstant(instant) }
    }

    /**
     * Stops the engine for good: no catalogue or outcome lands after it, and every later command does nothing. The
     * state stays as it was. Closing twice does nothing more.
     */
    override fun close() {
        if (closed) return
        closed = true
        map = null
        catalogueRead = null
    }

    /**
     * Reads the catalogue of the map the selection drives, where it holds none, and folds it in as it lands
     * while the selection still waits for it.
     */
    private fun readCatalogue() {
        val map = map ?: return
        if (closed || held.value.catalogue != null) return
        val read = nextRead++
        catalogueRead = read
        scope.launch {
            val outcome =
                try {
                    Result.success(map.loadLayers())
                } catch (failure: Throwable) {
                    // A read the scope cancelled lands for nobody; any other failure is the read's.
                    currentCoroutineContext().ensureActive()
                    Result.failure<TileSchema.Layers>(failure)
                }
            if (closed || catalogueRead != read) return@launch
            catalogueRead = null
            val selection = held.value
            held.value =
                outcome.fold(
                    // The map is asked what it shows only while nothing is selected, as the catalogue lands.
                    onSuccess = { catalogue ->
                        selection.with(
                            catalogue = catalogue,
                            terrain = selection.terrain ?: map.terrain,
                            refusal = null,
                        )
                    },
                    onFailure = { failure -> selection.with(refusal = failure) },
                )
        }
    }

    /**
     * Runs [command] on the map the selection drives, and reports its outcome while the selection it was made
     * under stands: its refusal, or, where it succeeds, the end of a refusal an earlier one reported.
     */
    private fun drive(command: suspend (LayersSelectionTarget) -> Unit) {
        val map = map ?: return
        val drive = drives
        scope.launch {
            val refusal =
                try {
                    command(map)
                    null
                } catch (failure: Throwable) {
                    currentCoroutineContext().ensureActive()
                    failure
                }
            if (closed || drives != drive) return@launch
            if (refusal == null && held.value.refusal == null) return@launch
            held.value = held.value.with(refusal = refusal)
        }
    }
}

/** This selection with the members given changed, every other kept. */
private fun LayersSelection.with(
    catalogue: TileSchema.Layers? = this.catalogue,
    terrain: String? = this.terrain,
    overlay: String? = this.overlay,
    enrichments: List<String> = this.enrichments,
    refusal: Throwable? = this.refusal,
    instant: Instant? = this.instant,
): LayersSelection = LayersSelection(catalogue, terrain, overlay, enrichments, refusal, instant)
