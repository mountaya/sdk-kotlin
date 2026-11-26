package com.mountaya.elements

/**
 * SourcePushes is what the renderer sends MapLibre to bring the sources it drew to a state's: the GeoJSON sources given
 * their new features in place, the sources taken off, every layer drawing from them with them, and the sources added,
 * one taken off for another kind of tiles among them. MapLibre parses and tiles again every source it is sent, so a
 * change sends the sources it changed alone, and a scrub frame the cursor's alone (`source-pushes` in the contract's
 * `sdk/hot-paths.yaml`).
 */
internal class SourcePushes(
    /** The GeoJSON sources drawn already whose features changed, each given its new ones in place, in the order drawn. */
    val refreshed: List<MapSource>,
    /** The id of every source taken off: gone from the state, or serving another kind of tiles than the one drawn. */
    val removed: Set<String>,
    /** The sources added, in the state's order: each the renderer drew none of, or one it took off for another kind. */
    val added: List<MapSource>,
) {
    /** The ids of the sources MapLibre is sent, each given its features again or added, which it parses and tiles anew. */
    val sent: List<String> get() = refreshed.map { it.id } + added.map { it.id }
}

/**
 * sourcePushes answers what brings the sources the renderer drew, [drawn] by id, to [wanted], a state's sources: a
 * source equal to the one drawn is sent nothing; GeoJSON drawn as GeoJSON is given its new features in place; and a
 * source gone, or serving another kind of tiles, is taken off, to be added again where the state still holds it, with
 * every source the renderer drew none of.
 */
internal fun sourcePushes(
    drawn: Map<String, MapSource>,
    wanted: List<MapSource>,
): SourcePushes {
    val refreshed = mutableListOf<MapSource>()
    val removed = mutableSetOf<String>()
    for ((id, held) in drawn) {
        val source = wanted.firstOrNull { it.id == id }
        if (source == held) continue
        if (source != null && source.kind is MapSource.Kind.GeoJson && held.kind is MapSource.Kind.GeoJson) {
            refreshed += source
            continue
        }
        removed += id
    }
    val added = wanted.filter { it.id !in drawn || it.id in removed }
    return SourcePushes(refreshed, removed, added)
}
