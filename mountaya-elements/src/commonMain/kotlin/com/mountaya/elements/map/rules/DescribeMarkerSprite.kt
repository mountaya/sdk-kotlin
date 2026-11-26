package com.mountaya.elements

import com.mountaya.geo.GlyphSet

/** The side of a marker sprite, in density-independent pixels. */
internal const val MARKER_SPRITE_PX: Double = 48.0

/** The width of the border around a marker sprite's disc, in density-independent pixels. */
internal const val MARKER_BORDER_PX: Double = 3.0

/** The colour of a marker sprite's border and of its glyph. */
internal const val MARKER_WHITE: String = "#ffffff"

/**
 * describeMarkerSprite answers what the marker sprite of [glyph] in [color] draws from [set], or null
 * where the set does not hold the glyph, which makes no sprite (`map.icons.marker-sprite`).
 *
 * The image is 48 pixels square: a disc in the feature's colour, as given, of radius 21 under a white
 * border 3 pixels wide centred on its edge, which the image holds whole with a pixel and a half to spare,
 * and the glyph in white fitted to the 24-pixel square at its centre, half the image, whatever the box
 * the set draws in: one unit of a 48-unit box is half a pixel, of a 16-unit box a pixel and a half. The
 * glyph is painted as its set paints it (`glyphPaint`). A glyph the set holds with no shape still makes a
 * sprite: the rule asks whether the set holds it, never what it draws.
 */
internal fun describeMarkerSprite(
    glyph: String,
    color: String,
    set: GlyphSet,
): MarkerSprite? {
    if (glyph !in set.markup) return null
    val half = MARKER_SPRITE_PX / 2.0
    // The glyph's square is half the image, centred.
    val glyphPx = MARKER_SPRITE_PX / 2.0
    return MarkerSprite(
        size = MARKER_SPRITE_PX,
        disc =
            MarkerSprite.Disc(
                center = listOf(half, half),
                radius = half - MARKER_BORDER_PX,
                fill = color,
                borderColor = MARKER_WHITE,
                borderWidth = MARKER_BORDER_PX,
            ),
        glyph =
            MarkerSprite.GlyphFit(
                offset = (MARKER_SPRITE_PX - glyphPx) / 2.0,
                scale = glyphPx / set.viewBox,
                color = MARKER_WHITE,
            ),
    )
}
