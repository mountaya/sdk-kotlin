package com.mountaya.conformance

import com.mountaya.geo.GlyphSet
import com.mountaya.geo.GlyphShape
import com.mountaya.geo.ShapePaint

/** The markup each glyph of a set the fixtures state by its glyphs alone holds: a placeholder no rule reads. */
internal const val PLACEHOLDER_GLYPH: String = "<path d=\"M0 0h1\"/>"

/**
 * The set [written] declares, as `geo.icons.glyph-set` declares one: its `name`, its `paint`, its
 * `markup` where it states one, and its `strokeWidth` and `viewBox` where it states them, the factory's
 * own defaults standing for those it leaves out; null where the set is refused, a paint the closed
 * `GlyphSet.Paint` does not hold among them.
 */
internal fun fixtureGlyphSet(written: FixtureValue.Object): GlyphSet? {
    val name = written.text("name")
    val width = written.numberOrNull("strokeWidth")
    val box = written.numberOrNull("viewBox")
    val markup =
        written.objectOrNull("markup")?.entries.orEmpty().associate { (glyph, value) ->
            glyph to
                ((value as? FixtureValue.Text)?.value ?: throw AssertionError("the markup of `$glyph` is not a string"))
        }
    val paint = GlyphSet.Paint.entries.firstOrNull { it.id == written.text("paint") } ?: return null
    return when {
        width != null && box != null -> GlyphSet.of(name, paint, width, box, markup)
        width != null -> GlyphSet.of(name, paint, strokeWidth = width, markup = markup)
        box != null -> GlyphSet.of(name, paint, viewBox = box, markup = markup)
        else -> GlyphSet.of(name, paint, markup = markup)
    }
}

/** The set [written] declares, which the case states as one that is drawn. */
internal fun declaredGlyphSet(written: FixtureValue.Object): GlyphSet =
    fixtureGlyphSet(written) ?: throw AssertionError("a set the case declares is refused")

/** A stroke set holding [glyphs] and no other, each a placeholder no rule reads, as the fixtures state a set by the glyphs it holds. */
internal fun glyphSetHolding(glyphs: List<String>): GlyphSet =
    GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = glyphs.associateWith { PLACEHOLDER_GLYPH })
        ?: throw AssertionError("a set holding the case's glyphs is refused")

/** [value], a shape as `geo.icons.glyph-parse` answers one: its `tag` and its `attributes`. */
internal fun fixtureGlyphShape(value: FixtureValue): GlyphShape {
    val written = value as? FixtureValue.Object ?: throw AssertionError("a shape is not an object")
    val attributes =
        written.objectOrNull("attributes")?.entries.orEmpty().associate { (name, attribute) ->
            name to
                (
                    (attribute as? FixtureValue.Text)?.value
                        ?: throw AssertionError("the attribute `$name` is not a string")
                )
        }
    return GlyphShape(written.text("tag"), attributes)
}

/** [shape] as the fixtures write one, its attributes in the order of their names, which the comparison does not read. */
internal fun writtenGlyphShape(shape: GlyphShape): FixtureValue =
    FixtureValue.Object(
        listOf(
            "attributes" to
                FixtureValue.Object(
                    shape.attributes.entries.sortedBy { it.key }.map {
                        it.key to
                            FixtureValue.Text(it.value)
                    },
                ),
            "tag" to FixtureValue.Text(shape.tag),
        ),
    )

/** [paint] as the fixtures write a shape's paint, the current colour as `current`. */
internal fun writtenShapePaint(paint: ShapePaint): FixtureValue =
    FixtureValue.Object(
        listOf(
            "fill" to FixtureValue.Text(if (paint.isFilled) "current" else "none"),
            "fillRule" to FixtureValue.Text(paint.fillRule.id),
            "lineCap" to FixtureValue.Text(paint.lineCap.id),
            "lineJoin" to FixtureValue.Text(paint.lineJoin.id),
            "miterLimit" to FixtureValue.Number(paint.miterLimit),
            "stroke" to FixtureValue.Text(if (paint.isStroked) "current" else "none"),
            "strokeWidth" to FixtureValue.Number(paint.strokeWidth),
        ),
    )
