package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class FindOverlayTest {
    private val rain = TileFixture.overlay("rain")
    private val weatherSnow = TileFixture.overlay("snow", id = "weather-snow")
    private val pois = TileFixture.overlay("pois")
    private val enrichmentSnow = TileFixture.overlay("snow", id = "enrichment-snow")
    private val layers =
        TileFixture.layers(
            listOf(
                TileFixture.category("weather", listOf(rain, weatherSnow)),
                TileFixture.category(
                    ENRICHMENT_CATEGORY_SLUG,
                    listOf(TileFixture.overlay("hillshade"), pois, enrichmentSnow),
                ),
            ),
        )

    @Test
    fun theFirstOverlayCarryingTheSlugIsTheAnswer() {
        assertSame(rain, findOverlay(layers, "rain"))
        assertSame(weatherSnow, findOverlay(layers, "snow"))
        assertNull(findOverlay(layers, "fog"))
    }

    @Test
    fun aKindNamedReadsOnlyTheCategoriesOfThatKind() {
        assertSame(rain, findOverlay(layers, "rain", OverlayKind.Classic))
        assertSame(pois, findOverlay(layers, "pois", OverlayKind.Enrichment))
        assertSame(enrichmentSnow, findOverlay(layers, "snow", OverlayKind.Enrichment))
        assertNull(findOverlay(layers, "rain", OverlayKind.Enrichment))
        assertNull(findOverlay(layers, "pois", OverlayKind.Classic))
    }

    /** The slug and the category's are read exactly as the manifest spells them. */
    @Test
    fun aSlugIsMatchedExactly() {
        assertNull(findOverlay(layers, "Rain"))
        assertEquals(OverlayKind.Classic, overlayKindOf(TileFixture.category("Enrichment", emptyList())))
        assertEquals(OverlayKind.Enrichment, overlayKindOf(TileFixture.category("enrichment", emptyList())))
    }
}
