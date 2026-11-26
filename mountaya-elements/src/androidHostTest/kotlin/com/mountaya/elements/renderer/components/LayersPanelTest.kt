package com.mountaya.elements

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.LayersPanelChoice
import com.mountaya.geo.LayersPanelPress
import com.mountaya.geo.LayersSelectionEngine
import com.mountaya.geo.LayersSelectionTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * A layers panel's presses on the host JVM, Compose left out: each card and switch asks its choice's command of the
 * selection, a locked entry's press is handed on and selects nothing, the time asks nothing of a press, and the slider's
 * release pins through the selection.
 */
class LayersPanelTest {
    /** A map a selection drives, which answers every command at once, on the thread that sent it, and records it. */
    private class Answering : LayersSelectionTarget {
        val calls = mutableListOf<String>()
        override val terrain: String? = null

        override suspend fun loadLayers(): TileSchema.Layers = error("the presses read no catalogue")

        override suspend fun setTerrain(slug: String?) {
            calls += "terrain:$slug"
        }

        override suspend fun setOverlay(slug: String) {
            calls += "overlay:$slug"
        }

        override suspend fun clearOverlay() {
            calls += "overlay:-"
        }

        override suspend fun addEnrichment(slug: String) {
            calls += "add:$slug"
        }

        override suspend fun removeEnrichment(slug: String) {
            calls += "remove:$slug"
        }

        override suspend fun setInstant(instant: Instant?) {
            calls += "instant:${instant ?: "-"}"
        }
    }

    @Test
    fun aPressAsksItsChoicesCommandALockedOneIsHandedOnAndTheTimeAsksNothing() {
        val map = Answering()
        // Unconfined, so each command reaches the map before the call that made it returns.
        val selection = LayersSelectionEngine(CoroutineScope(Dispatchers.Unconfined), map)
        val locked = mutableListOf<String>()

        layersPress(LayersPanelPress.Command(LayersPanelChoice.Terrain, "topo"), selection) { locked += it }
        layersPress(LayersPanelPress.Command(LayersPanelChoice.Overlay, "slopes"), selection) { locked += it }
        layersPress(LayersPanelPress.Command(LayersPanelChoice.Enrichment, "hillshade"), selection) { locked += it }
        layersPress(LayersPanelPress.Command(LayersPanelChoice.Time, "now"), selection) { locked += it }
        layersPress(LayersPanelPress.Locked("snow-depth"), selection) { locked += it }
        selection.setInstant(Instant.parse("2026-03-15T12:00:00Z"))

        assertEquals(
            listOf("terrain:topo", "overlay:slopes", "add:hillshade", "instant:2026-03-15T12:00:00Z"),
            map.calls,
        )
        assertEquals(listOf("snow-depth"), locked)
        selection.close()
    }
}
