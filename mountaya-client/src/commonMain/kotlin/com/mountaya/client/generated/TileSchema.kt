// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

@file:kotlinx.serialization.UseSerializers(com.mountaya.client.TimestampSerializer::class)

package com.mountaya.client.generated

/** TileSchema is the types of the Tile API, as the contract names them. */
public object TileSchema {
    /**
     * A named group of overlay layers in the built-in layer catalog. The group is
     * not itself renderable: `children` holds the overlays that can be added to a
     * map, and `locales` carries the group's display name per language.
     */
    @kotlinx.serialization.Serializable
    public class CategoryOverlays private constructor(
        /**
         * The overlays in this group. These are the renderable entries; the group
         * itself is not.
         */
        public val children: kotlin.collections.List<TileSchema.Overlay>,
        /** Identifier of the overlay group. */
        public val id: kotlin.String,
        /**
         * The category's display name per language. Render from here rather than
         * from `slug`: the slug is a stable identifier and is not translated.
         */
        public val locales: TileSchema.Locales,
        /**
         * Stable, URL-safe identifier of the overlay group. Unlike the display name
         * in `locales`, it does not change between languages or releases. Lowercase
         * letters, digits and single hyphens, the same shape every slug on this
         * surface carries.
         */
        public val slug: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.CategoryOverlays &&
                other.children == this.children &&
                other.id == this.id &&
                other.locales == this.locales &&
                other.slug == this.slug

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.children.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.locales.hashCode()
            hash = 31 * hash + this.slug.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "CategoryOverlays(" +
                "children=" + this.children +
                ", id=" + this.id +
                ", locales=" + this.locales +
                ", slug=" + this.slug +
                ")"

        /** Builder sets the optional properties of a [TileSchema.CategoryOverlays], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.CategoryOverlays]. */
        public companion object {
            /** Returns a [TileSchema.CategoryOverlays] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                children: kotlin.collections.List<TileSchema.Overlay>,
                id: kotlin.String,
                locales: TileSchema.Locales,
                slug: kotlin.String,
                optional: TileSchema.CategoryOverlays.Builder.() -> kotlin.Unit = {},
            ): TileSchema.CategoryOverlays {
                val given = TileSchema.CategoryOverlays.Builder().apply(optional)
                return TileSchema.CategoryOverlays(
                    children = children,
                    id = id,
                    locales = locales,
                    slug = slug,
                )
            }
        }
    }

    /**
     * The elevation raster a map bakes its relief from: a `raster-dem` source whose
     * pixels encode height rather than colour.
     *
     * It is not a layer a caller turns on. Nothing draws it, and a map that adds it
     * as an ordinary source shows a garish false-colour image. It is consumed as the
     * terrain source a renderer reads heights out of, which is what turns a flat base
     * map into a three-dimensional one and what a hillshade or a slope reading is
     * computed against.
     *
     * Served through a TileJSON document rather than a tile template, because a
     * renderer needs the encoding, bounds and zoom range before it can decode a
     * single pixel.
     */
    @kotlinx.serialization.Serializable
    public class Elevation private constructor(
        /**
         * Identifier of the elevation raster. It belongs to no terrain, so it has an
         * identifier of its own, which stays the same across releases.
         */
        public val id: kotlin.String,
        /**
         * The tile size to declare when this source is added to a map. Only one
         * value is served, and declaring a different one offsets every zoom level.
         */
        public val tile_size: TileSchema.ElevationTileSize,
        /**
         * URL of the TileJSON document describing the elevation source, ready to
         * hand to a map renderer unmodified. It is the Tile API's
         * `getElevationTileJson`, whose tile template is the Tile API's
         * `getElevationTile`, so the heights are drawn under the credential a map
         * sends there, and no URL carries a credential of Mountaya's. Uncapped,
         * unlike a URL a caller supplies: Mountaya issues this one, so there is no
         * untrusted input for a ceiling to bound.
         */
        public val url: com.mountaya.client.Url,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.Elevation &&
                other.id == this.id &&
                other.tile_size == this.tile_size &&
                other.url == this.url

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.tile_size.hashCode()
            hash = 31 * hash + this.url.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "Elevation(" +
                "id=" + this.id +
                ", tile_size=" + this.tile_size +
                ", url=" + this.url +
                ")"

        /** Builder sets the optional properties of a [TileSchema.Elevation], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.Elevation]. */
        public companion object {
            /** Returns a [TileSchema.Elevation] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                tile_size: TileSchema.ElevationTileSize,
                url: com.mountaya.client.Url,
                optional: TileSchema.Elevation.Builder.() -> kotlin.Unit = {},
            ): TileSchema.Elevation {
                val given = TileSchema.Elevation.Builder().apply(optional)
                return TileSchema.Elevation(
                    id = id,
                    tile_size = tile_size,
                    url = url,
                )
            }
        }
    }

    /**
     * Edge length in pixels of one elevation raster tile. Only 512 is served, and a
     * client that assumed 256 would request four times as many tiles for the same
     * ground and read every height off by a zoom level.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ElevationTileSize(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.Int,
    ) {
        override fun toString(): kotlin.String = value.toString()

        /** The values the contract names. */
        public companion object {
            /** A 512 by 512 pixel tile, the only size the elevation source publishes. */
            public val Size512: ElevationTileSize = ElevationTileSize(512)
        }
    }

    /**
     * The built-in map layer catalog: the terrain base maps, the grouped overlay
     * layers, and the two layers that render itineraries and zones. `itineraries`
     * and `zones` are typed as overlays because that is what they are to a map: one
     * tile source each, registered the same way. But they are singular and always
     * present, so they sit beside the `overlays` groups rather than inside them.
     *
     * `elevation` is the one entry that draws nothing. It is the height raster the
     * terrains bake their relief from, and it is always present: the terrains and
     * the raster they are shaded from are published together, so a catalog that
     * offers a terrain offers the heights that terrain is drawn against.
     */
    @kotlinx.serialization.Serializable
    public class Layers private constructor(
        /**
         * The height raster a renderer turns into three-dimensional relief. Every
         * catalog carries one, so a client registers the terrain source
         * unconditionally rather than branching on whether heights are available.
         */
        public val elevation: TileSchema.Elevation,
        /**
         * The single layer that draws the itineraries this credential reaches, in
         * the studio scope `organization_id` names when the catalog was asked for
         * one, which its URL template then carries. It is typed as an overlay because that is what it is to a map, one tile source
         * registered the same way, and it sits here rather than inside `overlays`
         * because it is singular and always present: there is one of it, for every
         * caller, and no group to file it under.
         */
        public val itineraries: TileSchema.Overlay,
        /**
         * The overlay catalog, grouped. Each group holds the overlays a caller can
         * add to a map. Under a publishable key or a share token the overlays a map
         * draws are the ones the organization's overlay allowlist admits, and the
         * Tile API holds every overlay tile to it. The Tile API's own catalog,
         * `getTileLayers`, lists only those, dropping a group left with none; the
         * Map Studio API's `getLayers` lists every overlay whatever the allowlist
         * says. From Mountaya's web application both list every overlay, in the
         * person's own space and in each organization they are a member of, while
         * the Tile API draws in the studio scope `organization_id` names: the points
         * of interest alone in the person's own space and in an organization that
         * does not hold Map Studio, and every overlay in one that does.
         */
        public val overlays: kotlin.collections.List<TileSchema.CategoryOverlays>,
        /** The terrain base maps a caller can draw itineraries and zones on. */
        public val terrains: kotlin.collections.List<TileSchema.Terrain>,
        /**
         * The single layer that draws the zones this credential reaches, in the
         * studio scope `organization_id` names when the catalog was asked for one,
         * which its URL template then carries. Typed as an overlay and kept outside
         * the `overlays` groups for the reason `itineraries` gives.
         */
        public val zones: TileSchema.Overlay,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.Layers &&
                other.elevation == this.elevation &&
                other.itineraries == this.itineraries &&
                other.overlays == this.overlays &&
                other.terrains == this.terrains &&
                other.zones == this.zones

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.elevation.hashCode()
            hash = 31 * hash + this.itineraries.hashCode()
            hash = 31 * hash + this.overlays.hashCode()
            hash = 31 * hash + this.terrains.hashCode()
            hash = 31 * hash + this.zones.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "Layers(" +
                "elevation=" + this.elevation +
                ", itineraries=" + this.itineraries +
                ", overlays=" + this.overlays +
                ", terrains=" + this.terrains +
                ", zones=" + this.zones +
                ")"

        /** Builder sets the optional properties of a [TileSchema.Layers], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.Layers]. */
        public companion object {
            /** Returns a [TileSchema.Layers] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                elevation: TileSchema.Elevation,
                itineraries: TileSchema.Overlay,
                overlays: kotlin.collections.List<TileSchema.CategoryOverlays>,
                terrains: kotlin.collections.List<TileSchema.Terrain>,
                zones: TileSchema.Overlay,
                optional: TileSchema.Layers.Builder.() -> kotlin.Unit = {},
            ): TileSchema.Layers {
                val given = TileSchema.Layers.Builder().apply(optional)
                return TileSchema.Layers(
                    elevation = elevation,
                    itineraries = itineraries,
                    overlays = overlays,
                    terrains = terrains,
                    zones = zones,
                )
            }
        }
    }

    /** A map of locale keys (e.g. "name", "description") to nullable string values. */
    public typealias LocaleMap = kotlin.collections.Map<kotlin.String, kotlin.String?>

    /**
     * Localized text for a tile legend, keyed by language code. Each language maps
     * to a set of named strings (see LocaleMap); a language is null when no
     * translation is provided for it.
     */
    @kotlinx.serialization.Serializable
    public class Locales private constructor(
        /**
         * The German strings for this legend. Null when the legend carries no German
         * translation, which is the case a client falls back from rather than
         * renders.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val de: TileSchema.LocaleMap? = null,
        /**
         * The English strings for this legend. Null when the legend carries no
         * English translation, which is the case a client falls back from rather
         * than renders.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val en: TileSchema.LocaleMap? = null,
        /**
         * The Spanish strings for this legend. Null when the legend carries no
         * Spanish translation, which is the case a client falls back from rather
         * than renders.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val es: TileSchema.LocaleMap? = null,
        /**
         * The French strings for this legend. Null when the legend carries no French
         * translation, which is the case a client falls back from rather than
         * renders.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val fr: TileSchema.LocaleMap? = null,
        /**
         * The Italian strings for this legend. Null when the legend carries no
         * Italian translation, which is the case a client falls back from rather
         * than renders.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val it: TileSchema.LocaleMap? = null,
        /**
         * The Japanese strings for this legend. Null when the legend carries no
         * Japanese translation, which is the case a client falls back from rather
         * than renders.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val ja: TileSchema.LocaleMap? = null,
        /**
         * The Chinese strings for this legend. Null when the legend carries no
         * Chinese translation, which is the case a client falls back from rather
         * than renders.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val zh: TileSchema.LocaleMap? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.Locales &&
                other.de == this.de &&
                other.en == this.en &&
                other.es == this.es &&
                other.fr == this.fr &&
                other.it == this.it &&
                other.ja == this.ja &&
                other.zh == this.zh

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.de?.hashCode() ?: 0)
            hash = 31 * hash + (this.en?.hashCode() ?: 0)
            hash = 31 * hash + (this.es?.hashCode() ?: 0)
            hash = 31 * hash + (this.fr?.hashCode() ?: 0)
            hash = 31 * hash + (this.it?.hashCode() ?: 0)
            hash = 31 * hash + (this.ja?.hashCode() ?: 0)
            hash = 31 * hash + (this.zh?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "Locales(" +
                "de=" + this.de +
                ", en=" + this.en +
                ", es=" + this.es +
                ", fr=" + this.fr +
                ", it=" + this.it +
                ", ja=" + this.ja +
                ", zh=" + this.zh +
                ")"

        /** Builder sets the optional properties of a [TileSchema.Locales], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.Locales]. */
        public companion object {
            /** Returns a [TileSchema.Locales] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                de: TileSchema.LocaleMap?,
                en: TileSchema.LocaleMap?,
                es: TileSchema.LocaleMap?,
                fr: TileSchema.LocaleMap?,
                it: TileSchema.LocaleMap?,
                ja: TileSchema.LocaleMap?,
                zh: TileSchema.LocaleMap?,
                optional: TileSchema.Locales.Builder.() -> kotlin.Unit = {},
            ): TileSchema.Locales {
                val given = TileSchema.Locales.Builder().apply(optional)
                return TileSchema.Locales(
                    de = de,
                    en = en,
                    es = es,
                    fr = fr,
                    it = it,
                    ja = ja,
                    zh = zh,
                )
            }
        }
    }

    /**
     * A built-in overlay layer drawn on top of a terrain base map, such as slope
     * steepness or snow depth. Carries everything needed to add it to a map: the
     * tile sources to register and which side of the wire handles each optional
     * capability.
     */
    @kotlinx.serialization.Serializable
    public class Overlay private constructor(
        /**
         * Which of this overlay's options each side of the wire resolves, so read
         * this before building the control. Where `server.timestamp` is true, a
         * tile's `timestamp` selects the instant it depicts. Where it is false, the
         * instant sent is ignored, and any change over time is the client's to draw
         * from what it already holds. Two settings are refused. A `timestamp` that
         * is not an RFC 3339 instant, or that lies more than three days before or
         * six days after the request, is a `400` on every overlay tile, whether or
         * not the overlay has a time dimension. Where `server.timestamp` is true, an
         * instant more than 15 minutes from the current one is a `403` unless the
         * organization's `with_overlays_control` safety rule allows it, a
         * share-token request included.
         */
        public val capabilities: TileSchema.OverlayCapabilities,
        /**
         * What the overlay's tiles carry, which decides how they are drawn: a raster
         * overlay is an image layer, a vector one is styled by the layers its
         * specification declares. For a raster overlay it names the format of the
         * tiles within its source's zoom range; a tile asked for deeper than that
         * is cut from its ancestor and is always PNG, so read each tile's
         * `Content-Type` header rather than this.
         */
        public val content_type: TileSchema.OverlayContentType,
        /**
         * Identifier of the overlay. `slug` is the value the Tile API takes; this is
         * the one the catalog keys on.
         */
        public val id: kotlin.String,
        /**
         * The overlay's display name and blurb per language. A language absent here
         * is null rather than missing, which is the case to fall back from.
         */
        public val locales: TileSchema.Locales,
        /**
         * Stable, URL-safe identifier of the overlay, and the value the Tile API
         * takes in `/v1/overlays/{overlay}/...`. Lowercase letters, digits and
         * single hyphens, so it is safe in a path segment without escaping. The
         * shape is declared here and on that path parameter identically, because a
         * value this read hands a client is a value that client sends straight back.
         */
        public val slug: kotlin.String,
        /**
         * Tile sources to register with the map renderer before this overlay can be
         * drawn.
         */
        public val sources: kotlin.collections.List<TileSchema.TileSource>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.Overlay &&
                other.capabilities == this.capabilities &&
                other.content_type == this.content_type &&
                other.id == this.id &&
                other.locales == this.locales &&
                other.slug == this.slug &&
                other.sources == this.sources

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.capabilities.hashCode()
            hash = 31 * hash + this.content_type.hashCode()
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.locales.hashCode()
            hash = 31 * hash + this.slug.hashCode()
            hash = 31 * hash + this.sources.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "Overlay(" +
                "capabilities=" + this.capabilities +
                ", content_type=" + this.content_type +
                ", id=" + this.id +
                ", locales=" + this.locales +
                ", slug=" + this.slug +
                ", sources=" + this.sources +
                ")"

        /** Builder sets the optional properties of a [TileSchema.Overlay], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.Overlay]. */
        public companion object {
            /** Returns a [TileSchema.Overlay] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                capabilities: TileSchema.OverlayCapabilities,
                content_type: TileSchema.OverlayContentType,
                id: kotlin.String,
                locales: TileSchema.Locales,
                slug: kotlin.String,
                sources: kotlin.collections.List<TileSchema.TileSource>,
                optional: TileSchema.Overlay.Builder.() -> kotlin.Unit = {},
            ): TileSchema.Overlay {
                val given = TileSchema.Overlay.Builder().apply(optional)
                return TileSchema.Overlay(
                    capabilities = capabilities,
                    content_type = content_type,
                    id = id,
                    locales = locales,
                    slug = slug,
                    sources = sources,
                )
            }
        }
    }

    /**
     * Where each of an overlay's optional capabilities is handled. The two sides are
     * declared separately because they are independent: the same capability can be
     * answered in the tile, answered by the client, answered by both, or answered by
     * neither.
     */
    @kotlinx.serialization.Serializable
    public class OverlayCapabilities private constructor(
        /**
         * Which of the optional behaviours the CLIENT is expected to provide for
         * this overlay, such as restyling data it already holds when the moment
         * changes.
         */
        public val client: TileSchema.OverlaySupportedCapabilities,
        /**
         * Which of the optional behaviours the SERVER provides for this overlay,
         * such as serving the tile for a requested instant.
         */
        public val server: TileSchema.OverlaySupportedCapabilities,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.OverlayCapabilities &&
                other.client == this.client &&
                other.server == this.server

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.client.hashCode()
            hash = 31 * hash + this.server.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OverlayCapabilities(" +
                "client=" + this.client +
                ", server=" + this.server +
                ")"

        /** Builder sets the optional properties of a [TileSchema.OverlayCapabilities], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.OverlayCapabilities]. */
        public companion object {
            /** Returns a [TileSchema.OverlayCapabilities] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                client: TileSchema.OverlaySupportedCapabilities,
                server: TileSchema.OverlaySupportedCapabilities,
                optional: TileSchema.OverlayCapabilities.Builder.() -> kotlin.Unit = {},
            ): TileSchema.OverlayCapabilities {
                val given = TileSchema.OverlayCapabilities.Builder().apply(optional)
                return TileSchema.OverlayCapabilities(
                    client = client,
                    server = server,
                )
            }
        }
    }

    /**
     * MIME type of overlay tile content.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class OverlayContentType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Raster PNG tile. */
            public val Png: OverlayContentType = OverlayContentType("image/png")

            /** Vector Mapbox Vector Tile (MVT) in Protocol Buffer format. */
            public val Protobuf: OverlayContentType = OverlayContentType("application/x-protobuf")

            /** Raster WebP tile. */
            public val Webp: OverlayContentType = OverlayContentType("image/webp")
        }
    }

    /**
     * The optional overlay behaviours one side of the wire supports. `timestamp`
     * means that side resolves the moment the overlay depicts: server-side by
     * serving the tile for a requested instant, client-side by restyling data it
     * already holds. False on both sides means the overlay has no time dimension.
     */
    @kotlinx.serialization.Serializable
    public class OverlaySupportedCapabilities private constructor(
        /** Whether this side of the wire resolves the moment the overlay depicts. */
        public val timestamp: kotlin.Boolean,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.OverlaySupportedCapabilities &&
                other.timestamp == this.timestamp

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.timestamp.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "OverlaySupportedCapabilities(" +
                "timestamp=" + this.timestamp +
                ")"

        /** Builder sets the optional properties of a [TileSchema.OverlaySupportedCapabilities], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.OverlaySupportedCapabilities]. */
        public companion object {
            /** Returns a [TileSchema.OverlaySupportedCapabilities] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                timestamp: kotlin.Boolean,
                optional: TileSchema.OverlaySupportedCapabilities.Builder.() -> kotlin.Unit = {},
            ): TileSchema.OverlaySupportedCapabilities {
                val given = TileSchema.OverlaySupportedCapabilities.Builder().apply(optional)
                return TileSchema.OverlaySupportedCapabilities(
                    timestamp = timestamp,
                )
            }
        }
    }

    /**
     * Which snapshot of a resource to read. `published` is the only accepted value:
     * there is deliberately no `draft` spelling, because the parameter only ever
     * narrows what the caller is served, and omitting it already means "whatever I
     * am authorized to see". An unrecognized value is rejected with a validation
     * error rather than silently ignored.
     *
     * Declared once and shared, because the same single-value vocabulary is accepted
     * by the itinerary and zone reads on the Map Studio API and by the itinerary,
     * zone and collection tiles and the collection's specification on the Tile
     * API. A second value added here has to mean the same thing on both.
     *
     * It is one of two vocabularies spelled `surface` on this contract, and the one
     * about CONTENT. `UsageTileSurface` names where a counted request came from,
     * decided by the server after the fact. This one names which snapshot a single
     * read returns, and is decided by the caller on every request.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class ReadSurface(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Read the published snapshot, never the draft. */
            public val Published: ReadSurface = ReadSurface("published")
        }
    }

    /**
     * A built-in terrain: the base map itineraries and zones are drawn on, such as
     * the summer topographic map or satellite imagery. `variants` carries its light
     * and dark renditions, `locales` its display name per language.
     */
    @kotlinx.serialization.Serializable
    public class Terrain private constructor(
        /**
         * Identifier of the terrain. `slug` is the value a person reads; this is the
         * one the catalog keys on.
         */
        public val id: kotlin.String,
        /**
         * The terrain's display name per language. Render from here rather than from
         * `slug`: the slug is a stable identifier and is not translated.
         */
        public val locales: TileSchema.Locales,
        /**
         * Stable, URL-safe identifier of the terrain. Unlike the display name in
         * `locales`, it does not change between languages or releases. Lowercase
         * letters, digits and single hyphens, the same shape every slug on this
         * surface carries.
         */
        public val slug: kotlin.String,
        /**
         * The light and dark renditions of this terrain. Both are always present, so
         * a client follows the user's color scheme without fetching the catalog
         * again.
         */
        public val variants: TileSchema.TerrainVariants,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.Terrain &&
                other.id == this.id &&
                other.locales == this.locales &&
                other.slug == this.slug &&
                other.variants == this.variants

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.locales.hashCode()
            hash = 31 * hash + this.slug.hashCode()
            hash = 31 * hash + this.variants.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "Terrain(" +
                "id=" + this.id +
                ", locales=" + this.locales +
                ", slug=" + this.slug +
                ", variants=" + this.variants +
                ")"

        /** Builder sets the optional properties of a [TileSchema.Terrain], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.Terrain]. */
        public companion object {
            /** Returns a [TileSchema.Terrain] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                locales: TileSchema.Locales,
                slug: kotlin.String,
                variants: TileSchema.TerrainVariants,
                optional: TileSchema.Terrain.Builder.() -> kotlin.Unit = {},
            ): TileSchema.Terrain {
                val given = TileSchema.Terrain.Builder().apply(optional)
                return TileSchema.Terrain(
                    id = id,
                    locales = locales,
                    slug = slug,
                    variants = variants,
                )
            }
        }
    }

    /**
     * The index of a terrain's sprite sheet, in the sprite format of the MapLibre
     * Style Specification: one entry per icon, keyed by the icon's name, giving
     * where the icon sits on the sheet `getTerrainSprite` serves beside it as a
     * PNG. A map renderer reads it on its own; a client never needs to.
     */
    public typealias TerrainSpriteIndex = kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>

    /**
     * A terrain rendition as a map style document, in the MapLibre Style
     * Specification (https://maplibre.org/maplibre-style-spec/), ready to hand to
     * a map renderer unmodified. It is the base-map provider's own style with every
     * address rewritten to point at this API: each source that fetches tiles
     * carries its `tiles` template on `getTerrainSourceTile` rather than a `url` of
     * its own (a source naming no address, such as an attribution-only one, is left
     * as the provider wrote it), `sprite` names `getTerrainSprite`, and `glyphs`
     * names `getTerrainGlyph`, each under the same terrain and rendition as the style. So
     * a renderer that loads it reaches the base map through this API alone, under
     * the credential it sends here, and no address in it carries a credential of
     * Mountaya's.
     *
     * The document is the provider's format rather than this contract's, so its
     * members are not declared here, and a member the style specification adds
     * reaches a client unchanged.
     */
    public typealias TerrainStyle = kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>

    /**
     * Edge length in pixels of one terrain raster tile. Only 512 is served: the
     * terrain sources are published as 512px tiles, and a client that assumed 256
     * would request four times as many tiles for the same ground.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TerrainTileSize(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.Int,
    ) {
        override fun toString(): kotlin.String = value.toString()

        /** The values the contract names. */
        public companion object {
            /** A 512 by 512 pixel tile, the only size the terrain sources publish. */
            public val Size512: TerrainTileSize = TerrainTileSize(512)
        }
    }

    /**
     * Visual style variant for terrain map tiles.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TerrainVariant(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Light terrain style suited for daytime use. */
            public val Light: TerrainVariant = TerrainVariant("light")

            /** Dark terrain style suited for low-light use. */
            public val Dark: TerrainVariant = TerrainVariant("dark")
        }
    }

    /**
     * A terrain rendition as a complete map style document, referenced by URL and
     * ready to hand to a map renderer unmodified. The URL is the Tile API's
     * `getTerrainStyle`, and every address the style holds is the Tile API's as
     * well, so a map draws the whole base map under the credential it sends there,
     * and no URL carries a credential of Mountaya's.
     */
    @kotlinx.serialization.Serializable
    public class TerrainVariantAsStyle private constructor(
        /**
         * URL of the style document, ready to hand to a map renderer unmodified.
         * Uncapped, unlike a URL a caller supplies: Mountaya issues this one, so
         * there is no untrusted input for a ceiling to bound.
         */
        public val url: com.mountaya.client.Url,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TerrainVariantAsStyle &&
                other.url == this.url

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.url.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainVariantAsStyle(" +
                "url=" + this.url +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TerrainVariantAsStyle], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.TerrainVariantAsStyle]. */
        public companion object {
            /** Returns a [TileSchema.TerrainVariantAsStyle] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                url: com.mountaya.client.Url,
                optional: TileSchema.TerrainVariantAsStyle.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TerrainVariantAsStyle {
                val given = TileSchema.TerrainVariantAsStyle.Builder().apply(optional)
                return TileSchema.TerrainVariantAsStyle(
                    url = url,
                )
            }
        }
    }

    /**
     * A terrain rendition as a tile source alone, referenced by the URL of its
     * TileJSON document, together with the tile size to declare when the source is
     * added to a map. Declaring a different size offsets every zoom level.
     */
    @kotlinx.serialization.Serializable
    public class TerrainVariantAsTiles private constructor(
        /**
         * The tile size to declare when this source is added to a map. Only one
         * value is served, and declaring a different one offsets every zoom level.
         */
        public val size: TileSchema.TerrainTileSize,
        /**
         * URL of the TileJSON document describing this rendition's tiles: where
         * they are fetched from, their zoom range and their attribution. It is the
         * Tile API's `getTerrainTileJson`, whose tile template is the Tile API's
         * `getTerrainTile`, so the tiles are drawn under the credential a map
         * sends there, and no URL carries a credential of Mountaya's. It is a
         * resolvable address rather than a tile template, so register it as the
         * source's `url` rather than among its `tiles`, and hand it to the map
         * renderer unmodified. Uncapped, unlike a URL a caller supplies: Mountaya
         * issues this one, so there is no untrusted input for a ceiling to bound.
         */
        public val url: com.mountaya.client.Url,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TerrainVariantAsTiles &&
                other.size == this.size &&
                other.url == this.url

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.size.hashCode()
            hash = 31 * hash + this.url.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainVariantAsTiles(" +
                "size=" + this.size +
                ", url=" + this.url +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TerrainVariantAsTiles], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.TerrainVariantAsTiles]. */
        public companion object {
            /** Returns a [TileSchema.TerrainVariantAsTiles] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                size: TileSchema.TerrainTileSize,
                url: com.mountaya.client.Url,
                optional: TileSchema.TerrainVariantAsTiles.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TerrainVariantAsTiles {
                val given = TileSchema.TerrainVariantAsTiles.Builder().apply(optional)
                return TileSchema.TerrainVariantAsTiles(
                    size = size,
                    url = url,
                )
            }
        }
    }

    /**
     * One rendition of a terrain, offered in both consumable forms. `style` is a
     * complete map style document, for a client that renders the terrain as-is;
     * `tiles` is the tile source alone, described by a TileJSON document, for a
     * client composing its own style. The two describe the same map, so consume
     * one, not both.
     */
    @kotlinx.serialization.Serializable
    public class TerrainVariantConfig private constructor(
        /**
         * This rendition as a complete map style document, for a client that renders
         * the terrain as it is given.
         */
        public val style: TileSchema.TerrainVariantAsStyle,
        /**
         * This rendition as a tile source alone, described by a TileJSON document,
         * for a client composing a style of its own.
         */
        public val tiles: TileSchema.TerrainVariantAsTiles,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TerrainVariantConfig &&
                other.style == this.style &&
                other.tiles == this.tiles

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.style.hashCode()
            hash = 31 * hash + this.tiles.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainVariantConfig(" +
                "style=" + this.style +
                ", tiles=" + this.tiles +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TerrainVariantConfig], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.TerrainVariantConfig]. */
        public companion object {
            /** Returns a [TileSchema.TerrainVariantConfig] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                style: TileSchema.TerrainVariantAsStyle,
                tiles: TileSchema.TerrainVariantAsTiles,
                optional: TileSchema.TerrainVariantConfig.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TerrainVariantConfig {
                val given = TileSchema.TerrainVariantConfig.Builder().apply(optional)
                return TileSchema.TerrainVariantConfig(
                    style = style,
                    tiles = tiles,
                )
            }
        }
    }

    /**
     * The light and dark renditions of a terrain. Both are always present, so a
     * client can follow the user's color scheme without refetching the catalog.
     */
    @kotlinx.serialization.Serializable
    public class TerrainVariants private constructor(
        /** The rendition to draw under a dark color scheme. */
        public val dark: TileSchema.TerrainVariantConfig,
        /** The rendition to draw under a light color scheme. */
        public val light: TileSchema.TerrainVariantConfig,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TerrainVariants &&
                other.dark == this.dark &&
                other.light == this.light

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.dark.hashCode()
            hash = 31 * hash + this.light.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TerrainVariants(" +
                "dark=" + this.dark +
                ", light=" + this.light +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TerrainVariants], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.TerrainVariants]. */
        public companion object {
            /** Returns a [TileSchema.TerrainVariants] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                dark: TileSchema.TerrainVariantConfig,
                light: TileSchema.TerrainVariantConfig,
                optional: TileSchema.TerrainVariants.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TerrainVariants {
                val given = TileSchema.TerrainVariants.Builder().apply(optional)
                return TileSchema.TerrainVariants(
                    dark = dark,
                    light = light,
                )
            }
        }
    }

    /**
     * A tile source described by a TileJSON document
     * (https://github.com/mapbox/tilejson-spec), ready to hand to a map renderer
     * unmodified: where its tiles are fetched from, its zoom range, its bounds and
     * its attribution. Its `tiles` templates point at this API, so a renderer that
     * registers the source reaches its tiles through this API alone, under the
     * credential it sends here, and no address in it carries a credential of
     * Mountaya's.
     *
     * The document is the TileJSON format rather than this contract's, so its
     * members are not declared here, and a member that format adds reaches a
     * client unchanged. It declares its own version in its `tilejson` member: the
     * version the base-map provider's document carries, which Mountaya does not
     * set. The elevation source's document also carries the `encoding` its pixels
     * are decoded with.
     */
    public typealias TileJson = kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>

    /**
     * A single render-layer to add with `map.addLayer()`. Carries the style type,
     * paint and layout objects, the source it draws from, the vector `source_layer`
     * to pull, the zoom bounds, and an optional legend.
     */
    @kotlinx.serialization.Serializable
    public class TileLayer private constructor(
        /**
         * Optional MapLibre/Mapbox filter expression restricting which features the
         * layer renders. Pass through to the `filter` option of `map.addLayer()`.
         * Omitted when no filter applies; never null and never an empty array.
         */
        public val filter: kotlin.collections.List<kotlinx.serialization.json.JsonElement>? = null,
        /**
         * Unique layer identifier. Use it as the first argument to `map.addLayer()`.
         *
         * A composite path rather than a UUID, for example
         * `itineraries/<itinerary_id>/lines`. Treat it as an opaque string: the
         * shape is a rendering detail and is free to change, and it is the only
         * kind of `id` on this surface that a UUID parser would reject.
         */
        public val id: kotlin.String,
        /**
         * Stable, human-readable concept slug grouping render-layers that depict the
         * same map concept (e.g. "parking", "hillshade"). Unlike `id`, which is the
         * per-layer unique identifier, `key` MAY repeat across several layers of the
         * same overlay when one concept is rendered with multiple layers (a fill
         * plus a symbol, for instance). Clients group and toggle layers by `key`;
         * `id` remains the uniqueness guarantee.
         *
         * The empty string is a real value, and means this layer belongs to no
         * named concept: an itinerary's own line layers carry it. Group by `key`
         * only after discarding the empty ones, or every unnamed layer collapses
         * into one group.
         */
        public val key: kotlin.String,
        /**
         * MapLibre/Mapbox layout properties for this layer (e.g. `visibility`,
         * symbol placement). Pass through to the `layout` option of
         * `map.addLayer()`. Null when the layer needs no layout overrides.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val layout: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>? = null,
        /**
         * Optional legend describing how to read the layer's colors or scale. Drives
         * the map UI legend; not consumed by `map.addLayer()`. Omitted when the
         * layer has no legend; never null.
         */
        public val legend: TileSchema.TileLegend? = null,
        /**
         * MapLibre/Mapbox paint properties for this layer (colors, opacity, widths,
         * data-driven expressions). Pass through to the `paint` option of
         * `map.addLayer()`. Null when the layer relies on style defaults.
         */
        @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)
        public val paint: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>? = null,
        /**
         * Identifier of the source this layer draws from. Matches the `id` of the
         * enclosing tile source.
         */
        public val source: kotlin.String? = null,
        /**
         * Name of the layer within a vector tile to render (e.g. "fills", "points",
         * "lines"). Pass through to the `source-layer` option of `map.addLayer()`.
         * Only meaningful for vector sources.
         */
        public val source_layer: kotlin.String,
        /**
         * How this layer draws, and what `paint` may carry: a map library maps it
         * onto its own layer type, so pass it through rather than inferring one from
         * the source.
         */
        public val type: TileSchema.TileLayerType,
        /**
         * Highest zoom level at which the layer is drawn. Pass to the `maxzoom`
         * option of `map.addLayer()`.
         */
        public val zoom_max: kotlin.Int,
        /**
         * Lowest zoom level at which the layer is drawn. Pass to the `minzoom`
         * option of `map.addLayer()`.
         */
        public val zoom_min: kotlin.Int,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TileLayer &&
                other.filter == this.filter &&
                other.id == this.id &&
                other.key == this.key &&
                other.layout == this.layout &&
                other.legend == this.legend &&
                other.paint == this.paint &&
                other.source == this.source &&
                other.source_layer == this.source_layer &&
                other.type == this.type &&
                other.zoom_max == this.zoom_max &&
                other.zoom_min == this.zoom_min

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.filter?.hashCode() ?: 0)
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.key.hashCode()
            hash = 31 * hash + (this.layout?.hashCode() ?: 0)
            hash = 31 * hash + (this.legend?.hashCode() ?: 0)
            hash = 31 * hash + (this.paint?.hashCode() ?: 0)
            hash = 31 * hash + (this.source?.hashCode() ?: 0)
            hash = 31 * hash + this.source_layer.hashCode()
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + this.zoom_max.hashCode()
            hash = 31 * hash + this.zoom_min.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TileLayer(" +
                "filter=" + this.filter +
                ", id=" + this.id +
                ", key=" + this.key +
                ", layout=" + this.layout +
                ", legend=" + this.legend +
                ", paint=" + this.paint +
                ", source=" + this.source +
                ", source_layer=" + this.source_layer +
                ", type=" + this.type +
                ", zoom_max=" + this.zoom_max +
                ", zoom_min=" + this.zoom_min +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TileLayer], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<kotlinx.serialization.json.JsonElement>? = null
            internal var slot4: TileSchema.TileLegend? = null
            internal var slot6: kotlin.String? = null

            /**
             * Optional MapLibre/Mapbox filter expression restricting which features the
             * layer renders. Pass through to the `filter` option of `map.addLayer()`.
             * Omitted when no filter applies; never null and never an empty array.
             */
            public var filter: kotlin.collections.List<kotlinx.serialization.json.JsonElement>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Optional legend describing how to read the layer's colors or scale. Drives
             * the map UI legend; not consumed by `map.addLayer()`. Omitted when the
             * layer has no legend; never null.
             */
            public var legend: TileSchema.TileLegend
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot4 = value
                }

            /**
             * Identifier of the source this layer draws from. Matches the `id` of the
             * enclosing tile source.
             */
            public var source: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot6 = value
                }
        }

        /** Builds a [TileSchema.TileLayer]. */
        public companion object {
            /** Returns a [TileSchema.TileLayer] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                key: kotlin.String,
                layout: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>?,
                paint: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>?,
                source_layer: kotlin.String,
                type: TileSchema.TileLayerType,
                zoom_max: kotlin.Int,
                zoom_min: kotlin.Int,
                optional: TileSchema.TileLayer.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TileLayer {
                val given = TileSchema.TileLayer.Builder().apply(optional)
                return TileSchema.TileLayer(
                    filter = given.slot0,
                    id = id,
                    key = key,
                    layout = layout,
                    legend = given.slot4,
                    paint = paint,
                    source = given.slot6,
                    source_layer = source_layer,
                    type = type,
                    zoom_max = zoom_max,
                    zoom_min = zoom_min,
                )
            }
        }
    }

    /**
     * MapLibre/Mapbox layer style type determining how features are drawn.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TileLayerType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Draw the source's tiles as a raster image. */
            public val Raster: TileLayerType = TileLayerType("raster")

            /** Shade the terrain from an elevation-encoded raster-dem source. */
            public val Hillshade: TileLayerType = TileLayerType("hillshade")

            /** Fill the interior of polygon features. */
            public val Fill: TileLayerType = TileLayerType("fill")

            /** Stroke line features, and the outlines of polygons. */
            public val Line: TileLayerType = TileLayerType("line")

            /** Draw a circle at each point feature. */
            public val Circle: TileLayerType = TileLayerType("circle")

            /** Draw an icon and/or text label at each feature. */
            public val Symbol: TileLayerType = TileLayerType("symbol")
        }
    }

    /**
     * Legend describing how to read a layer's symbology: the localized title, the
     * measurement unit, and the ordered color steps. Used to render a map legend UI;
     * not consumed by the map style itself.
     */
    @kotlinx.serialization.Serializable
    public class TileLegend private constructor(
        /**
         * How the legend is laid out: `row` draws the steps below as an ordered row
         * of color swatches, and `compass` as a compass dial, for a directional
         * overlay such as aspect/slope.
         */
        public val kind: TileSchema.TileLegendKind? = null,
        /**
         * The legend's title and step labels per language. A language absent here is
         * null rather than missing, which is the case to fall back from.
         */
        public val locales: TileSchema.Locales,
        /**
         * Ordered color stops mapping a value (or bucket) to the color the layer
         * paints for it, from low to high. Drives a graduated or categorical legend.
         */
        public val steps: kotlin.collections.List<TileSchema.TileLegendStep>? = null,
        /**
         * The unit the step values are measured in, for the suffix a legend prints
         * beside each one. Absent when the steps are categories rather than
         * quantities.
         */
        public val unit: TileSchema.UnitLegend? = null,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TileLegend &&
                other.kind == this.kind &&
                other.locales == this.locales &&
                other.steps == this.steps &&
                other.unit == this.unit

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.kind?.hashCode() ?: 0)
            hash = 31 * hash + this.locales.hashCode()
            hash = 31 * hash + (this.steps?.hashCode() ?: 0)
            hash = 31 * hash + (this.unit?.hashCode() ?: 0)
            return hash
        }

        override fun toString(): kotlin.String =
            "TileLegend(" +
                "kind=" + this.kind +
                ", locales=" + this.locales +
                ", steps=" + this.steps +
                ", unit=" + this.unit +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TileLegend], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: TileSchema.TileLegendKind? = null
            internal var slot2: kotlin.collections.List<TileSchema.TileLegendStep>? = null
            internal var slot3: TileSchema.UnitLegend? = null

            /**
             * How the legend is laid out: `row` draws the steps below as an ordered row
             * of color swatches, and `compass` as a compass dial, for a directional
             * overlay such as aspect/slope.
             */
            public var kind: TileSchema.TileLegendKind
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Ordered color stops mapping a value (or bucket) to the color the layer
             * paints for it, from low to high. Drives a graduated or categorical legend.
             */
            public var steps: kotlin.collections.List<TileSchema.TileLegendStep>
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot2 = value
                }

            /**
             * The unit the step values are measured in, for the suffix a legend prints
             * beside each one. Absent when the steps are categories rather than
             * quantities.
             */
            public var unit: TileSchema.UnitLegend
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [TileSchema.TileLegend]. */
        public companion object {
            /** Returns a [TileSchema.TileLegend] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                locales: TileSchema.Locales,
                optional: TileSchema.TileLegend.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TileLegend {
                val given = TileSchema.TileLegend.Builder().apply(optional)
                return TileSchema.TileLegend(
                    kind = given.slot0,
                    locales = locales,
                    steps = given.slot2,
                    unit = given.slot3,
                )
            }
        }
    }

    /**
     * Layout hint for rendering a tile legend: a horizontal/vertical row of color
     * steps, or a compass dial (used for directional overlays such as
     * aspect/slope).
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TileLegendKind(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Render the legend as an ordered row of color steps. */
            public val Row: TileLegendKind = TileLegendKind("row")

            /** Render the legend as a compass dial for directional (bearing) data. */
            public val Compass: TileLegendKind = TileLegendKind("compass")
        }
    }

    /** A single legend entry pairing a color with the value or bucket it represents. */
    @kotlinx.serialization.Serializable
    public class TileLegendStep private constructor(
        /**
         * CSS color the layer paints for this step (e.g. "#1c7ed6"). Matches the
         * color used in the layer's paint expression.
         *
         * Case-insensitive, unlike the colors on an itinerary's segments, waypoints
         * and segment groups. Those are values a caller wrote, normalized to the
         * six-digit lowercase form on the
         * way in; this one is read from the layer's own palette and served in
         * whatever case that palette is written in. Compare these case-insensitively
         * rather than by string equality.
         */
        public val color: kotlin.String,
        /**
         * Label for the value or bucket this color represents, such as a threshold
         * like "20" or a range like "25 - 30". It is the same in every language:
         * the legend's translated text is in its `locales`.
         */
        public val step: kotlin.String,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TileLegendStep &&
                other.color == this.color &&
                other.step == this.step

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + this.color.hashCode()
            hash = 31 * hash + this.step.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TileLegendStep(" +
                "color=" + this.color +
                ", step=" + this.step +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TileLegendStep], each by assigning it. */
        public class Builder internal constructor() {
        }

        /** Builds a [TileSchema.TileLegendStep]. */
        public companion object {
            /** Returns a [TileSchema.TileLegendStep] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                color: kotlin.String,
                step: kotlin.String,
                optional: TileSchema.TileLegendStep.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TileLegendStep {
                val given = TileSchema.TileLegendStep.Builder().apply(optional)
                return TileSchema.TileLegendStep(
                    color = color,
                    step = step,
                )
            }
        }
    }

    /**
     * A single map source to register with `map.addSource()`. Bundles the
     * `{z}/{x}/{y}` tile URL template, the source kind, its zoom bounds, and the
     * render-layers that draw from it.
     */
    @kotlinx.serialization.Serializable
    public class TileSource private constructor(
        /**
         * Attribution HTML to display on the map for this source: the credit of
         * whoever supplies its data, Mountaya for a resource source and the data
         * provider for an overlay. Pass through to the `attribution` option of
         * `map.addSource()`.
         */
        public val attribution: kotlin.String? = null,
        /**
         * Unique source identifier. Use it as the first argument to
         * `map.addSource()` and as the `source` of every layer that draws from it.
         *
         * Declared as a string rather than a UUID so that it stays opaque: a
         * resource source is named by the resource's id, and an overlay or
         * aggregate source by a fixed identifier of its own. Every one of them is
         * spelled as a UUID, but that shape is not part of this contract, so treat
         * it as an opaque string and match it against a layer's `source` rather
         * than parsing it.
         */
        public val id: kotlin.String,
        /**
         * Render-layers to draw from this source, in paint order. Each becomes one
         * `map.addLayer()` call.
         */
        public val layers: kotlin.collections.List<TileSchema.TileLayer>,
        /**
         * Edge length in pixels of one raster tile, for `map.addSource()`'s
         * `tileSize`. Omitted for vector sources, which have no pixel size of their
         * own.
         */
        public val size: TileSchema.TileSourceSize? = null,
        /**
         * Tile URL templates with `{z}`, `{x}`, and `{y}` placeholders that the map
         * library expands per tile. Usually a single entry pointing at the matching
         * `{z}/{x}/{y}` endpoint. Hand the template to the map library with its
         * placeholders intact: the library substitutes them as it pans and zooms, so
         * a template expanded before it is handed over produces one tile rather
         * than a map. An overlay's template, and in a layer catalog the itinerary
         * and zone layers' templates, carry the `organization_id` the
         * specification or the catalog was asked for with, when one was sent, as a
         * query string of its own, so a credential added to such a template joins
         * it with `&` rather than starting a second `?`.
         */
        public val tiles: kotlin.collections.List<kotlin.String>,
        /**
         * What kind of data the source serves, which decides how a map library reads
         * its tiles. A `raster-dem` source carries elevation rather than pixels, so
         * it feeds terrain and hillshade rather than being drawn directly.
         */
        public val type: TileSchema.TileSourceType,
        /**
         * Deepest zoom a map library requests this source's tiles at. A layer drawn
         * from the source can reach deeper, and a map overzooms the source's tiles
         * for it. It says where to ask rather than what is refused: a tile asked for
         * deeper is still served, and on a specification served to a publishable
         * key the value is brought within the organization's zoom bounds.
         */
        public val zoom_max: kotlin.Int,
        /**
         * Shallowest zoom a map library requests this source's tiles at. Like
         * `zoom_max`, it says where to ask rather than what is refused, and on a
         * specification served to a publishable key it is brought within the
         * organization's zoom bounds.
         */
        public val zoom_min: kotlin.Int,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TileSource &&
                other.attribution == this.attribution &&
                other.id == this.id &&
                other.layers == this.layers &&
                other.size == this.size &&
                other.tiles == this.tiles &&
                other.type == this.type &&
                other.zoom_max == this.zoom_max &&
                other.zoom_min == this.zoom_min

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.attribution?.hashCode() ?: 0)
            hash = 31 * hash + this.id.hashCode()
            hash = 31 * hash + this.layers.hashCode()
            hash = 31 * hash + (this.size?.hashCode() ?: 0)
            hash = 31 * hash + this.tiles.hashCode()
            hash = 31 * hash + this.type.hashCode()
            hash = 31 * hash + this.zoom_max.hashCode()
            hash = 31 * hash + this.zoom_min.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TileSource(" +
                "attribution=" + this.attribution +
                ", id=" + this.id +
                ", layers=" + this.layers +
                ", size=" + this.size +
                ", tiles=" + this.tiles +
                ", type=" + this.type +
                ", zoom_max=" + this.zoom_max +
                ", zoom_min=" + this.zoom_min +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TileSource], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.String? = null
            internal var slot3: TileSchema.TileSourceSize? = null

            /**
             * Attribution HTML to display on the map for this source: the credit of
             * whoever supplies its data, Mountaya for a resource source and the data
             * provider for an overlay. Pass through to the `attribution` option of
             * `map.addSource()`.
             */
            public var attribution: kotlin.String
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }

            /**
             * Edge length in pixels of one raster tile, for `map.addSource()`'s
             * `tileSize`. Omitted for vector sources, which have no pixel size of their
             * own.
             */
            public var size: TileSchema.TileSourceSize
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot3 = value
                }
        }

        /** Builds a [TileSchema.TileSource]. */
        public companion object {
            /** Returns a [TileSchema.TileSource] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                id: kotlin.String,
                layers: kotlin.collections.List<TileSchema.TileLayer>,
                tiles: kotlin.collections.List<kotlin.String>,
                type: TileSchema.TileSourceType,
                zoom_max: kotlin.Int,
                zoom_min: kotlin.Int,
                optional: TileSchema.TileSource.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TileSource {
                val given = TileSchema.TileSource.Builder().apply(optional)
                return TileSchema.TileSource(
                    attribution = given.slot0,
                    id = id,
                    layers = layers,
                    size = given.slot3,
                    tiles = tiles,
                    type = type,
                    zoom_max = zoom_max,
                    zoom_min = zoom_min,
                )
            }
        }
    }

    /**
     * Edge length in pixels of one raster tile. Pass it to the `tileSize` option of
     * `map.addSource()`. Omitted for vector sources, which have no pixel size of
     * their own.
     *
     * These two values are the only ones a source can carry, and the raster
     * overlay sources all carry 512, so a client may switch on them exhaustively
     * rather than treating this as an open integer. Declaring a size
     * the source does not serve offsets every zoom level rather than failing, which
     * is why the set is closed: read it off the source instead of assuming one.
     *
     * The terrain sources publish 512 alone, which `TerrainTileSize` declares on its
     * own for the variant that carries nothing else.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TileSourceSize(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.Int,
    ) {
        override fun toString(): kotlin.String = value.toString()

        /** The values the contract names. */
        public companion object {
            /** A 256 by 256 pixel tile, the classic web-map size. */
            public val Size256: TileSourceSize = TileSourceSize(256)

            /** A 512 by 512 pixel tile, covering the same ground as four 256px tiles at one zoom deeper. */
            public val Size512: TileSourceSize = TileSourceSize(512)
        }
    }

    /**
     * Source kind. `vector` for Mapbox Vector Tile sources (itineraries, zones, and
     * the weather, snow and POI overlays), `raster` for image overlays (the
     * terrain analyses, such as slope steepness and aspect/slope), and
     * `raster-dem` for elevation-encoded sources that drive hillshade and terrain.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class TileSourceType(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Mapbox Vector Tile source, carrying itinerary, zone, weather and POI geometry. */
            public val Vector: TileSourceType = TileSourceType("vector")

            /** Raster image source, used for terrain analysis overlays such as slope steepness. */
            public val Raster: TileSourceType = TileSourceType("raster")

            /** Elevation-encoded raster source driving hillshade and 3D terrain. */
            public val RasterDem: TileSourceType = TileSourceType("raster-dem")
        }
    }

    /**
     * Rendering specification for a tile resource. Returned by every
     * `/specification` endpoint and consumed once while a map is initializing:
     * iterate `sources` and call `map.addSource()` for each, then iterate the
     * `layers` inside each source and call `map.addLayer()` for each. The map
     * library then fetches the `{z}/{x}/{y}` tiles automatically. A collection's
     * specification also carries `bbox`, the extent of the itineraries its tiles
     * draw, which is where to fit the camera once the sources are registered.
     *
     * Served to a publishable key, every `zoom_min` and `zoom_max` in it is already
     * narrowed to the organization's zoom-bounds safety rule, so a map that follows
     * it asks only for tiles that rule serves. A layer's range is intersected with
     * the bounds, and a layer whose range lies wholly outside them is left out, as
     * is a source left with no layer. A source's own range is clamped into the
     * bounds rather than intersected, so a source whose tiles stop below the
     * bounds still reports a range inside them, and the tiles past its own ceiling
     * are cut from their ancestors.
     */
    @kotlinx.serialization.Serializable
    public class TileSpecification private constructor(
        /**
         * The extent of the itineraries this specification's tiles draw, as
         * [west, south, east, north] in WGS 84 decimal degrees. This is the flat
         * four-tuple every bounding box on every Mountaya surface uses. Exactly four
         * numbers, longitude first in each pair, every longitude between -180 and
         * 180.
         *
         * Of the two boxes the longitudes allow, read as written and read across
         * the antimeridian, it is the narrower, the one as written when they are
         * as wide. One across the antimeridian has west greater than east, as RFC
         * 7946 section 5.2 writes such a box: itineraries near 179.8 and -179.9
         * degrees answer `[179.8, s, -179.9, n]`, a box 0.3 degrees wide, rather
         * than the 359.7 degrees the same longitudes span as written. Any other
         * box this answers has west at or below east, the minimum corner first. A
         * map library that takes a box only with west below east, MapLibre's
         * `fitBounds()` among them, is handed east plus 360 for one that crosses:
         * `[179.8, s, 180.1, n]`.
         *
         * Fit the map's camera to it once the sources are registered, with
         * MapLibre's `fitBounds()` for instance, so the map opens on every
         * itinerary the tiles draw rather than on the `center` and `zoom` it was
         * created with. When it is null there is nothing to fit to, and the map
         * keeps its own camera.
         *
         * Carried by a collection's specification and by no other: every other
         * specification leaves it out. On a collection's it is always present. It
         * spans every line and point of the collection's itineraries that its
         * tiles draw for the same credential, the same `surface` and the same
         * resolved `with_itineraries`, each as it is drawn: under a publishable
         * key alone, and under a key with a session token minted without
         * `user_id`, the public ones of the key's organization, as last
         * published; under a key with a session token naming a member, every one
         * the key's organization owns, as its draft, or as last published when
         * `surface=published` asks for the published snapshots; under a
         * collection share token, every one the collection holds, as last
         * published. Zones never count toward it, drawn or not. It is not clipped
         * to the organization's geographic boundaries, so under a publishable key
         * it can reach past them, where a tile is refused.
         *
         * Null when none of those itineraries is drawn: the collection holds none
         * this credential is served, none it is served has a geometry yet, or
         * `with_itineraries` resolves to off, which on a publishable-key request
         * includes the organization's safety rule keeping collection itineraries
         * off. Under a key, another organization's collection draws none, so its
         * box is null.
         */
        public val bbox: kotlin.collections.List<kotlin.Double>? = null,
        /**
         * Map sources to register, in order. Each becomes one `map.addSource()` call
         * and carries the render-layers to draw from it.
         */
        public val sources: kotlin.collections.List<TileSchema.TileSource>,
    ) {
        override fun equals(other: kotlin.Any?): kotlin.Boolean =
            other is TileSchema.TileSpecification &&
                other.bbox == this.bbox &&
                other.sources == this.sources

        override fun hashCode(): kotlin.Int {
            var hash = 0
            hash = 31 * hash + (this.bbox?.hashCode() ?: 0)
            hash = 31 * hash + this.sources.hashCode()
            return hash
        }

        override fun toString(): kotlin.String =
            "TileSpecification(" +
                "bbox=" + this.bbox +
                ", sources=" + this.sources +
                ")"

        /** Builder sets the optional properties of a [TileSchema.TileSpecification], each by assigning it. */
        public class Builder internal constructor() {
            internal var slot0: kotlin.collections.List<kotlin.Double>? = null

            /**
             * The extent of the itineraries this specification's tiles draw, as
             * [west, south, east, north] in WGS 84 decimal degrees. This is the flat
             * four-tuple every bounding box on every Mountaya surface uses. Exactly four
             * numbers, longitude first in each pair, every longitude between -180 and
             * 180.
             *
             * Of the two boxes the longitudes allow, read as written and read across
             * the antimeridian, it is the narrower, the one as written when they are
             * as wide. One across the antimeridian has west greater than east, as RFC
             * 7946 section 5.2 writes such a box: itineraries near 179.8 and -179.9
             * degrees answer `[179.8, s, -179.9, n]`, a box 0.3 degrees wide, rather
             * than the 359.7 degrees the same longitudes span as written. Any other
             * box this answers has west at or below east, the minimum corner first. A
             * map library that takes a box only with west below east, MapLibre's
             * `fitBounds()` among them, is handed east plus 360 for one that crosses:
             * `[179.8, s, 180.1, n]`.
             *
             * Fit the map's camera to it once the sources are registered, with
             * MapLibre's `fitBounds()` for instance, so the map opens on every
             * itinerary the tiles draw rather than on the `center` and `zoom` it was
             * created with. When it is null there is nothing to fit to, and the map
             * keeps its own camera.
             *
             * Carried by a collection's specification and by no other: every other
             * specification leaves it out. On a collection's it is always present. It
             * spans every line and point of the collection's itineraries that its
             * tiles draw for the same credential, the same `surface` and the same
             * resolved `with_itineraries`, each as it is drawn: under a publishable
             * key alone, and under a key with a session token minted without
             * `user_id`, the public ones of the key's organization, as last
             * published; under a key with a session token naming a member, every one
             * the key's organization owns, as its draft, or as last published when
             * `surface=published` asks for the published snapshots; under a
             * collection share token, every one the collection holds, as last
             * published. Zones never count toward it, drawn or not. It is not clipped
             * to the organization's geographic boundaries, so under a publishable key
             * it can reach past them, where a tile is refused.
             *
             * Null when none of those itineraries is drawn: the collection holds none
             * this credential is served, none it is served has a geometry yet, or
             * `with_itineraries` resolves to off, which on a publishable-key request
             * includes the organization's safety rule keeping collection itineraries
             * off. Under a key, another organization's collection draws none, so its
             * box is null.
             */
            public var bbox: kotlin.collections.List<kotlin.Double>?
                @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
                get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
                set(value) {
                    slot0 = value
                }
        }

        /** Builds a [TileSchema.TileSpecification]. */
        public companion object {
            /** Returns a [TileSchema.TileSpecification] of the required properties given here and the optional ones [optional] sets. */
            public operator fun invoke(
                sources: kotlin.collections.List<TileSchema.TileSource>,
                optional: TileSchema.TileSpecification.Builder.() -> kotlin.Unit = {},
            ): TileSchema.TileSpecification {
                val given = TileSchema.TileSpecification.Builder().apply(optional)
                return TileSchema.TileSpecification(
                    bbox = given.slot0,
                    sources = sources,
                )
            }
        }
    }

    /**
     * Measurement unit a legend's values are expressed in. Determines the unit
     * suffix shown next to each legend step.
     *
     * It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.
     */
    @kotlinx.serialization.Serializable
    @kotlin.jvm.JvmInline
    public value class UnitLegend(
        /** The value, exactly as the wire carries it. */
        public val value: kotlin.String,
    ) {
        override fun toString(): kotlin.String = value

        /** The values the contract names. */
        public companion object {
            /** Speed in kilometers per hour (e.g. wind speed). */
            public val Kmh: UnitLegend = UnitLegend("km/h")

            /** Angle in degrees (e.g. slope or aspect). */
            public val Degrees: UnitLegend = UnitLegend("degrees")

            /** Length, depth or elevation in meters (e.g. snow depth). */
            public val M: UnitLegend = UnitLegend("m")

            /** Length or depth in centimeters (e.g. snowfall). */
            public val Cm: UnitLegend = UnitLegend("cm")

            /** Temperature in degrees Celsius. */
            public val Celsius: UnitLegend = UnitLegend("celsius")

            /** Ratio expressed as a percentage. */
            public val Percent: UnitLegend = UnitLegend("percent")
        }
    }

    /** GetCollectionSpecificationParameters sets the optional parameters of `getCollectionSpecification`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetCollectionSpecificationParameters internal constructor() {
        internal var slot0: TileSchema.ReadSurface? = null
        internal var slot1: kotlin.Boolean? = null
        internal var slot2: kotlin.Boolean? = null

        /**
         * Reads the published snapshot rather than the draft. `published` is the only
         * accepted value: there is deliberately no `draft` spelling, because the param
         * only ever narrows what the caller is served. An unrecognized value is rejected
         * with a validation error rather than silently ignored. A read of one itinerary,
         * or of one zone, refuses it with `403` when it is personal, owned by no
         * organization, to the person whose it is: the published surface is what a
         * video and an embed are made of, and a personal itinerary or zone makes
         * neither. Every other caller reads a personal itinerary or zone as published,
         * and since one is never published it is answered as one that does not exist.
         */
        public var surface: TileSchema.ReadSurface
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }

