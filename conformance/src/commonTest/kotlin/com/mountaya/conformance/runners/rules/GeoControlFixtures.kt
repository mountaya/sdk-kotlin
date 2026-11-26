package com.mountaya.conformance

import com.mountaya.client.Url
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.ENRICHMENT_CATEGORY_SLUG
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.HexColor
import com.mountaya.geo.LayersSelection
import com.mountaya.geo.LayersSelectionTarget
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.ThemeToken
import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.themeTokenValue
import com.mountaya.geo.wordOverridesOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.time.Instant

/**
 * GeoControlFixtures reads what the family 8d fixtures write, the controls' and the panels', into the values Geo
 * takes, and writes Geo's answers back in the fixtures' shapes.
 */
internal object GeoControlFixtures {
    // Tokens.

    /**
     * RunnerToken is one token of the contract's table as a consumer reaches it: [read] off `ElementTokens`, and
     * [write] on the builder a copy of `ElementTokens.standard` hands it, the same member both.
     */
    class RunnerToken<Value>(
        val read: KProperty1<ElementTokens, ThemeToken<Value>>,
        val write: KMutableProperty1<ElementTokens.Builder, ThemeToken<Value>>,
    )

    /**
     * Each colour token's member, by the name the contract's table gives it, as a consumer reaches it. The jvm suite
     * holds the names to the contract's table and each entry to the member the table's value reads under.
     */
    val colorTokens: Map<String, RunnerToken<HexColor>> =
        mapOf(
            "accent" to RunnerToken(ElementTokens::accent, ElementTokens.Builder::accent),
            "control-bg" to RunnerToken(ElementTokens::controlBg, ElementTokens.Builder::controlBg),
            "control-bg-hover" to RunnerToken(ElementTokens::controlBgHover, ElementTokens.Builder::controlBgHover),
            "control-fg" to RunnerToken(ElementTokens::controlFg, ElementTokens.Builder::controlFg),
            "control-fg-disabled" to
                RunnerToken(ElementTokens::controlFgDisabled, ElementTokens.Builder::controlFgDisabled),
            "dimmed" to RunnerToken(ElementTokens::dimmed, ElementTokens.Builder::dimmed),
            "focus-ring" to RunnerToken(ElementTokens::focusRing, ElementTokens.Builder::focusRing),
            "panel-bg" to RunnerToken(ElementTokens::panelBg, ElementTokens.Builder::panelBg),
            "panel-border" to RunnerToken(ElementTokens::panelBorder, ElementTokens.Builder::panelBorder),
            "panel-fg" to RunnerToken(ElementTokens::panelFg, ElementTokens.Builder::panelFg),
            "profile-cursor" to RunnerToken(ElementTokens::profileCursor, ElementTokens.Builder::profileCursor),
            "profile-grid-color" to
                RunnerToken(ElementTokens::profileGridColor, ElementTokens.Builder::profileGridColor),
            "profile-lit-fill" to RunnerToken(ElementTokens::profileLitFill, ElementTokens.Builder::profileLitFill),
            "profile-navigator-handle" to
                RunnerToken(ElementTokens::profileNavigatorHandle, ElementTokens.Builder::profileNavigatorHandle),
            "profile-navigator-veil" to
                RunnerToken(ElementTokens::profileNavigatorVeil, ElementTokens.Builder::profileNavigatorVeil),
            "profile-tooltip-bg" to
                RunnerToken(ElementTokens::profileTooltipBg, ElementTokens.Builder::profileTooltipBg),
            "profile-tooltip-fg" to
                RunnerToken(ElementTokens::profileTooltipFg, ElementTokens.Builder::profileTooltipFg),
            "profile-waypoint-color" to
                RunnerToken(ElementTokens::profileWaypointColor, ElementTokens.Builder::profileWaypointColor),
            "profile-waypoint-line" to
                RunnerToken(ElementTokens::profileWaypointLine, ElementTokens.Builder::profileWaypointLine),
            "table-border" to RunnerToken(ElementTokens::tableBorder, ElementTokens.Builder::tableBorder),
            "table-row-hover" to RunnerToken(ElementTokens::tableRowHover, ElementTokens.Builder::tableRowHover),
            "table-spark-base" to RunnerToken(ElementTokens::tableSparkBase, ElementTokens.Builder::tableSparkBase),
            "tooltip-bg" to RunnerToken(ElementTokens::tooltipBg, ElementTokens.Builder::tooltipBg),
            "tooltip-fg" to RunnerToken(ElementTokens::tooltipFg, ElementTokens.Builder::tooltipFg),
            "waypoint-card-mark-color" to
                RunnerToken(ElementTokens::waypointCardMarkColor, ElementTokens.Builder::waypointCardMarkColor),
            "waypoint-card-mark-fg" to
                RunnerToken(ElementTokens::waypointCardMarkFg, ElementTokens.Builder::waypointCardMarkFg),
        )

