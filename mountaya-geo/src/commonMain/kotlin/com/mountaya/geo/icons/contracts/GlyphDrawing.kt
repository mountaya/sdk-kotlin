package com.mountaya.geo

import com.mountaya.geo.generated.ElementTokens
import com.mountaya.geo.generated.artworkDrawings
import kotlin.reflect.KProperty1

/**
 * GlyphDrawing is one of Mountaya's own drawings, which no glyph set holds since each of its shapes is
 * painted on its own: the compass rose, whose needle keeps its colour while its disc follows the scheme,
 * the profile's marker, and the Mountaya watermark a map draws, its shapes filled with gradients and laid
 * over the map at an [opacity] above a [shadow].
 *
 * [artwork] holds the drawings the contract's glyph table carries, by name. Two drawings are equal where
 * their boxes, their shapes, their opacities and their shadows are, every number compared as every value of
 * the geo layer compares it, `NaN` the same as `NaN` and 0 another than -0, and equal drawings hash alike.
 */
public class GlyphDrawing(
    /** The box the shapes are drawn in, `[x, y, width, height]`, every shape clipped to it. */
    public val viewBox: List<Double>,
    /** The drawing's shapes, in drawing order. */
    public val shapes: List<PaintedShape>,
    /** How opaque the whole drawing, its shadow included, is laid over what is under it, from 0 to 1. */
    public val opacity: Double = 1.0,
    /** The drop shadow painted beneath the shapes and never over them, or null where the drawing states none. */
    public val shadow: Shadow? = null,
) {
    /**
     * Shadow is a drawing's drop shadow: its silhouette offset by [dx] and [dy], blurred by a Gaussian of the
     * standard deviation [blur], all in the units of the drawing's box, and painted in [color].
     */
    public class Shadow(
        /** The standard deviation of the Gaussian blur, in the drawing's units. */
        public val blur: Double,
        /** How far right the silhouette is moved, in the drawing's units. */
        public val dx: Double,
        /** How far down the silhouette is moved, in the drawing's units. */
        public val dy: Double,
        /** The colour the shadow is painted in, at its opacity. */
        public val color: HexColor,
    ) {
        override fun equals(other: Any?): Boolean =
            other is Shadow && sameNumber(other.blur, blur) && sameNumber(other.dx, dx) &&
                sameNumber(other.dy, dy) && other.color == color

        override fun hashCode(): Int =
            ((blur.hashCode() * 31 + dx.hashCode()) * 31 + dy.hashCode()) * 31 + color.hashCode()

        override fun toString(): String = "Shadow(blur=$blur, dx=$dx, dy=$dy, color=$color)"
    }

    /** GradientStop is one colour of a gradient, at its [offset] from 0, the gradient's start, to 1, its end. */
    public class GradientStop(
        /** Where the colour stands along the gradient, from 0 to 1. */
        public val offset: Double,
        /** The colour at that offset, at its opacity. */
        public val color: HexColor,
    ) {
        override fun equals(other: Any?): Boolean =
            other is GradientStop && sameNumber(other.offset, offset) && other.color == color

        override fun hashCode(): Int = offset.hashCode() * 31 + color.hashCode()

        override fun toString(): String = "GradientStop(offset=$offset, color=$color)"
    }

    /**
     * Paint is a shape's fill or stroke: a colour of its own, or one of the elements' colour tokens, each
     * at an opacity, or a fill's linear gradient.
     */
    public sealed interface Paint {
        /**
         * LinearGradient is a fill that runs from [from] to [to], points in the drawing's units, through its
         * [stops], the colour before the first stop and past the last one held.
         */
        public class LinearGradient(
            /** Where the gradient starts, `[x, y]` in the drawing's units. */
            public val from: List<Double>,
            /** Where the gradient ends, `[x, y]` in the drawing's units. */
            public val to: List<Double>,
            /** The gradient's colours, in order of their offsets. */
            public val stops: List<GradientStop>,
        ) : Paint {
            override fun equals(other: Any?): Boolean =
                other is LinearGradient && sameNumbers(other.from, from) && sameNumbers(other.to, to) &&
                    other.stops == stops

            override fun hashCode(): Int = (from.hashCode() * 31 + to.hashCode()) * 31 + stops.hashCode()

            override fun toString(): String = "Paint.LinearGradient(from=$from, to=$to, stops=$stops)"
        }

        /** Color is a colour the drawing fixes, whatever the scheme. */
        public class Color(
            /** The colour. */
            public val color: HexColor,
        ) : Paint {
            override fun equals(other: Any?): Boolean = other is Color && other.color == color

            override fun hashCode(): Int = color.hashCode()

            override fun toString(): String = "Paint.Color($color)"
        }

        /** Token is one of the colours of [ElementTokens], read under the scheme drawn in, at an opacity. */
        public class Token(
            /** The colour token, as `ElementTokens::controlBg` names it. */
            public val token: KProperty1<ElementTokens, ThemeToken<HexColor>>,
            /** How opaque the token's colour is drawn, from 0 to 1. */
            public val opacity: Double,
        ) : Paint {
            override fun equals(other: Any?): Boolean =
                other is Token && other.token == token && other.opacity.compareTo(opacity) == 0

            override fun hashCode(): Int = token.hashCode() * 31 + opacity.hashCode()

            override fun toString(): String = "Paint.Token(${token.name}, opacity=$opacity)"
        }
    }

    /**
     * PaintedShape is one shape of a drawing: its markup and the paint it is drawn in.
     *
     * Two are equal where their markup, their paints and their stroke's width are, the width the same
     * number as every value of the geo layer reads one, `NaN` the same as `NaN` and 0 another than -0.
     */
    public class PaintedShape(
        /** The shape's markup, read into shapes by `parseGlyph`. */
        public val markup: String,
        /** What the shape is filled with, or null where it is not filled. */
        public val fill: Paint? = null,
        /** What the shape is stroked with, or null where it is not stroked. */
        public val stroke: Paint? = null,
        /** The width of the shape's stroke, in the units of the drawing's box, or null where it is not stroked. */
        public val strokeWidth: Double? = null,
    ) {
        override fun equals(other: Any?): Boolean =
            other is PaintedShape &&
                other.markup == markup &&
                other.fill == fill &&
                other.stroke == stroke &&
                sameNumberOrNull(other.strokeWidth, strokeWidth)

        override fun hashCode(): Int {
            var hash = markup.hashCode()
            hash = 31 * hash + fill.hashCode()
            hash = 31 * hash + stroke.hashCode()
            return 31 * hash + strokeWidth.hashCode()
        }

        override fun toString(): String =
            "PaintedShape(markup=$markup, fill=$fill, stroke=$stroke, strokeWidth=$strokeWidth)"
    }

    override fun equals(other: Any?): Boolean =
        other is GlyphDrawing && sameNumbers(other.viewBox, viewBox) && other.shapes == shapes &&
            sameNumber(other.opacity, opacity) && other.shadow == shadow

    override fun hashCode(): Int =
        ((viewBox.hashCode() * 31 + shapes.hashCode()) * 31 + opacity.hashCode()) * 31 + shadow.hashCode()

    override fun toString(): String = "GlyphDrawing(viewBox=$viewBox, shapes=$shapes, opacity=$opacity, shadow=$shadow)"

    /** The drawings the contract's glyph table carries. */
    public companion object {
        /**
         * The drawings of Mountaya's own that the contract's glyph table carries, by the name it gives each, as a view
         * that refuses every change: a cast to a mutable map throws, so no caller removes or replaces a drawing a map
         * draws, the watermark among them.
         */
        public val artwork: Map<String, GlyphDrawing> by lazy { ReadOnlyMap(artworkDrawings) }
    }
}

/**
 * ReadOnlyMap is [held] behind a map that declares no mutable member, so it is no `MutableMap` at runtime on any
 * platform and a cast to one fails rather than reaching [held].
 */
private class ReadOnlyMap<K, V>(
    private val held: Map<K, V>,
) : Map<K, V> by held {
    override fun equals(other: Any?): Boolean = held == other

    override fun hashCode(): Int = held.hashCode()

    override fun toString(): String = held.toString()
}
