package com.mountaya.geo

import com.mountaya.client.generated.TileSchema

/**
 * POIS_OVERLAY_SLUG is the slug of the points-of-interest overlay: an enrichment the points-of-interest picker
 * drives (`geo.layers.poi-rows`), which a layers picker never lists (`geo.layers.picker-rows`).
 */
public const val POIS_OVERLAY_SLUG: String = "pois"

/**
 * layersPanelSections is what a layers picker lists over [selection] (`geo.layers.picker-rows`), in the sections and the
 * order of the map studio's layers panel:
 *
 * - **the terrains**, headed by the word `layers.terrain`, as picture cards in one choice, a section standing while the
 *   catalogue is still to land, with no card;
 * - **the enrichments**, headed by `layers.enrichment`, as switches: the overlays of every category whose overlays
 *   stack, in the catalogue's order, the points of interest left out ([POIS_OVERLAY_SLUG]), since their own picker
 *   drives them, the section standing only where it lists a row;
 * - **the classic overlays**, headed by `layers.overlays`, as picture cards in one choice across every classic
 *   category, shown one category at a time: `categories` lists each classic category holding an overlay, which the
 *   picker switches between where it lists two or more, and the rows are the overlays of the category it opens on, the
 *   section standing only where it lists a category;
 * - **the time**, headed by `layers.time`, with no row, where the host offers it, [offersTime].
 *
 * The overlays open on [picked], the category the reader last picked in the switch, where it names a listed category,
 * else on the first listed category holding the selected overlay, else on the first listed: a pick naming no category on
 * offer, the enrichments' among them, is no pick. The split between enrichments and classic overlays is the
 * catalogue's, never a preference: base maps replace one another, classic overlays replace one another across
 * categories, since two of them repaint the same ground, and enrichments stack (`overlayKindOf`).
 *
 * A row is named in [language], else in English, else by its slug (`localizedName`), and so is a category. A row is
 * checked where the selection holds its slug, compared exactly, and shows its lock where [isLocked] says the entry is
 * out of the viewer's reach, in place of any tick, else a tick where it is checked.
 */
public fun layersPanelSections(
    selection: LayersSelection,
    language: String,
    isLocked: (String) -> Boolean = { false },
    picked: String? = null,
    offersTime: Boolean = false,
): List<LayersPanelSection> {
    fun row(
        slug: String,
        locales: TileSchema.Locales,
        isChecked: Boolean,
    ): LayersPanelRow =
        LayersPanelRow(
            slug = slug,
            label = localizedName(locales, slug, language),
            isChecked = isChecked,
            mark =
                when {
                    isLocked(slug) -> LayersPanelMark.Lock
                    isChecked -> LayersPanelMark.Tick
                    else -> null
                },
        )
    val catalogue = selection.catalogue
    val categories = catalogue?.overlays.orEmpty()
    val sections = mutableListOf<LayersPanelSection>()
    sections +=
        LayersPanelSection(
            heading = LayersPanelHeading("layers.terrain"),
            choice = LayersPanelChoice.Terrain,
            category = null,
            rows = catalogue?.terrains.orEmpty().map { row(it.slug, it.locales, it.slug == selection.terrain) },
        )

    val switches =
        categories
            .filter { overlayKindOf(it) == OverlayKind.Enrichment }
            .flatMap { it.children }
            .filter { it.slug != POIS_OVERLAY_SLUG }
            .map { row(it.slug, it.locales, it.slug in selection.enrichments) }
    if (switches.isNotEmpty()) {
        sections +=
            LayersPanelSection(
                heading = LayersPanelHeading("layers.enrichment"),
                choice = LayersPanelChoice.Enrichment,
                category = ENRICHMENT_CATEGORY_SLUG,
                rows = switches,
            )
    }

    val offered = categories.filter { overlayKindOf(it) == OverlayKind.Classic && it.children.isNotEmpty() }
    val open =
        offered.firstOrNull { it.slug == picked }
            ?: offered.firstOrNull { category -> category.children.any { it.slug == selection.overlay } }
            ?: offered.firstOrNull()
    if (open != null) {
        sections +=
            LayersPanelSection(
                heading = LayersPanelHeading("layers.overlays"),
                choice = LayersPanelChoice.Overlay,
                category = open.slug,
                rows = open.children.map { row(it.slug, it.locales, it.slug == selection.overlay) },
                categories =
                    offered.map { LayersPanelCategory(it.slug, localizedName(it.locales, it.slug, language)) },
            )
    }

    if (offersTime) {
        sections +=
            LayersPanelSection(
                heading = LayersPanelHeading("layers.time"),
                choice = LayersPanelChoice.Time,
                category = null,
                rows = emptyList(),
            )
    }
    return sections
}
