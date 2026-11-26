package com.mountaya.elements

/**
 * MapLayerRegistry is the one registry every layer a map draws above its base map goes through, which
 * is what makes a layer's place the contract's rank and never the order code happened to add it in
 * (`geo.layers.order`).
 *
 * It holds the layers bottom to top: a layer of a greater rank above one of a lesser, two of one rank
 * in the order they were placed, the later above (`map.layers.rank-insertion`). A layer placed again under
 * an id in use at the rank it holds keeps its place and takes the new definition there, and one placed again
 * at another rank moves to the top of its new band. An id is matched exactly.
 *
 * It also holds the order the layers were placed in, which a point query asks the renderer about them in
 * (`map.query.layers`): a layer placed again at the rank it holds keeps its place there, and one placed at
 * another rank is placed anew, last.
 */
internal class MapLayerRegistry {
    private val held = mutableListOf<MapLayer>()

    /** The id of every layer held, in the order the layers were placed. */
    private val placed = mutableListOf<String>()

    /** Every layer held, bottom to top. */
    val layers: List<MapLayer> get() = held.toList()

    /** Every layer held, its id and its rank, in the order the layers were placed. */
    val placement: List<PlacedLayer>
        get() = placed.mapNotNull { id -> layer(id)?.let { PlacedLayer(id, it.rank) } }

    /**
     * Places [layer] above every layer of its rank or a lesser one and below every layer of a greater rank, or,
     * where a layer of its id is held at its rank, in that layer's place.
     */
    fun add(layer: MapLayer) {
        val index = held.indexOfFirst { it.id == layer.id }
        if (index >= 0 && held[index].rank == layer.rank) {
            held[index] = layer
            return
        }
        placed.remove(layer.id)
        placed += layer.id
        held.removeAll { it.id == layer.id }
        val place = held.indexOfFirst { it.rank > layer.rank }
        held.add(if (place < 0) held.size else place, layer)
    }

    /** The layer held under [id], or null where none is. */
    fun layer(id: String): MapLayer? = held.firstOrNull { it.id == id }

    /** Changes the layer held under [id] in its place, keeping its rank, and tells whether one was held. */
    fun replace(
        id: String,
        layer: MapLayer,
    ): Boolean {
        val place = held.indexOfFirst { it.id == id }
        if (place < 0) return false
        held[place] = layer
        return true
    }

    /** Takes the layer held under [id] off, and tells whether one was held. */
    fun remove(id: String): Boolean {
        placed.remove(id)
        return held.removeAll { it.id == id }
    }

    /** Takes off every layer drawing from the source of id [source]. */
    fun removeLayers(source: String) {
        val drawing = held.filter { it.source == source }.map { it.id }.toSet()
        held.removeAll { it.source == source }
        placed.removeAll { it in drawing }
    }
}
