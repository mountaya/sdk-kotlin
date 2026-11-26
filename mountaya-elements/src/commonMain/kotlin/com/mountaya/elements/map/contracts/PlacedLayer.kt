package com.mountaya.elements

/**
 * PlacedLayer is a layer a map placed in its rank bands, as a point query reads it: its id and the rank it
 * was placed at (`map.query.layers`). Two are equal where both are.
 */
internal class PlacedLayer(
    /** The layer's id. */
    val id: String,
    /** The rank the layer was placed at. */
    val rank: Int,
) {
    override fun equals(other: Any?): Boolean = other is PlacedLayer && other.id == id && other.rank == rank

    override fun hashCode(): Int = id.hashCode() * 31 + rank

    override fun toString(): String = "PlacedLayer($id, $rank)"
}
