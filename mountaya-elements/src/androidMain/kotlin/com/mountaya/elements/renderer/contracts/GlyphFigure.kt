package com.mountaya.elements

import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import com.mountaya.geo.ShapePaint

/**
 * GlyphFigure is one shape of a glyph as a renderer draws it: its outline in the units of its set's box,
 * the transform it is drawn through, and its paint, the current colour standing for whatever colour the
 * surface draws the glyph in.
 *
 * The transform is applied as the shape is drawn, never to the outline, so a stroke's width scales with
 * the shape as SVG scales it. The outline's fill type is the paint's fill rule.
 */
internal class GlyphFigure(
    /** The shape's outline, before its transform. */
    val path: Path,
    /** The transform the shape is drawn through. */
    val transform: Matrix,
    /** How the shape is filled and stroked (`geo.icons.glyph-paint`). */
    val paint: ShapePaint,
)
