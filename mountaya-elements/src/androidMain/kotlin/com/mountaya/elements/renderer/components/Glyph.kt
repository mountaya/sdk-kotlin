package com.mountaya.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import com.mountaya.geo.GlyphSet
import kotlin.math.min

/**
 * Glyph draws one glyph of a set, in Jetpack Compose: a control's role from the scope's control set, or any
 * glyph of the set it is handed.
 *
 * ```
 * Glyph("zoom-in")
 * Glyph("hut", Modifier.padding(4.dp), set = GlyphSet.marker, size = 24.dp, color = ink, label = "Refuge")
 * ```
 *
 * The glyph's markup is read into shapes (`parseGlyph`), never drawn as it was handed over, and each shape is
 * painted as its set paints it (`glyphPaint`): a stroke set strokes each shape at its width with round ends,
 * a fill set fills each, and a shape's own fill, stroke, cap, join and miter limit replace the set's for it
 * alone. The current colour is [color]: the elements' `control-fg` token where the host hands none, as the
 * tokens around it, `LocalMountayaTokens`, read it under the colour scheme in force, so a glyph beside the
 * controls is drawn in their ink and follows a `MountayaScope`'s scheme as they do, as `sdk-dart`'s draws in
 * the scope's. Compose's foundation keeps no colour of the content around a composable, so a host drawing it
 * beside text of its own theme passes that theme's colour, as it passes one to the text.
 *
 * Handed no set, it draws from the control set of the scope around it, `LocalMountayaScope`, which is the bundled
 * one, `GlyphSet.control`, where no `MountayaScope` states another: a host that restyles its controls' glyphs once
 * restyles the glyphs it draws beside them with it.
 *
 * A glyph the set does not hold, or whose markup draws nothing, is drawn as nothing and takes no room, as a
 * control draws a glyph its set cannot (`geo.icons.glyph-fallback`). The glyph is hidden from accessibility
 * services unless it is given a [label], for the glyph that is the control rather than one beside a label of
 * its own.
 *
 * @param name The glyph's name in its set.
 * @param modifier The host's modifier, as every composable takes one.
 * @param set The set the glyph is drawn from, the scope's control set where it is null.
 * @param size The side of the square the glyph is drawn in, its set's box scaled to it.
 * @param color The colour the glyph's current colour is, the `control-fg` token under the scheme in force unless
 * the host hands one.
 * @param label What an accessibility service reads the glyph as, or null to hide it from them.
 */
@Composable
public fun Glyph(
    name: String,
    modifier: Modifier = Modifier,
    set: GlyphSet? = null,
    size: Dp = 16.dp,
    color: Color = tokenColor(LocalMountayaTokens.current.controlFg, themeSchemeInForce()),
    label: String? = null,
) {
    val drawn = set ?: LocalMountayaScope.current.controlGlyphs
    val figures = remember(name, drawn) { glyphFigures(name, drawn).orEmpty() }
    if (figures.isEmpty()) return
    val described =
        if (label == null) {
            Modifier.clearAndSetSemantics {}
        } else {
            Modifier.clearAndSetSemantics {
                contentDescription = label
                role = Role.Image
            }
        }
    Layout(
        modifier =
            modifier.then(described).drawBehind {
                val scale = min(this.size.width, this.size.height) / drawn.viewBox.toFloat()
                scale(scale, scale, pivot = Offset.Zero) { drawGlyphFigures(figures, color) }
            },
    ) { _, constraints ->
        val side = size.roundToPx()
        layout(constraints.constrainWidth(side), constraints.constrainHeight(side)) {}
    }
}
