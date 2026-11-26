package com.mountaya.elements

/**
 * MapException is a command a map engine refuses: because what it names is not there, a layer, a marker or
 * a result no command added, or a terrain or an overlay the catalogue does not list; because what it is
 * handed is no value a map takes, a position that names no place, a number that is not finite, a pan lock
 * that encloses no area, a zoom range a camera cannot take or a source of a type the contract does not
 * declare; because it names something of the other kind, or an edit has no input to ask again with; because
 * the device answered no fix to locate; or because the map is closed. The map is left as it was.
 *
 * Each kind is a refusal the contract names for the map engines, kebab-case there (`position-refused`,
 * `number-refused`, `window-refused` for `ZoomBoundsRefused`, `fix-refused`, `closed`, `type-refused`,
 * `kind-mismatch`, `no-input`, and `not-found` for every kind named so). A query or a read the map gave up
 * on is no refusal: the call throws the platform's `CancellationException`, the contract's `cancelled`. Its
 * message says what to do, for a developer reading a log; a caller reads [kind], [name] and, for a fix
 * refused, [reason].
 */
public class MapException(
    /** Which refusal it is. */
    public val kind: Kind,
    /**
     * What the command named: the layer's or the marker's id, the terrain's slug, the id of the marker or the
     * waypoint placed nowhere, the pan lock or the zoom range as written, the member of a camera move or a fit
     * refused, or the call a closed map refused.
     */
    public val name: String,
    /** The reason the device's geolocation named for answering no fix, for a refusal of kind `FixRefused`, and null for every other. */
    public val reason: GeolocationRefusal? = null,
) : Exception(messageOf(kind, name, reason)) {
    /**
     * Kind is every reason a map command is refused that this build knows.
     *
     * It is open, so a `when` over it ends in `else`: the set grows where the engine adds a reason.
     */
    @kotlin.jvm.JvmInline
    public value class Kind internal constructor(
        /** The reason, as a log names it. */
        public val value: String,
    ) {
        override fun toString(): String = value

        /** The reasons this build knows. */
        public companion object {
            /** A command naming a layer the map does not draw. */
            public val LayerNotFound: Kind = Kind("layerNotFound")

            /** A base map naming a terrain the Tile API's catalogue does not list. */
            public val TerrainNotFound: Kind = Kind("terrainNotFound")

            /** A command naming a marker the map does not show. */
            public val MarkerNotFound: Kind = Kind("markerNotFound")

            /**
             * A marker, a waypoint or a camera placed at a position that names no place a map draws: one that is
             * not two finite numbers, its latitude within [-90, 90], or a box to frame that is not four finite
             * numbers.
             */
            public val PositionRefused: Kind = Kind("positionRefused")

            /**
             * A pan lock that encloses no area once it is read as a lock: its west and east edges on one
             * meridian, its south and north edges on one parallel, or an edge at no finite coordinate
             * (`geo.camera.max-bounds-refusal`).
             */
            public val MaxBoundsRefused: Kind = Kind("maxBoundsRefused")

            /**
             * A zoom range a camera cannot be held to: a least zoom under the renderer's floor, or a least zoom
             * above the greatest, the one stated beside it or the one the map holds where the call states one end
             * alone (`map.camera.zoom-window`).
             */
            public val ZoomBoundsRefused: Kind = Kind("zoomBoundsRefused")

            /**
             * A number a camera cannot take: a zoom, a bearing, a pitch, a zoom limit or a fit's ceiling that is not
             * a finite number, or a fit's padding or duration that is not one from 0 up (`map.camera.pose`,
             * `map.camera.fit-options`, `map.camera.zoom-window`).
             */
            public val NumberRefused: Kind = Kind("numberRefused")

            /**
             * A locate the device's geolocation answered with no fix, for the [reason] it named; nothing moved
             * (`map.camera.locate`).
             */
            public val FixRefused: Kind = Kind("fixRefused")

            /**
             * A locate asked of a closed map, which asks the device nothing, since a fix may prompt its reader and
             * nothing would follow the prompt, or a layers catalogue asked of one that holds none.
             */
            public val Closed: Kind = Kind("closed")

            /**
             * A tile specification holding a source of a type the contract does not declare, which a newer Tile
             * API may add; nothing of the specification is drawn (`map.specification.style`).
             */
            public val TypeRefused: Kind = Kind("typeRefused")

            /** An overlay slug the layers catalogue holds under neither kind (`map.overlays.composition`). */
            public val OverlayNotFound: Kind = Kind("overlayNotFound")

            /**
             * A slug or an id that names something of the other kind: an enrichment's slug drawn as the classic
             * overlay or the other way round (`map.overlays.composition`), or an edit of a result of another kind
             * than the one it names (`map.analysis.edit`).
             */
            public val KindMismatch: Kind = Kind("kindMismatch")

            /** An edit of a result id nothing is drawn under (`map.analysis.edit`). */
            public val AnalysisNotFound: Kind = Kind("analysisNotFound")

            /**
             * An edit that states a member to ask again with, of a result drawn from an answer handed over rather
             * than asked for, which holds no input to merge it onto (`map.analysis.edit`).
             */
            public val NoInput: Kind = Kind("noInput")
        }
    }

    /** Answers whether [other] is a refusal of the same kind, naming the same and giving the same reason. */
    override fun equals(other: Any?): Boolean =
        other is MapException && other.kind == kind && other.name == name && other.reason == reason

    override fun hashCode(): Int = (kind.hashCode() * 31 + name.hashCode()) * 31 + (reason?.hashCode() ?: 0)

    override fun toString(): String =
        if (reason ==
            null
        ) {
            "MapException($kind, $name)"
        } else {
            "MapException($kind, $name, $reason)"
        }

    private companion object {
        fun messageOf(
            kind: Kind,
            name: String,
            reason: GeolocationRefusal?,
        ): String =
            when (kind) {
                Kind.TerrainNotFound -> {
                    "MapEngine: the Tile API's catalogue lists no terrain \"$name\"; pass a slug its " +
                        "`getTileLayers` lists, or none for its first."
                }

                Kind.MarkerNotFound -> {
                    "MapEngine: no marker is shown under \"$name\". It was never added, or it was removed."
                }

                Kind.PositionRefused -> {
                    "MapEngine: \"$name\" is placed at a position that names no place a map draws; pass " +
                        "[longitude, latitude] as two finite numbers, the latitude from -90 to 90."
                }

                Kind.MaxBoundsRefused -> {
                    "MapEngine: the pan lock $name encloses no area, held within the latitudes a map draws; " +
                        "give it two finite longitudes that differ and two finite latitudes that differ, or " +
                        "null to lift the lock."
                }

                Kind.ZoomBoundsRefused -> {
                    "MapEngine: the zoom range $name is no range a camera takes; pass a least zoom from the " +
                        "renderer's floor up, at or under the greatest zoom, or release a limit to the renderer's own."
                }

                Kind.NumberRefused -> {
                    "MapEngine: $name is no number a camera takes; pass a finite number, from 0 up for a " +
                        "padding or a duration, or leave it out."
                }

                Kind.FixRefused -> {
                    "MapEngine: the device answered no fix to locate (${reason?.id ?: "no reason"}); nothing " +
                        "moved. A fix the app holds no permission for is denied: ask the reader in the app's own flow."
                }

                Kind.Closed -> {
                    "MapEngine: $name was asked of a closed map, which asks the device nothing."
                }

                Kind.TypeRefused -> {
                    "MapEngine: a specification holds a source of type \"$name\", which this build does not " +
                        "draw; it draws a \"vector\", a \"raster\" or a \"raster-dem\" source. Nothing of the " +
                        "specification was drawn."
                }

                Kind.OverlayNotFound -> {
                    "MapEngine: the Tile API's catalogue lists no overlay \"$name\"; pass a slug its " +
                        "`getTileLayers` lists."
                }

                Kind.KindMismatch -> {
                    "MapEngine: \"$name\" names something of the other kind: an enrichment is added with " +
                        "`addEnrichment` and a classic overlay drawn with `setOverlay`, and a result is edited " +
                        "through the edit of its own kind."
                }

                Kind.AnalysisNotFound -> {
                    "MapEngine: no result is drawn under \"$name\". It was never added, or it was removed."
                }

                Kind.NoInput -> {
                    "MapEngine: \"$name\" was drawn from an answer handed over, which holds no input to ask " +
                        "again with; edit its draw options alone, or add it again from an input."
                }

                else -> {
                    "MapEngine: no layer is drawn under \"$name\". It was never added, or it was removed."
                }
            }
    }
}
