package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LayersPanelSectionsTest {
    private val conditions =
        TileFixture.category(
            "conditions",
            listOf(
                TileFixture.overlay("slopes", locales = TileFixture.names("Slope", "Pente")),
                TileFixture.overlay("snow-depth", locales = TileFixture.names("Snow depth", "Hauteur de neige")),
            ),
            TileFixture.names("Conditions", "Conditions"),
        )
    private val hazards =
        TileFixture.category(
            "hazards",
            listOf(
                TileFixture.overlay("avalanche", locales = TileFixture.names("Avalanche risk", "Risque d’avalanche")),
            ),
            TileFixture.names("Hazards", "Dangers"),
        )
    private val enrichment =
        TileFixture.category(
            "enrichment",
            listOf(
                TileFixture.overlay("pois", locales = TileFixture.names("Points of interest", "Points d’intérêt")),
                TileFixture.overlay("hillshade", locales = TileFixture.names("Hillshade", "Ombrage")),
            ),
            TileFixture.names("Enrichment", "Enrichissement"),
        )
    private val terrains =
        listOf(
            TileFixture.terrain("topo", TileFixture.names("Topographic", "Topographique")),
            TileFixture.terrain("satellite", TileFixture.names("Satellite")),
        )

    /** Two terrains, two classic categories and the enrichment category, the points of interest first in it. */
    private val catalogue = TileFixture.layers(listOf(conditions, enrichment, hazards), terrains)

    /** Each section's rows as `slug:mark`, `-` for none. */
    private fun marks(sections: List<LayersPanelSection>): List<List<String>> =
        sections.map { section -> section.rows.map { "${it.slug}:${it.mark?.id ?: "-"}" } }

    @Test
    fun theTerrainsThenTheEnrichmentsThenTheOverlaysOneCategoryAtATimeThenTheTimeWhereOffered() {
        val sections =
            layersPanelSections(
                LayersSelection(catalogue, terrain = "satellite", overlay = "slopes", enrichments = listOf("pois")),
                "en",
                isLocked = { it == "snow-depth" },
                offersTime = true,
            )

        assertEquals(
            listOf("layers.terrain", "layers.enrichment", "layers.overlays", "layers.time"),
            sections.map { it.heading.word },
        )
        assertEquals(
            listOf(
                LayersPanelChoice.Terrain,
                LayersPanelChoice.Enrichment,
                LayersPanelChoice.Overlay,
                LayersPanelChoice.Time,
            ),
            sections.map { it.choice },
        )
        assertEquals(listOf(null, "enrichment", "conditions", null), sections.map { it.category })
        assertEquals(
            listOf(
                listOf("topo:-", "satellite:tick"),
                listOf("hillshade:-"),
                listOf("slopes:tick", "snow-depth:lock"),
                emptyList(),
            ),
            marks(sections),
        )
        assertEquals(
            listOf(LayersPanelCategory("conditions", "Conditions"), LayersPanelCategory("hazards", "Hazards")),
            sections[2].categories,
        )
        assertTrue(sections.filter { it.choice != LayersPanelChoice.Overlay }.all { it.categories.isEmpty() })
        assertEquals(listOf("Topographic", "Satellite"), sections[0].rows.map { it.label })
    }

    @Test
    fun thePointsOfInterestAreNoSwitchAndAnEnrichmentCategoryOfThemAloneListsNoSection() {
        val alone = TileFixture.category("enrichment", listOf(TileFixture.overlay(POIS_OVERLAY_SLUG)))
        val sections =
            layersPanelSections(
                LayersSelection(TileFixture.layers(listOf(conditions, alone), terrains), enrichments = listOf("pois")),
                "en",
            )

        assertEquals("pois", POIS_OVERLAY_SLUG)
        assertEquals(listOf(LayersPanelChoice.Terrain, LayersPanelChoice.Overlay), sections.map { it.choice })
        assertTrue(sections.flatMap { it.rows }.none { it.slug == POIS_OVERLAY_SLUG })
    }

    @Test
    fun theOverlaysOpenOnThePickElseOnTheSelectedOverlaysCategoryElseOnTheFirst() {
        fun open(
            picked: String?,
            overlay: String?,
        ): Pair<String?, List<String>> {
            val overlays =
                layersPanelSections(LayersSelection(catalogue, overlay = overlay), "en", picked = picked)
                    .single { it.choice == LayersPanelChoice.Overlay }
            return overlays.category to overlays.rows.map { it.slug }
        }

        assertEquals("hazards" to listOf("avalanche"), open(picked = "hazards", overlay = "slopes"))
        assertEquals("hazards" to listOf("avalanche"), open(picked = null, overlay = "avalanche"))
        assertEquals("conditions" to listOf("slopes", "snow-depth"), open(picked = null, overlay = null))
        // A pick naming no category on offer, the enrichments' among them, is no pick.
        assertEquals("hazards" to listOf("avalanche"), open(picked = "weather", overlay = "avalanche"))
        assertEquals("hazards" to listOf("avalanche"), open(picked = "enrichment", overlay = "avalanche"))
        // An overlay no category holds opens on the first.
        assertEquals("conditions" to listOf("slopes", "snow-depth"), open(picked = null, overlay = "hillshade"))
    }

    @Test
    fun aCategoryHoldingNoOverlayIsNotOfferedAndACatalogueOfNoneListsNoOverlays() {
        val empty = TileFixture.category("weather", emptyList(), TileFixture.names("Weather"))
        val sections =
            layersPanelSections(LayersSelection(TileFixture.layers(listOf(empty, hazards, enrichment), terrains)), "en")
        assertEquals(
            listOf(LayersPanelCategory("hazards", "Hazards")),
            sections.single { it.choice == LayersPanelChoice.Overlay }.categories,
        )

        val none = layersPanelSections(LayersSelection(TileFixture.layers(listOf(empty, enrichment), terrains)), "en")
        assertEquals(listOf(LayersPanelChoice.Terrain, LayersPanelChoice.Enrichment), none.map { it.choice })
    }

    @Test
    fun anEntryAndACategoryAreNamedInTheReadersLanguageElseInEnglishElseByTheirSlug() {
        val unnamed =
            TileFixture.category(
                "conditions",
                conditions.children + TileFixture.overlay("aspect"),
                conditions.locales,
            )
        val sections =
            layersPanelSections(
                LayersSelection(
                    TileFixture.layers(
                        listOf(
                            unnamed,
                            enrichment,
                            hazards,
                            TileFixture.category("radar", listOf(TileFixture.overlay("rain"))),
                        ),
                    ),
                    terrain = "topo",
                ),
                "fr-CA",
            )

        assertEquals(listOf("Ombrage"), sections[1].rows.map { it.label })
        assertEquals(listOf("Pente", "Hauteur de neige", "aspect"), sections[2].rows.map { it.label })
        assertEquals(listOf("Conditions", "Dangers", "radar"), sections[2].categories.map { it.name })
    }

    @Test
    fun aLockedEntryShowsItsLockInPlaceOfItsTick() {
        val sections =
            layersPanelSections(
                LayersSelection(
                    TileFixture.layers(listOf(conditions, enrichment), terrains),
                    terrain = "satellite",
                    enrichments = listOf("hillshade"),
                ),
                "en",
                isLocked = { it in listOf("satellite", "hillshade") },
            )

        assertEquals(
            listOf(
                listOf("topo:-", "satellite:lock"),
                listOf("hillshade:lock"),
                listOf("slopes:-", "snow-depth:-"),
            ),
            marks(sections),
        )
        assertTrue(sections[0].rows[1].isChecked)
    }

    @Test
    fun theTerrainsAndTheTimeStandWhileTheCatalogueIsStillToLand() {
        val terrain =
            LayersPanelSection(LayersPanelHeading("layers.terrain"), LayersPanelChoice.Terrain, null, emptyList())

        assertEquals(listOf(terrain), layersPanelSections(LayersSelection(terrain = "topo"), "en"))
        assertEquals(
            listOf(
                terrain,
                LayersPanelSection(LayersPanelHeading("layers.time"), LayersPanelChoice.Time, null, emptyList()),
            ),
            layersPanelSections(LayersSelection(terrain = "topo"), "en", offersTime = true),
        )
    }

    @Test
    fun theSelectionHoldsAnEntryByItsSlugExactly() {
        val sections =
            layersPanelSections(
                LayersSelection(
                    TileFixture.layers(listOf(TileFixture.category("conditions", listOf(TileFixture.overlay("café"))))),
                    overlay = "café",
                ),
                "en",
            )

        assertEquals(listOf(false), sections[1].rows.map { it.isChecked })
    }
}