    /** Each shadow token's member, by the name the contract's table gives it. */
    val shadowTokens: Map<String, RunnerToken<List<HexColor>>> =
        mapOf(
            "control-shadow" to RunnerToken(ElementTokens::controlShadow, ElementTokens.Builder::controlShadow),
            "profile-tooltip-shadow" to
                RunnerToken(ElementTokens::profileTooltipShadow, ElementTokens.Builder::profileTooltipShadow),
        )

    /** Each dash token's member, by the name the contract's table gives it. */
    val dashTokens: Map<String, RunnerToken<List<Double>>> =
        mapOf(
            "profile-cursor-dash" to
                RunnerToken(ElementTokens::profileCursorDash, ElementTokens.Builder::profileCursorDash),
        )

    /**
     * Sets on [builder] the value a consumer states for the token [name], as a consumer does: the token set whole to a
     * token of that value alone.
     */
    fun state(
        builder: ElementTokens.Builder,
        name: String,
        value: FixtureValue,
    ) {
        colorTokens[name]?.let { return it.write.set(builder, ThemeToken(light = color(value, name))) }
        shadowTokens[name]?.let { token ->
            val colors =
                (value as? FixtureValue.Array)?.items?.map { color(it, name) }
                    ?: throw AssertionError("`$name` is no list")
            return token.write.set(builder, ThemeToken(light = colors))
        }
        dashTokens[name]?.let { token ->
            val lengths =
                (value as? FixtureValue.Array)?.items?.map {
                    (it as? FixtureValue.Number)?.value
                        ?: throw AssertionError("`$name` holds a length that is no number")
                } ?: throw AssertionError("`$name` is no list")
            return token.write.set(builder, ThemeToken(light = lengths))
        }
        throw AssertionError("`$name` is no token of the table")
    }

    /**
     * The value the token [name] of [tokens] reads under [scheme], written as the table writes one: a colour `{hex}`,
     * its `opacity` beside it only where it is translucent, a shadow as its colours and a dash as its lengths.
     */
    fun value(
        name: String,
        tokens: ElementTokens,
        scheme: ThemeScheme,
    ): FixtureValue {
        colorTokens[name]?.let { return written(themeTokenValue(it.read.get(tokens), scheme)) }
        shadowTokens[name]?.let { token ->
            return FixtureValue.Array(themeTokenValue(token.read.get(tokens), scheme).map(::written))
        }
        dashTokens[name]?.let { token ->
            return FixtureValue.Array(themeTokenValue(token.read.get(tokens), scheme).map { FixtureValue.Number(it) })
        }
        throw AssertionError("`$name` is no token of the table")
    }

    private fun color(
        value: FixtureValue,
        name: String,
    ): HexColor {
        val written = value as? FixtureValue.Object ?: throw AssertionError("the value of `$name` is no colour")
        return HexColor(written.text("hex"), written.numberOrNull("opacity") ?: 1.0)
    }

    private fun written(color: HexColor): FixtureValue =
        FixtureValue.Object(
            listOfNotNull(
                "hex" to FixtureValue.Text(color.hex),
                if (color.opacity != 1.0) "opacity" to FixtureValue.Number(color.opacity) else null,
            ),
        )

    // Words and scopes.

    /** The word [words] holds at the dotted [path], or null where it holds none there. */
    fun word(
        path: String,
        words: JsonObject,
    ): JsonElement? {
        val keys = path.split(".")
        var section: JsonObject = words
        for (key in keys.dropLast(1)) section = section[key] as? JsonObject ?: return null
        return section[keys.last()]
    }