        /**
         * Whether the collection's zones are part of the answer. On this route that
         * is the zone layers the specification declares.
         *
         * It declares no `default`, and that is deliberate rather than an omission:
         * the value when the parameter is absent is the organization's own default for
         * this layer, clamped to its safety rules for a publishable-key caller, so
         * there is no one figure this document could publish. For a request from
         * Mountaya's web application, which names no organization of its own, that
         * organization is Mountaya's own, and its default is not clamped. Sending it
         * explicitly is the way to decide it per request, within the rule: on a
         * publishable-key request an explicit `true` is still capped by the
         * organization's safety rule.
         */
        public var with_zones: kotlin.Boolean
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot1 = value
            }

        /**
         * Whether the collection's itineraries are part of the answer. On this route
         * that is the itinerary layers the specification declares, `itinerary_lines`
         * and `itinerary_points`.
         *
         * It declares no `default`, and that is deliberate rather than an omission:
         * the value when the parameter is absent is the organization's own default for
         * this layer, clamped to its safety rules for a publishable-key caller, so
         * there is no one figure this document could publish. For a request from
         * Mountaya's web application, which names no organization of its own, that
         * organization is Mountaya's own, and its default is not clamped. Sending it
         * explicitly is the way to decide it per request, within the rule: on a
         * publishable-key request an explicit `true` is still capped by the
         * organization's safety rule.
         */
        public var with_itineraries: kotlin.Boolean
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot2 = value
            }
    }

    /** GetItinerariesSpecificationParameters sets the optional parameters of `getItinerariesSpecification`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetItinerariesSpecificationParameters internal constructor() {
    }

    /** GetItinerarySpecificationParameters sets the optional parameters of `getItinerarySpecification`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetItinerarySpecificationParameters internal constructor() {
    }

    /** GetOverlaySpecificationParameters sets the optional parameters of `getOverlaySpecification`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetOverlaySpecificationParameters internal constructor() {
        internal var slot0: kotlin.String? = null

        /**
         * The studio scope the map draws in, named by the organization's identifier. A
         * value that is not a UUID in its canonical form, or the nil UUID, is a `400`
         * naming `organization_id`.
         *
         * Under a publishable key or a share token it may name only the credential's
         * own organization: any other identifier is a `403`, whether or not it exists.
         * It changes nothing but being echoed, so a request sending it is answered as
         * one that omits it, and every URL template an answer returns under
         * `sources[].tiles` for a layer that takes it carries the same
         * `organization_id`.
         *
         * A request from Mountaya's web application, which carries the person's own
         * session rather than a key or a share token, uses it to choose the scope it
         * draws in. Omitted, the scope is the person's own space, which has no
         * identifier of its own to send. Naming an organization the person is a member
         * of makes that organization the scope. Any other identifier, an organization
         * the person is not a member of or one that does not exist, is a `404`, so the
         * parameter cannot be used to learn which organizations exist.
         *
         * What a scope draws depends on the layer. The overlays served under a key or a
         * share token are the ones the organization's overlay allowlist admits. From
         * Mountaya's web application, the person's own space draws the points of
         * interest alone, and an organization draws every overlay when it holds Map
         * Studio and the points of interest alone when it does not, an overlay outside
         * the scope being a `403`. The itinerary and zone tiles draw what the scope
         * holds: in the person's own space, their own itineraries and zones, which
         * belong to no organization, and in an organization, its itineraries and zones,
         * never those of another space the person belongs to.
         */
        public var organization_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }
    }

    /** GetTileLayersParameters sets the optional parameters of `getTileLayers`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetTileLayersParameters internal constructor() {
        internal var slot0: kotlin.String? = null

        /**
         * The studio scope the map draws in, named by the organization's identifier. A
         * value that is not a UUID in its canonical form, or the nil UUID, is a `400`
         * naming `organization_id`.
         *
         * Under a publishable key or a share token it may name only the credential's
         * own organization: any other identifier is a `403`, whether or not it exists.
         * It changes nothing but being echoed, so a request sending it is answered as
         * one that omits it, and every URL template an answer returns under
         * `sources[].tiles` for a layer that takes it carries the same
         * `organization_id`.
         *
         * A request from Mountaya's web application, which carries the person's own
         * session rather than a key or a share token, uses it to choose the scope it
         * draws in. Omitted, the scope is the person's own space, which has no
         * identifier of its own to send. Naming an organization the person is a member
         * of makes that organization the scope. Any other identifier, an organization
         * the person is not a member of or one that does not exist, is a `404`, so the
         * parameter cannot be used to learn which organizations exist.
         *
         * What a scope draws depends on the layer. The overlays served under a key or a
         * share token are the ones the organization's overlay allowlist admits. From
         * Mountaya's web application, the person's own space draws the points of
         * interest alone, and an organization draws every overlay when it holds Map
         * Studio and the points of interest alone when it does not, an overlay outside
         * the scope being a `403`. The itinerary and zone tiles draw what the scope
         * holds: in the person's own space, their own itineraries and zones, which
         * belong to no organization, and in an organization, its itineraries and zones,
         * never those of another space the person belongs to.
         */
        public var organization_id: kotlin.String
            @kotlin.Deprecated("A builder is written, never read back.", level = kotlin.DeprecationLevel.HIDDEN)
            get() = throw kotlin.UnsupportedOperationException("A builder is written, never read back.")
            set(value) {
                slot0 = value
            }
    }

    /** GetZonesSpecificationParameters sets the optional parameters of `getZonesSpecification`, each by assigning it. Every operation takes one, even with nothing in it, so a parameter the contract adds changes no call site. */
    public class GetZonesSpecificationParameters internal constructor() {
    }
}
