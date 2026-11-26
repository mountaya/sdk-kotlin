package com.mountaya.geo

import com.mountaya.client.Url
import com.mountaya.client.generated.TileSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.picker-rows`: the sections a layers picker lists over the case's `catalogue`, null while it is still to
 * be read, and `selection` in its `language`, its `locked` entries locked, the classic category it `picked`, none where
 * the case states none, and the time offered where `time` says so, which a case stating none does not; and what a
 * press on its `press` asks for. A section is written with its heading's word by its path, the catalogue category it
 * lists, and the overlays' categories, each by its slug and its name. The category whose overlays stack and the
 * points-of-interest overlay are held to the ones Geo publishes, `ENRICHMENT_CATEGORY_SLUG` and `POIS_OVERLAY_SLUG`.
 */
object GeoLayersPickerRowsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstantsExactly(
            case,
            mapOf(
                "enrichmentCategory" to FixtureValue.Text(ENRICHMENT_CATEGORY_SLUG),
                "poisOverlay" to FixtureValue.Text(POIS_OVERLAY_SLUG),
            ),
        )
        val input = case.fields
        val selected = input.objectOrNull("selection") ?: throw AssertionError("the case names no `selection`")
        val locked = texts(input.items("locked"))
        val selection =
            LayersSelection(
                catalogue = input.objectOrNull("catalogue")?.let(::catalogueOf),
                terrain = selected.textOrNull("terrain"),
                overlay = selected.textOrNull("overlay"),
                enrichments = texts(selected.items("enrichments")),
            )
        val sections =
            layersPanelSections(
                selection,
                input.text("language"),
                isLocked = { it in locked },
                picked = input.textOrNull("picked"),
                offersTime = input["time"]?.let { input.bool("time") } ?: false,
            )
        val pressed =
            input.textOrNull("press")?.let { press ->
                val section =
                    sections.firstOrNull { section -> section.rows.any { it.slug == press } }
                        ?: throw AssertionError("no row of `$press` stands")
                when (val asked = layersPanelPress(section.choice, section.rows.first { it.slug == press })) {
                    is LayersPanelPress.Locked -> {
                        FixtureValue.Object(listOf("locked" to FixtureValue.Text(asked.slug)))
                    }

                    is LayersPanelPress.Command -> {
                        FixtureValue.Object(
                            listOf(
                                "command" to FixtureValue.Text(asked.choice.id),
                                "slug" to FixtureValue.Text(asked.slug),
                            ),
                        )
                    }
                }
            } ?: FixtureValue.Null
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf("pressed" to pressed, "sections" to FixtureValue.Array(sections.map(::written))),
            ),
        )
    }

    /** A section as the fixture writes one, the overlays' with the categories their switch offers. */
    private fun written(section: LayersPanelSection): FixtureValue =
        FixtureValue.Object(
            listOfNotNull(
                if (section.choice == LayersPanelChoice.Overlay) {
                    "categories" to
                        FixtureValue.Array(
                            section.categories.map { category ->
                                FixtureValue.Object(
                                    listOf(
                                        "name" to FixtureValue.Text(category.name),
                                        "slug" to FixtureValue.Text(category.slug),
                                    ),
                                )
                            },
                        )
                } else {
                    null
                },
                "category" to (section.category?.let { FixtureValue.Text(it) } ?: FixtureValue.Null),
                "choice" to FixtureValue.Text(section.choice.id),
                "heading" to FixtureValue.Object(listOf("word" to FixtureValue.Text(section.heading.word))),
                "rows" to
                    FixtureValue.Array(
                        section.rows.map { row ->
                            FixtureValue.Object(
                                listOf(
                                    "checked" to FixtureValue.Bool(row.isChecked),
                                    "label" to FixtureValue.Text(row.label),
                                    "mark" to (row.mark?.let { FixtureValue.Text(it.id) } ?: FixtureValue.Null),
                                    "slug" to FixtureValue.Text(row.slug),
                                ),
                            )
                        },
                    ),
            ),
        )

    /** The texts [items] holds. */
    private fun texts(items: List<FixtureValue>): List<String> =
        items.map {
            (it as? FixtureValue.Text)?.value ?: throw AssertionError(
                "a list holds a value that is no string",
            )
        }

    /**
     * The catalogue a picker fixture writes: its terrains and its categories, each entry by its slug and the names
     * its `locales` hold, every other member a placeholder no picker reads.
     */
    private fun catalogueOf(written: FixtureValue.Object): TileSchema.Layers {
        val categories =
            written.items("overlays").map { item ->
                val category = item as? FixtureValue.Object ?: throw AssertionError("a category is not an object")
                val slug = category.text("slug")
                TileSchema.CategoryOverlays(
                    children =
                        category.items("children").map { child ->
                            val entry =
                                child as? FixtureValue.Object ?: throw AssertionError("an overlay is not an object")
                            overlay(entry.text("slug"), localesOf(entry))
                        },
                    id = slug,
                    locales = localesOf(category),
                    slug = slug,
                )
            }
        val terrains =
            written.items("terrains").map { item ->
                val terrain = item as? FixtureValue.Object ?: throw AssertionError("a terrain is not an object")
                terrainOf(terrain.text("slug"), localesOf(terrain))
            }
        return TileSchema.Layers(
            elevation =
                TileSchema.Elevation(
                    id = "55763749-32b4-42d5-9f62-b6c51ef54d99",
                    tile_size = TileSchema.ElevationTileSize.Size512,
                    url = Url("https://tiles.example.invalid/elevation"),
                ),
            itineraries = overlay("itineraries", noLocales),
            overlays = categories,
            terrains = terrains,
            zones = overlay("zones", noLocales),
        )
    }

    private val noLocales =
        TileSchema.Locales(
            de = null,
            en = null,
            es = null,
            fr = null,
            it = null,
            ja = null,
            zh = null,
        )

    /** The names an entry's `locales` hold, keyed by language. */
    private fun localesOf(entry: FixtureValue.Object): TileSchema.Locales {
        val languages = entry.objectOrNull("locales") ?: throw AssertionError("an entry has no `locales` object")

        fun names(language: String): TileSchema.LocaleMap? =
            languages.objectOrNull(language)?.entries?.associate { (key, value) ->
                key to
                    (value as? FixtureValue.Text)?.value
            }
        return TileSchema.Locales(
            de = names("de"),
            en = names("en"),
            es = names("es"),
            fr = names("fr"),
            it = names("it"),
            ja = names("ja"),
            zh = names("zh"),
        )
    }

    /** The overlay [slug] names, under [locales], every other member a placeholder. */
    private fun overlay(
        slug: String,
        locales: TileSchema.Locales,
    ): TileSchema.Overlay =
        TileSchema.Overlay(
            capabilities =
                TileSchema.OverlayCapabilities(
                    client = TileSchema.OverlaySupportedCapabilities(timestamp = false),
                    server = TileSchema.OverlaySupportedCapabilities(timestamp = false),
                ),
            content_type = TileSchema.OverlayContentType.Png,
            id = slug,
            locales = locales,
            slug = slug,
            sources = emptyList(),
        )

    /** The terrain [slug] names, under [locales], its variants placeholders no rule here reads. */
    private fun terrainOf(
        slug: String,
        locales: TileSchema.Locales,
    ): TileSchema.Terrain {
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
            locales = locales,
            slug = slug,
            variants = TileSchema.TerrainVariants(dark = variant, light = variant),
        )
    }
}
