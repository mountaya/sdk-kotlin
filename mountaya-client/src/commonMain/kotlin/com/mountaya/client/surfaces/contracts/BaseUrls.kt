package com.mountaya.client

/**
 * BaseUrls is where the client sends each surface's requests.
 *
 * Each surface has its own origin, and each is overridden on its own: pointing the Data API at
 * a proxy leaves the REST API and the tiles where they were. An origin with no scheme, a path or a
 * host alone, is refused with a [ConfigurationException] where it is given, since no request it
 * would address goes anywhere a caller meant.
 */
public class BaseUrls(
    /** The Data API's origin. */
    public val data: Url = standard.data,
    /** The REST API's origin. */
    public val rest: Url = standard.rest,
    /** The Tile API's origin. */
    public val tiles: Url = standard.tiles,
) {
    init {
        for ((surface, origin) in listOf("Data API" to data, "REST API" to rest, "Tile API" to tiles)) {
            if (!origin.hasScheme) {
                throw ConfigurationException(
                    "The $surface's base URL `$origin` is not a URL: a URL starts with a scheme, such as `https:`.",
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean =
        other is BaseUrls && other.data == data && other.rest == rest && other.tiles == tiles

    override fun hashCode(): Int = (data.hashCode() * 31 + rest.hashCode()) * 31 + tiles.hashCode()

    override fun toString(): String = "BaseUrls(data=$data, rest=$rest, tiles=$tiles)"

    /** The origins Mountaya serves its public surfaces from. */
    public companion object {
        /** The origins Mountaya serves its public surfaces from, which a client uses unless told otherwise. */
        public val standard: BaseUrls =
            BaseUrls(
                data = Url("https://data.mountaya.com"),
                rest = Url("https://api.mountaya.com"),
                tiles = Url("https://tiles.mountaya.com"),
            )
    }
}