    /**
     * A statement of a nested scope as a fixture writes it: a `language`, a `locale`, the `words` it states, keyed by
     * section or by language, and the glyph sets its controls and its markers draw with, by their names ([glyphSet]).
     */
    fun statement(written: FixtureValue.Object): ElementsScopeStatement {
        val words =
            written.objectOrNull("words")?.let {
                wordOverridesOf(fixtureJson(it) as JsonObject)
                    ?: throw AssertionError("the words name a language and a section together")
            }
        return ElementsScopeStatement(
            language = written.textOrNull("language")?.let { RestSchema.Language(it) },
            locale = written.textOrNull("locale"),
            words = words,
            controlGlyphs = written.textOrNull("controlGlyphs")?.let(::glyphSet),
            markerGlyphs = written.textOrNull("markerGlyphs")?.let(::glyphSet),
        )
    }

    /**
     * The set a fixture names: the bundled control set, `bundled-controls`, the bundled marker set, `bundled-markers`,
     * or a licensed set of the host's own under any other name, which holds a glyph no rule here draws.
     */
    fun glyphSet(name: String): GlyphSet =
        when (name) {
            "bundled-controls" -> {
                GlyphSet.control
            }

            "bundled-markers" -> {
                GlyphSet.marker
            }

            else -> {
                GlyphSet.of(name, GlyphSet.Paint.Stroke, markup = mapOf("zoom-in" to """<path d="M0 0"/>"""))
                    ?: throw AssertionError("no set can be named `$name`")
            }
        }

    /** The name a fixture gives [set]. */
    fun nameOf(set: GlyphSet): String =
        when (set) {
            GlyphSet.control -> "bundled-controls"
            GlyphSet.marker -> "bundled-markers"
            else -> set.name
        }

    // The layers catalogue.

    /**
     * The catalogue a selection fixture names by slug: its terrains, one classic category, `conditions`, holding the
     * classic overlays, and the category whose overlays stack holding the enrichments, every other member a
     * placeholder no selection reads.
     */
    fun catalogueBySlug(written: FixtureValue.Object): TileSchema.Layers {
        val overlays = written.objectOrNull("overlays") ?: throw AssertionError("the catalogue names no `overlays`")

        fun slugs(items: List<FixtureValue>): List<String> =
            items.map { (it as? FixtureValue.Text)?.value ?: throw AssertionError("a slug is not a string") }
        return TileSchema.Layers(
            elevation =
                TileSchema.Elevation(
                    id = "55763749-32b4-42d5-9f62-b6c51ef54d99",
                    tile_size = TileSchema.ElevationTileSize.Size512,
                    url = Url("https://tiles.example.invalid/elevation"),
                ),
            itineraries = fixtureOverlay("itineraries", "itineraries"),
            overlays =
                listOf(
                    fixtureCategory("conditions", slugs(overlays.items("classic")).map { fixtureOverlay(it, it) }),
                    fixtureCategory(
                        ENRICHMENT_CATEGORY_SLUG,
                        slugs(overlays.items("enrichment")).map { fixtureOverlay(it, it) },
                    ),
                ),
            terrains = slugs(written.items("terrains")).map(::terrain),
            zones = fixtureOverlay("zones", "zones"),
        )
    }

    /** The terrain [slug] names, its variants placeholders no rule here reads. */
    private fun terrain(slug: String): TileSchema.Terrain {
        val variant =
            TileSchema.TerrainVariantConfig(
                style = TileSchema.TerrainVariantAsStyle(url = Url("https://tiles.example.invalid/$slug/style")),
                tiles =
                    TileSchema.TerrainVariantAsTiles(
                        size = TileSchema.TerrainTileSize.Size512,
                        url = Url("https://tiles.example.invalid/$slug/tiles"),
                    ),
            )
        return TileSchema.Terrain(
            id = slug,
            locales = TileSchema.Locales(de = null, en = null, es = null, fr = null, it = null, ja = null, zh = null),
            slug = slug,
            variants = TileSchema.TerrainVariants(dark = variant, light = variant),
        )
    }

