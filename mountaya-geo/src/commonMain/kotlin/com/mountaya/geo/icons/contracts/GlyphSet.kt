package com.mountaya.geo

import com.mountaya.geo.generated.controlGlyphSet
import com.mountaya.geo.generated.markerGlyphSet

/**
 * GlyphSet is a set of glyphs drawn alike: its name, the markup of each glyph, by the glyph's name, and
 * the paint, the box and the stroke width every one of them is drawn at.
 *
 * [marker] and [control] are the sets the contract's glyph table holds, the marker set drawing every
 * waypoint icon, amenity badge, restricted-stretch kind and point of interest, and the control set every
 * role a control draws, both stroked 2 units wide in a 24-unit box (`geo.icons.bundled-sets`). A host
 * draws a set of its own, a licensed one, by building it from its markup with [of], alone or chained
 * over a bundled set with [chaining]. A glyph's markup is read into shapes by `parseGlyph`, and each
 * shape is painted as `glyphPaint` answers.
 *
 * Every set can be drawn: each factory refuses, by answering null, what a renderer could not draw, so a
 * set fails where it is declared and never as a blank marker on a map. Two sets are equal where their
 * names are and they hold the same markup, drawn alike.
 */
public class GlyphSet internal constructor(
    /** The name the set is known by, never empty: a chain's is the names of its sets joined by `+`. */
    public val name: String,
    /** How every glyph of the set is drawn. */
    public val paint: Paint,
    /**
     * The width a stroke set draws its glyphs at, and a fill set strokes a shape that asks for a stroke of
     * its own at, in the units of its box.
     */
    public val strokeWidth: Double,
    /** The side of the square box every glyph is drawn in. */
    public val viewBox: Double,
    /** Each glyph's markup, by the glyph's name: the set holds a glyph where it is a key. */
    public val markup: Map<String, String>,
) {
    /** Paint is how a set's glyphs are drawn: each shape filled, or each shape stroked. */
    public enum class Paint(
        /** The paint as the glyph table writes it. */
        public val id: String,
    ) {
        /** Each shape filled in the current colour, and none stroked. */
        Fill("fill"),

        /** Each shape stroked in the current colour at the set's width, and none filled. */
        Stroke("stroke"),
    }

    override fun equals(other: Any?): Boolean =
        other is GlyphSet &&
            other.name == name &&
            other.paint == paint &&
            other.strokeWidth.compareTo(strokeWidth) == 0 &&
            other.viewBox.compareTo(viewBox) == 0 &&
            other.markup == markup

    override fun hashCode(): Int {
        var hash = name.hashCode()
        hash = 31 * hash + paint.hashCode()
        hash = 31 * hash + strokeWidth.hashCode()
        hash = 31 * hash + viewBox.hashCode()
        return 31 * hash + markup.hashCode()
    }

    override fun toString(): String =
        "GlyphSet(name=$name, paint=$paint, strokeWidth=$strokeWidth, viewBox=$viewBox, ${markup.size} glyphs)"

    /** The sets the contract's glyph table holds, and the two ways a host builds one of its own. */
    public companion object {
        /**
         * The glyph table's marker set: every waypoint icon, amenity badge, restricted-stretch kind and point of
         * interest.
         */
        public val marker: GlyphSet get() = markerGlyphSet

        /** The glyph table's control set, whole: every role a control draws. */
        public val control: GlyphSet get() = controlGlyphSet

        /**
         * Builds a set from its [name], the [paint], the [strokeWidth] and the [viewBox] its glyphs are drawn
         * at, and their [markup], or answers null where it cannot be drawn (`geo.icons.glyph-set`).
         *
         * A set that states no width is drawn at 2 and one that states no box in a box of 24, under either
         * paint. It is refused where its name is empty, its box is no finite number above 0, or its width is
         * no finite number, or is 0 or less for a stroke set, which a stroke of no width draws nothing for,
         * or below 0 for a fill set, which reads its width only for a shape that asks for a stroke of its
         * own. The set keeps a copy of [markup], so a map the host changes later changes no set.
         */
        public fun of(
            name: String,
            paint: Paint,
            strokeWidth: Double = 2.0,
            viewBox: Double = 24.0,
            markup: Map<String, String>,
        ): GlyphSet? {
            val width = if (paint == Paint.Stroke) strokeWidth > 0.0 else strokeWidth >= 0.0
            val drawable = name.isNotEmpty() && viewBox.isFinite() && viewBox > 0.0 && strokeWidth.isFinite() && width
            return if (drawable) GlyphSet(name, paint, strokeWidth, viewBox, LinkedHashMap(markup)) else null
        }

        /**
         * Builds the chain of [sets], which answers each glyph from the first set that holds it and holds
         * every glyph any set holds, all drawn in the first set's paint, box and width, or answers null
         * where the chain cannot be drawn alike (`geo.icons.glyph-set-chain`). Its name is the names of
         * its sets joined by `+`, and it chains further as a set does.
         *
         * It is refused where it chains no set, and where a later set differs from the first in paint or in
         * box, or in width where the first strokes: a glyph drawn on another grid or for another weight
         * would be drawn wrong, and a fill set's width is never compared. A partial licensed set chains
         * over a bundled one of the same paint, box and width; across a paint, a host draws one set or the
         * other.
         */
        public fun chaining(sets: List<GlyphSet>): GlyphSet? {
            val first = sets.firstOrNull() ?: return null
            val alike =
                sets.all {
                    it.paint == first.paint &&
                        it.viewBox == first.viewBox &&
                        (first.paint == Paint.Fill || it.strokeWidth == first.strokeWidth)
                }
            if (!alike) return null
            val markup = LinkedHashMap<String, String>()
            for (set in sets) {
                for ((glyph, drawn) in set.markup) markup.getOrPut(glyph) { drawn }
            }
            return GlyphSet(sets.joinToString("+") { it.name }, first.paint, first.strokeWidth, first.viewBox, markup)
        }
    }
}
