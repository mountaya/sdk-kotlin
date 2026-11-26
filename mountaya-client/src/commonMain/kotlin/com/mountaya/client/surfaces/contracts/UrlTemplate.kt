package com.mountaya.client

/**
 * UrlTemplate is a route a map renderer fetches on its own: a tile, or the base map's style, TileJSON, sprite or
 * glyph document (SPEC.md section 3). A renderer reads it, never a caller who awaits it, so no method binds one:
 * `TileTemplates` holds every one the contract declares.
 *
 * Its path is resolved against its surface's origin, `BaseUrls.tiles` for a Tile API route, and a renderer expands
 * the `{z}`, `{x}` and `{y}` it leaves in place on its own schedule. Two are equal where they state the same.
 */
public class UrlTemplate(
    /** The contract's name for the route. */
    public val operationId: String,
    /** The path template, each parameter in braces, resolved against its surface's origin. */
    public val path: String,
    /** The path's parameters, in the order the path names them. */
    public val pathParameters: List<String> = emptyList(),
    /** The query parameters the route takes, in the contract's order. */
    public val queryParameters: List<String> = emptyList(),
    /** The media types the server answers the route in, in the contract's order. */
    public val mediaTypes: List<String> = emptyList(),
) {
    /**
     * Returns [path] with each placeholder [values] names replaced by its value, percent-encoded as one path segment,
     * and every other placeholder left as written, as a renderer's `{z}`, `{x}` and `{y}` are:
     * `TileTemplates.getItineraryTile.filled(mapOf("itinerary_id" to id))` is `/v1/itineraries/<id>/{z}/{x}/{y}`.
     *
     * A value is encoded before it is placed, so its braces are `%7B` and `%7D` and no value writes a placeholder
     * another one fills: the order the values are placed in decides nothing.
     */
    public fun filled(values: Map<String, String>): String {
        var filled = path
        for ((name, value) in values) filled = filled.replace("{$name}", encodePathSegment(value))
        return filled
    }

    override fun equals(other: Any?): Boolean =
        other is UrlTemplate &&
            other.operationId == operationId &&
            other.path == path &&
            other.pathParameters == pathParameters &&
            other.queryParameters == queryParameters &&
            other.mediaTypes == mediaTypes

    override fun hashCode(): Int =
        listOf(path, pathParameters, queryParameters, mediaTypes)
            .fold(operationId.hashCode()) { hash, part -> hash * 31 + part.hashCode() }

    override fun toString(): String = "UrlTemplate($operationId: $path)"
}