    /** What a selection holds as the fixtures write it: its terrain, its overlay and its enrichments, and whether a refusal stands where [refusal] asks. */
    fun selectionShown(
        selection: LayersSelection,
        refusal: Boolean,
    ): FixtureValue =
        FixtureValue.Object(
            listOfNotNull(
                "enrichments" to FixtureValue.Array(selection.enrichments.map { FixtureValue.Text(it) }),
                "overlay" to (selection.overlay?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                "terrain" to (selection.terrain?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                if (refusal) "refused" to FixtureValue.Bool(selection.refusal != null) else null,
            ),
        )

    /**
     * SelectionMaps stands for the maps a layers selection drives, each named, every command any of them is given and
     * every catalogue read numbered from 0 in the order sent, across maps, and held until a step settles it, or answered
     * at once where [answersAtOnce] says so. A command that takes something off, a cleared overlay and a removed
     * enrichment, is a command like any other, held open or refused as one that draws is.
     */
    class SelectionMaps(
        private val catalogue: TileSchema.Layers,
        private val answersAtOnce: Boolean,
    ) {
        /** Refused is what a map a step refuses throws. */
        class Refused : Exception("refused")

        private companion object {
            /** The command a pin is given to a map as. */
            const val INSTANT = "instant"
        }

        /** One command a map was given: its [slug], or for a pin the [instant] pinned, null for none. */
        class Call(
            val map: String,
            val command: String,
            val slug: String?,
            val instant: Instant? = null,
        )

        private val named = mutableMapOf<String, HeldMap>()
        private val reads = mutableListOf<CompletableDeferred<TileSchema.Layers>>()
        private val calls = mutableListOf<Call>()
        private val answers = mutableListOf<CompletableDeferred<Unit>?>()

        /** The map [name] names, made the first time it is named, showing [terrain]. */
        fun map(
            name: String,
            terrain: String?,
        ): HeldMap = named.getOrPut(name) { HeldMap(name, terrain) }

        /** Lands the catalogue read [read]. */
        fun land(read: Int) {
            val held = reads.getOrNull(read)
            if (held == null ||
                held.isCompleted
            ) {
                throw AssertionError("the catalogue read $read was never sent, or answered")
            }
            held.complete(catalogue)
        }

        /** Fails the catalogue read [read], as a read that reached no catalogue does. */
        fun fail(read: Int) {
            val held = reads.getOrNull(read)
            if (held == null ||
                held.isCompleted
            ) {
                throw AssertionError("the catalogue read $read was never sent, or answered")
            }
            held.completeExceptionally(Refused())
        }

        /** Lets the command [call] succeed, or fail where [refused] says so. */
        fun settle(
            call: Int,
            refused: Boolean,
        ) {
            val held = answers.getOrNull(call)
            if (held == null || held.isCompleted) throw AssertionError("the command $call was never sent, or answered")
            if (refused) held.completeExceptionally(Refused()) else held.complete(Unit)
        }

        /** Fails everything still held, so no coroutine waits for ever once a case ends. */
        fun release() {
            for (read in reads) read.completeExceptionally(Refused())
            for (answer in answers) answer?.completeExceptionally(Refused())
        }

        /**
         * Every command a map was given, as the fixtures write one, naming its map where [naming] asks: a pin by its
         * instant in epoch milliseconds, null for none, and every other command by its slug.
         */
        fun written(naming: Boolean): FixtureValue =
            FixtureValue.Array(
                calls.map { call ->
                    FixtureValue.Object(
                        listOfNotNull(
                            "command" to FixtureValue.Text(call.command),
                            if (call.command == INSTANT) {
                                "instant" to TimeFixtures.written(call.instant)
                            } else {
                                "slug" to (call.slug?.let { FixtureValue.Text(it) } ?: FixtureValue.Null)
                            },
                            if (naming) "map" to FixtureValue.Text(call.map) else null,
                        ),
                    )
                },
            )

        /** HeldMap is one map the selection may drive, which records into the maps' ledger. */
        inner class HeldMap(
            private val name: String,
            override var terrain: String?,
        ) : LayersSelectionTarget {
            override suspend fun loadLayers(): TileSchema.Layers {
                val read = CompletableDeferred<TileSchema.Layers>()
                reads += read
                return read.await()
            }

            override suspend fun setTerrain(slug: String?) = command("terrain", slug)

            override suspend fun setOverlay(slug: String) = command("overlay", slug)

            override suspend fun clearOverlay() = command("overlay", null)

            override suspend fun addEnrichment(slug: String) = command("add-enrichment", slug)

            override suspend fun removeEnrichment(slug: String) = command("remove-enrichment", slug)

            override suspend fun setInstant(instant: Instant?) = command(INSTANT, null, instant)

            private suspend fun command(
                command: String,
                slug: String?,
                instant: Instant? = null,
            ) {
                calls += Call(name, command, slug, instant)
                if (answersAtOnce) {
                    answers += null
                    return
                }
                val answer = CompletableDeferred<Unit>()
                answers += answer
                answer.await()
            }
        }
    }
}
