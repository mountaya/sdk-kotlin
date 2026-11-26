package com.mountaya.conformance

import com.mountaya.client.generated.TileSchema
import com.mountaya.geo.TIMESTAMP_PARAMETER
import com.mountaya.geo.timestampedSources
import kotlinx.coroutines.test.TestScope

/**
 * `geo.layers.tile-timestamp`: the sources `timestampedSources` asks again for the case's `instant`, over a typed
 * catalogue of its `overlays`, each category by its slug and each overlay by its slug, its two `capabilities` and its
 * `sources`, each its `id` and its `tiles`, every other member a placeholder no rule reads, the selected classic overlay
 * named by `overlay`. An instant that names no moment cannot be handed the rule, Kotlin's `Instant` holding none, so it
 * asks nothing. The parameter is held to the one Geo publishes, `TIMESTAMP_PARAMETER`.
 */
object GeoLayersTileTimestampRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values = mapOf("parameter" to FixtureValue.Text(TIMESTAMP_PARAMETER)),
        )
        val input = case.fields
        val catalogue = input.objectOrNull("catalogue") ?: throw AssertionError("the case names no `catalogue`")
        val instant = TimeFixtures.instant(input.text("instant"))
        val asked =
            instant?.let { timestampedSources(catalogueOf(catalogue), input.textOrNull("overlay"), it) }.orEmpty()
        return ConformanceOutcome.Value(
            FixtureValue.Array(
                asked.map { source ->
                    FixtureValue.Object(
                        listOf("source" to FixtureValue.Text(source.sourceId), "tiles" to textArray(source.tiles)),
                    )
                },
            ),
        )
    }

    /** The catalogue the case writes: its categories, each overlay by its slug, its capabilities and its sources. */
    private fun catalogueOf(written: FixtureValue.Object): TileSchema.Layers =
        fixtureLayers(
            written.items("overlays").map { item ->
                val category = item as? FixtureValue.Object ?: throw AssertionError("a category is not an object")
                fixtureCategory(
                    category.text("slug"),
                    category.items("children").map { child ->
                        overlayOf(child as? FixtureValue.Object ?: throw AssertionError("an overlay is not an object"))
                    },
                )
            },
        )

    /** The overlay [written] states: its slug, the times its server and its client resolve, and its sources. */
    private fun overlayOf(written: FixtureValue.Object): TileSchema.Overlay {
        val capabilities =
            written.objectOrNull("capabilities") ?: throw AssertionError("an overlay states no `capabilities`")

        fun resolves(side: String): Boolean =
            capabilities.objectOrNull(side)?.bool("timestamp") ?: throw AssertionError("no `$side.timestamp`")
        val slug = written.text("slug")
        return TileSchema.Overlay(
            capabilities =
                TileSchema.OverlayCapabilities(
                    client = TileSchema.OverlaySupportedCapabilities(timestamp = resolves("client")),
                    server = TileSchema.OverlaySupportedCapabilities(timestamp = resolves("server")),
                ),
            content_type = TileSchema.OverlayContentType.Png,
            id = slug,
            locales = fixtureOverlay(slug, slug).locales,
            slug = slug,
            sources =
                written.items("sources").map { item ->
                    val source = item as? FixtureValue.Object ?: throw AssertionError("a source is not an object")
                    TileSchema.TileSource(
                        id = source.text("id"),
                        layers = emptyList(),
                        tiles = fixtureTexts(source.items("tiles"), "`tiles`"),
                        type = TileSchema.TileSourceType.Vector,
                        zoom_max = 14,
                        zoom_min = 0,
                    )
                },
        )
    }
}
