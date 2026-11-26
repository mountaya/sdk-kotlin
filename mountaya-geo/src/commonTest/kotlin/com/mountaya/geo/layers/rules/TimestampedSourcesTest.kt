package com.mountaya.geo

import com.mountaya.client.generated.TileSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class TimestampedSourcesTest {
    private val instant = Instant.parse("2026-03-14T09:26:53.900Z")
    private val stamp = "timestamp=2026-03-14T09%3A26%3A53.900Z"

    /** An overlay of [slug] the server stamps where [server] says so, its one source drawn from [tiles]. */
    private fun overlay(
        slug: String,
        tiles: List<String>,
        server: Boolean = true,
        client: Boolean = false,
    ): TileSchema.Overlay {
        val bare = TileFixture.overlay(slug)
        return TileSchema.Overlay(
            capabilities =
                TileSchema.OverlayCapabilities(
                    client = TileSchema.OverlaySupportedCapabilities(timestamp = client),
                    server = TileSchema.OverlaySupportedCapabilities(timestamp = server),
                ),
            content_type = bare.content_type,
            id = slug,
            locales = bare.locales,
            slug = slug,
            sources =
                listOf(
                    TileSchema.TileSource(
                        id = slug,
                        layers = emptyList(),
                        tiles = tiles,
                        type = TileSchema.TileSourceType.Vector,
                        zoom_max = 14,
                        zoom_min = 0,
                    ),
                ),
        )
    }

    /** The one address [address] is stamped into, through a catalogue whose classic overlay draws from it alone. */
    private fun stamped(address: String): String =
        timestampedSources(
            TileFixture.layers(
                listOf(TileFixture.category("conditions", listOf(overlay("snow-depth", listOf(address))))),
            ),
            "snow-depth",
            instant,
        ).single().tiles.single()

    @Test
    fun theSelectedClassicOverlayTheServerStampsAsksEachSourceForTheInstant() {
        val catalogue =
            TileFixture.layers(
                listOf(
                    TileFixture.category(
                        "conditions",
                        listOf(
                            overlay("snow-depth", listOf("https://t/a/{z}/{x}/{y}?key=pk", "https://t/b/{z}/{x}/{y}")),
                        ),
                    ),
                ),
            )

        assertEquals(
            listOf(
                TimestampedSource(
                    "snow-depth",
                    listOf("https://t/a/{z}/{x}/{y}?key=pk&$stamp", "https://t/b/{z}/{x}/{y}?$stamp"),
                ),
            ),
            timestampedSources(catalogue, "snow-depth", instant),
        )
        assertEquals("timestamp", TIMESTAMP_PARAMETER)
    }

    @Test
    fun noOverlayAnUnstampedOneAMissingOneAndAnEnrichmentAskNothing() {
        val catalogue =
            TileFixture.layers(
                listOf(
                    TileFixture.category(
                        "conditions",
                        listOf(
                            overlay("slopes", listOf("https://t/slopes"), server = false),
                            overlay("hillshade-like", listOf("https://t/h"), server = false, client = true),
                        ),
                    ),
                    TileFixture.category("enrichment", listOf(overlay("radar", listOf("https://t/radar")))),
                ),
            )

        for (slug in listOf(null, "slopes", "hillshade-like", "avalanche", "radar")) {
            assertTrue(timestampedSources(catalogue, slug, instant).isEmpty(), "$slug asked something")
        }
    }

    @Test
    fun theParameterTakesThePlaceOfTheFirstPairNamedTimestampAndTheRestAreDropped() {
        assertEquals("https://t/{z}?$stamp&key=pk", stamped("https://t/{z}?timestamp=2026-01-01T00%3A00%3A00Z&key=pk"))
        assertEquals("https://t/{z}?$stamp&key=pk", stamped("https://t/{z}?&timestamp=a&&key=pk&timestamp=b&"))
        assertEquals("https://t/{z}?$stamp&key=pk", stamped("https://t/{z}?t%69mestamp=a&key=pk"))
        assertEquals("https://t/{z}?$stamp&key=pk", stamped("https://t/{z}?timestamp&key=pk"))
        // A `+` decodes to a space, so `time+stamp` names another parameter, and `timest%61mp` this one.
        assertEquals(
            "https://t/{z}?time+stamp=a&$stamp&key=pk",
            stamped("https://t/{z}?time+stamp=a&timest%61mp=b&key=pk"),
        )
    }

    @Test
    fun aNameInOtherLettersOrWhoseEscapeDoesNotDecodeIsAnotherParameter() {
        assertEquals("https://t/{z}?Timestamp=a&key=pk&$stamp", stamped("https://t/{z}?Timestamp=a&key=pk"))
        assertEquals("https://t/{z}?%zz=1&key=pk&$stamp", stamped("https://t/{z}?%zz=1&key=pk"))
        assertEquals("https://t/{z}?timestamp%=1&$stamp", stamped("https://t/{z}?timestamp%=1"))
        assertEquals("https://t/{z}?timestamp%C3=1&$stamp", stamped("https://t/{z}?timestamp%C3=1"))
    }

    @Test
    fun everyOtherPartIsKeptAsWrittenAndAnAddressWithNoSchemeAsItIs() {
        assertEquals(
            "https://t/{z}/{x}/{y}?sig=a%2Fb%7Bc%7D&key=pk&$stamp#layer",
            stamped("https://t/{z}/{x}/{y}?sig=a%2Fb%7Bc%7D&key=pk#layer"),
        )
        assertEquals("https://t/{z}/{x}/{y}.pbf?$stamp", stamped("https://t/{z}/{x}/{y}.pbf"))
        assertEquals("https://t/{z}?$stamp#a?b", stamped("https://t/{z}#a?b"))
        assertEquals("/v1/overlays/{z}?key=pk", stamped("/v1/overlays/{z}?key=pk"))
        assertEquals("1http://t/{z}", stamped("1http://t/{z}"))
        assertEquals("mbtiles+x.y-z://t/{z}?$stamp", stamped("mbtiles+x.y-z://t/{z}"))
    }

    @Test
    fun theInstantIsWrittenInUtcToTheMillisecondAWholeSecondWithItsZeros() {
        fun stampOf(at: Instant): String =
            timestampedSources(
                TileFixture.layers(
                    listOf(TileFixture.category("conditions", listOf(overlay("snow-depth", listOf("https://t/{z}"))))),
                ),
                "snow-depth",
                at,
            ).single().tiles.single().substringAfter("?")

        assertEquals("timestamp=2026-03-14T09%3A00%3A00.000Z", stampOf(Instant.parse("2026-03-14T09:00:00Z")))
        assertEquals("timestamp=2026-03-14T09%3A26%3A53.900Z", stampOf(Instant.parse("2026-03-14T10:26:53.9+01:00")))
        assertEquals(
            "timestamp=2026-03-14T09%3A26%3A53.999Z",
            stampOf(Instant.fromEpochSeconds(1773480413, 999_999_999)),
        )
        assertEquals("timestamp=1969-12-31T23%3A59%3A30.250Z", stampOf(Instant.fromEpochMilliseconds(-29750)))
    }
}
