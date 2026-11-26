package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GlyphSetTest {
    private fun built(
        name: String,
        paint: GlyphSet.Paint,
        strokeWidth: Double = 2.0,
        viewBox: Double = 24.0,
        markup: Map<String, String> = emptyMap(),
    ): GlyphSet = assertNotNull(GlyphSet.of(name, paint, strokeWidth, viewBox, markup), "$name is refused")

    @Test
    fun bothPrintedSetsStrokeTheirGlyphs2UnitsWideInA24UnitBoxTheWholeTableEach() {
        for (set in listOf(GlyphSet.marker, GlyphSet.control)) {
            assertEquals(GlyphSet.Paint.Stroke, set.paint)
            assertEquals(2.0, set.strokeWidth)
            assertEquals(24.0, set.viewBox)
        }
        assertEquals("marker", GlyphSet.marker.name)
        assertEquals("control", GlyphSet.control.name)
        assertEquals(82, GlyphSet.marker.markup.size)
        assertEquals(166, GlyphSet.control.markup.size)
        assertEquals("<path d=\"M5 12h14\" /><path d=\"M12 5v14\" />", GlyphSet.control.markup["zoom-in"])
        assertEquals(listOf("fill", "stroke"), GlyphSet.Paint.entries.map { it.id })
    }

    @Test
    fun everyPrintedGlyphIsMarkupTheGlyphReaderReadsIntoShapes() {
        for ((name, markup) in GlyphSet.marker.markup + GlyphSet.control.markup) {
            assertTrue(parseGlyph(markup).isNotEmpty(), name)
        }
    }

    @Test
    fun everyGlyphGeosRulesNameStandsInTheMarkerSet() {
        val marker = GlyphSet.marker.markup

        for (amenity in waypointAmenities) assertTrue(amenityGlyph(amenity) in marker, amenity.value)
        for (kind in RoadAccessKind.entries) assertTrue(roadAccessGlyph(kind) in marker, kind.id)
        assertTrue(WAYPOINT_FALLBACK_GLYPH in marker)
    }

    @Test
    fun aSetAHostBuildsHoldsWhatItIsGivenABoxOf24AndAWidthOf2UnlessStated() {
        val solid = built("solid", GlyphSet.Paint.Fill, markup = mapOf("dot" to "<circle r=\"1\" />"))
        val grid = built("grid", GlyphSet.Paint.Stroke, strokeWidth = 1.5, viewBox = 16.0)

        assertEquals("solid", solid.name)
        assertEquals(GlyphSet.Paint.Fill, solid.paint)
        assertEquals(24.0, solid.viewBox)
        assertEquals(2.0, solid.strokeWidth)
        assertEquals("<circle r=\"1\" />", solid.markup["dot"])
        assertEquals(16.0 to 1.5, grid.viewBox to grid.strokeWidth)
        assertNotEquals(GlyphSet.marker, solid)
        assertEquals(2.0, GlyphSet.of("outline", GlyphSet.Paint.Stroke, markup = emptyMap())?.strokeWidth)
        // Equal where the names are and the same markup is drawn alike.
        assertEquals(built("solid", GlyphSet.Paint.Fill, markup = solid.markup), solid)
        assertEquals(built("solid", GlyphSet.Paint.Fill, markup = solid.markup).hashCode(), solid.hashCode())
        assertNotEquals(built("other", GlyphSet.Paint.Fill, markup = solid.markup), solid)
        assertNotEquals(GlyphSet.control, GlyphSet.marker)
    }

    @Test
    fun aSetKeepsACopyOfTheMarkupItIsGiven() {
        val markup = mutableMapOf("dot" to "<circle r=\"1\" />")
        val set = built("solid", GlyphSet.Paint.Fill, markup = markup)
        markup["hut"] = "<path d=\"M0 0h1\"/>"

        assertEquals(setOf("dot"), set.markup.keys)
    }

    @Test
    fun aFillSetOfNoWidthIsDrawnAStrokeSetOfNoneIsRefused() {
        assertEquals(
            0.0,
            GlyphSet.of("solid", GlyphSet.Paint.Fill, strokeWidth = 0.0, markup = emptyMap())?.strokeWidth,
        )
        assertNotNull(
            GlyphSet.of("solid", GlyphSet.Paint.Fill, strokeWidth = -0.0, markup = emptyMap()),
            "the set is built",
        )
        assertNull(GlyphSet.of("outline", GlyphSet.Paint.Stroke, strokeWidth = 0.0, markup = emptyMap()))
        assertNotNull(
            GlyphSet.of("outline", GlyphSet.Paint.Stroke, strokeWidth = 0.01, markup = emptyMap()),
            "the set is built",
        )
    }

    @Test
    fun aSetIsRefusedWhereItsNameIsEmptyItsBoxOrItsWidthCannotBeDrawn() {
        assertNull(GlyphSet.of("", GlyphSet.Paint.Fill, markup = emptyMap()))
        assertNotNull(GlyphSet.of(" ", GlyphSet.Paint.Fill, markup = emptyMap()), "the set is built")
        assertNull(GlyphSet.of("solid", GlyphSet.Paint.Fill, strokeWidth = -1.0, markup = emptyMap()))
        assertNull(GlyphSet.of("outline", GlyphSet.Paint.Stroke, strokeWidth = -1.0, markup = emptyMap()))
        for (width in listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN)) {
            assertNull(GlyphSet.of("solid", GlyphSet.Paint.Fill, strokeWidth = width, markup = emptyMap()), "$width")
            assertNull(
                GlyphSet.of("outline", GlyphSet.Paint.Stroke, strokeWidth = width, markup = emptyMap()),
                "$width",
            )
        }
        for (box in listOf(0.0, -24.0, Double.POSITIVE_INFINITY, Double.NaN, -0.0)) {
            assertNull(GlyphSet.of("grid", GlyphSet.Paint.Stroke, viewBox = box, markup = emptyMap()), "$box")
        }
    }

    @Test
    fun aChainAnswersEachGlyphFromTheFirstSetThatHoldsItDrawnAsTheFirstSetIs() {
        val partial = built("partial", GlyphSet.Paint.Stroke, markup = mapOf("mountain" to "<path d=\"M1 1h1\"/>"))
        val rest =
            built(
                "rest",
                GlyphSet.Paint.Stroke,
                markup = mapOf("hut" to "<path d=\"M2 2h1\"/>", "mountain" to "<path d=\"M2 2h1\"/>"),
            )
        val chain = assertNotNull(GlyphSet.chaining(listOf(partial, rest)), "the partial set chains before the rest")

        assertEquals("<path d=\"M1 1h1\"/>", chain.markup["mountain"])
        assertEquals("<path d=\"M2 2h1\"/>", chain.markup["hut"])
        assertNull(chain.markup["x"])
        assertEquals(listOf("hut", "mountain"), chain.markup.keys.sorted())
        assertEquals("partial+rest", chain.name)
        assertEquals(Triple(GlyphSet.Paint.Stroke, 24.0, 2.0), Triple(chain.paint, chain.viewBox, chain.strokeWidth))
        assertEquals(partial, GlyphSet.chaining(listOf(partial)))
        // A chain chains further as a set does, every later set held to the first.
        val longer = assertNotNull(GlyphSet.chaining(listOf(chain, GlyphSet.marker)), "a chain chains again")
        assertEquals("partial+rest+marker", longer.name)
        assertEquals("<path d=\"M2 2h1\"/>", longer.markup["hut"])
        assertEquals(GlyphSet.marker.markup["map-pin"], longer.markup["map-pin"])
    }

    @Test
    fun aFillChainIsDrawnInTheFirstSetsBoxAndWidthWhateverALaterSetsWidth() {
        val first = built("first", GlyphSet.Paint.Fill, viewBox = 16.0, markup = mapOf("a" to "<path d=\"M1 1h1\"/>"))
        val second =
            built(
                "second",
                GlyphSet.Paint.Fill,
                strokeWidth = 0.0,
                viewBox = 16.0,
                markup =
                    mapOf("b" to "<path d=\"M2 2h1\"/>"),
            )
        val chain = assertNotNull(GlyphSet.chaining(listOf(first, second)), "the two sets chain")

        assertEquals(Triple(GlyphSet.Paint.Fill, 16.0, 2.0), Triple(chain.paint, chain.viewBox, chain.strokeWidth))
        assertEquals(listOf("a", "b"), chain.markup.keys.sorted())
    }

    @Test
    fun aChainIsRefusedWhereALaterSetDiffersFromTheFirstAndWhereItChainsNone() {
        val solid = built("solid", GlyphSet.Paint.Fill)
        val outline = built("outline", GlyphSet.Paint.Stroke)
        val narrow = built("narrow", GlyphSet.Paint.Fill, viewBox = 16.0)
        val thin = built("thin", GlyphSet.Paint.Stroke, strokeWidth = 1.5)

        assertNull(GlyphSet.chaining(emptyList()))
        assertNull(GlyphSet.chaining(listOf(solid, outline)))
        assertNull(GlyphSet.chaining(listOf(solid, narrow)))
        assertNull(GlyphSet.chaining(listOf(outline, thin)))
        assertNull(GlyphSet.chaining(listOf(outline, GlyphSet.marker, thin)))
        assertNotNull(GlyphSet.chaining(listOf(outline, GlyphSet.marker, GlyphSet.control)), "the sets chain")
    }
}
