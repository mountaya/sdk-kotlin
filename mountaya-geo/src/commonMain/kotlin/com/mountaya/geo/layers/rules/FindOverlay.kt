package com.mountaya.geo

import com.mountaya.client.generated.TileSchema

/** ENRICHMENT_CATEGORY_SLUG is the slug of the one category of the layers manifest whose overlays stack. */
public const val ENRICHMENT_CATEGORY_SLUG: String = "enrichment"

/**
 * overlayKindOf answers the kind of overlay [category] holds: an enrichment where its slug is
 * [ENRICHMENT_CATEGORY_SLUG], exactly, and a classic overlay for every other category.
 */
public fun overlayKindOf(category: TileSchema.CategoryOverlays): OverlayKind =
    if (category.slug == ENRICHMENT_CATEGORY_SLUG) OverlayKind.Enrichment else OverlayKind.Classic

/**
 * findOverlay answers the overlay of the layers manifest [layers] that [slug] names: the first
 * overlay carrying the slug, exactly as the manifest spells it, reading the categories in the
 * manifest's order and each one's overlays in its own, among the categories of [kind] where a kind is
 * named. Null where none carries it, and where only an overlay of the other kind does.
 */
public fun findOverlay(
    layers: TileSchema.Layers,
    slug: String,
    kind: OverlayKind? = null,
): TileSchema.Overlay? =
    layers.overlays
        .asSequence()
        .filter { kind == null || overlayKindOf(it) == kind }
        .firstNotNullOfOrNull { category -> category.children.firstOrNull { it.slug == slug } }
