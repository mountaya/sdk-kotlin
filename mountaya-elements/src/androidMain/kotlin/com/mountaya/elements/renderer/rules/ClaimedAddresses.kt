package com.mountaya.elements

/**
 * claimedAddresses answers the beginning of every address a map drawing [state] claims as its own, which the
 * process's request hook tells an answer to where several maps are live (`MapRequestHook`): the text before
 * the first `{` of each of its sources' tile templates, which the address of each of their tiles starts
 * with, and its style's directory, the style's address cut before its query and its fragment, then up to and
 * including the last `/` of its path, under which a Mountaya terrain's document, tiles, sprites, glyphs and
 * sources sit, and what an app's own style names beside it; a style address of no path claims nothing by its
 * directory. A base map's tile another host serves, a font a style names elsewhere among them, is claimed by
 * no map.
 */
internal fun claimedAddresses(state: MapState): List<String> =
    buildList {
        for (source in state.sources) addAll(tilePrefixes(source))
        state.style?.let { styleDirectory(it.toString()) }?.let(::add)
    }

/**
 * The text before the first `{` of each of [source]'s tile templates, where it holds any, which the address of
 * each of its tiles starts with: a template of no placeholder is one tile's whole address.
 */
internal fun tilePrefixes(source: MapSource): List<String> =
    when (val kind = source.kind) {
        is MapSource.Kind.Vector -> kind.tiles
        is MapSource.Kind.Raster -> kind.tiles
        is MapSource.Kind.RasterDem -> kind.tiles
        is MapSource.Kind.GeoJson -> emptyList()
    }.map { it.substringBefore('{') }.filter(String::isNotEmpty)

/**
 * [address] cut before its query and its fragment, then up to and including the last `/` of its path, or null
 * where it holds no path or names no authority, which claims nothing, as `sdk-swift` and `sdk-dart` read it.
 */
private fun styleDirectory(address: String): String? {
    val bare = address.substringBefore('#').substringBefore('?')
    val authority = bare.indexOf("://").takeIf { it >= 0 }?.plus("://".length) ?: return null
    if (bare.indexOf('/', authority) < 0) return null
    return bare.substring(0, bare.lastIndexOf('/') + 1)
}
