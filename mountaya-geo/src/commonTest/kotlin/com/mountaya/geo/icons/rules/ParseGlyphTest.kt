package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParseGlyphTest {
    private fun shape(
        tag: String,
        vararg attributes: Pair<String, String>,
    ) = GlyphShape(tag, mapOf(*attributes))

    @Test
    fun aShapeKeepsTheAttributesOnTheListQuotedEitherWay() {
        assertEquals(listOf(shape("path", "d" to "M5 12h14")), parseGlyph("""<path d="M5 12h14" />"""))
        assertEquals(
            listOf(shape("circle", "cx" to "12", "cy" to "12", "r" to "10")),
            parseGlyph("""<circle cx='12' cy="12" r = '10' />"""),
        )
        assertEquals(
            listOf(shape("circle", "cx" to "1", "cy" to "2", "r" to "3")),
            parseGlyph("""<circle cx="1"cy='2'r="3"/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0h1"), shape("path", "d" to "M1 1h1")),
            parseGlyph("""<path id='a"b' d="M0 0h1" /><path id="a>b" d='M1 1h1' />"""),
        )
    }

    @Test
    fun anAttributeOffTheListIsDropped() {
        assertEquals(
            listOf(shape("path", "d" to "M5 12h14")),
            parseGlyph("""<path d="M5 12h14" onload="alert(1)" />"""),
        )
        assertEquals(
            listOf(shape("circle", "cx" to "1", "cy" to "2", "r" to "3")),
            parseGlyph("""<circle cx="1" cy="2" r="3" style="fill:red" />"""),
        )
        assertEquals(
            listOf(shape("rect", "width" to "4", "height" to "4")),
            parseGlyph("""<rect width="4" height="4" sodipodi:rx="9" xlink:x="3" />"""),
        )
        assertEquals(
            listOf(shape("rect", "width" to "4")),
            parseGlyph("""<rect width="4" 1:rx="9" -x="3" .y="2" 9d="M0 0" />"""),
        )
        // A name with no quoted value is skipped whole, and a name written twice keeps its last value.
        assertEquals(
            listOf(shape("rect", "width" to "2", "height" to "3")),
            parseGlyph("""<rect width=1 height="3" width="2" />"""),
        )
    }

    @Test
    fun aFillIsKeptOnlyAsTheCurrentColourOrNone() {
        assertEquals(
            listOf(shape("circle", "r" to "1", "fill" to "CURRENTCOLOR")),
            parseGlyph("""<circle r="1" FILL='CURRENTCOLOR' />"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0", "fill" to "none")),
            parseGlyph("""<path d="M0 0" fill=" none " />"""),
        )
        assertEquals(listOf(shape("path", "d" to "M0 0")), parseGlyph("""<path d="M0 0" fill="#ff0000" />"""))
        assertEquals(listOf(shape("path", "d" to "M0 0")), parseGlyph("""<path d="M0 0" fill="url(#a)" />"""))
    }

    @Test
    fun aGroupsTransformIsFoldedOutermostFirstAndBeforeTheShapesOwn() {
        assertEquals(
            listOf(shape("path", "d" to "M0 0h1", "transform" to "translate(2 2) scale(3)")),
            parseGlyph("""<g transform="translate(2 2)"><path d="M0 0h1" transform="scale(3)" /></g>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0h1", "transform" to "translate(2 2) scale(3)")),
            parseGlyph("""<g transform="translate(2 2)"><g transform="scale(3)"><path d="M0 0h1" /></g></g>"""),
        )
        assertEquals(listOf(shape("path", "d" to "M5 12h14")), parseGlyph("""<g><path d="M5 12h14" /></g>"""))
        assertEquals(
            listOf(shape("path", "d" to "M5 12h14")),
            parseGlyph("""<g transform="scale(3)" /><path d="M5 12h14" />"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M5 12h14")),
            parseGlyph("""<svg viewBox="0 0 24 24"><path d="M5 12h14" /></svg>"""),
        )
    }

    @Test
    fun anElementThatIsNoShapeIsDroppedWithAllItHolds() {
        assertEquals(
            listOf(shape("path", "d" to "M5 12h14")),
            parseGlyph("""<script>alert(1)</script><path d="M5 12h14" />"""),
        )
        assertEquals(emptyList(), parseGlyph("""<image href="https://example.invalid/pixel.gif" />"""))
        assertEquals(emptyList(), parseGlyph("""<foreignObject><div>hi</div></foreignObject>"""))
        assertEquals(
            listOf(shape("path", "d" to "M0 0")),
            parseGlyph("""<defs><defs><rect width="1" /></defs><rect width="2" /></defs><path d="M0 0" />"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0"), shape("rect", "width" to "2")),
            parseGlyph("""<path d="M0 0"><circle r="1" /></path><rect width="2" />"""),
        )
    }

    /** Never closed, an element holds nothing: dropped alone, a shape kept alone, a group handing on nothing. */
    @Test
    fun anElementTheMarkupNeverClosesHoldsNothing() {
        assertEquals(
            listOf(shape("path", "d" to "M0 0"), shape("circle", "r" to "1")),
            parseGlyph("""<path d="M0 0"><circle r="1" />"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0"), shape("circle", "r" to "3")),
            parseGlyph("""<path d="M0 0" /><img src="x" onerror="alert(1)"><circle r="3" />"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0")),
            parseGlyph("""<g transform="translate(2 2)"><path d="M0 0" />"""),
        )
        assertEquals(
            listOf(
                shape("path", "d" to "M0 0"),
                shape("circle", "r" to "1", "transform" to "rotate(5)"),
                shape(
                    "rect",
                    "width" to "2",
                ),
            ),
            parseGlyph(
                """<g transform="scale(2)"><path d="M0 0" /><g transform="rotate(5)"><circle r="1" /></g><rect width="2" />""",
            ),
        )
    }

    @Test
    fun aClosingGroupTagEndsTheGroupItIsPairedWithWhereverItIsWritten() {
        assertEquals(
            listOf(shape("path", "d" to "M0 0")),
            parseGlyph("""<g transform="scale(2)"><mask></g></mask><path d="M0 0"/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M1 1", "transform" to "scale(2)"), shape("circle", "r" to "1")),
            parseGlyph("""<g transform="scale(2)"><path d="M1 1"></g></path><circle r="1"/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0", "transform" to "rotate(9)")),
            parseGlyph("""<g transform="rotate(9)"><mask><g></mask></g><path d="M0 0"/></g>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0")),
            parseGlyph("""<g transform="rotate(9)"></g></g><path d="M0 0"/>"""),
        )
    }

    @Test
    fun aCommentOrACdataSectionIsWhiteSpace() {
        assertEquals(
            listOf(shape("path", "d" to "M0 0")),
            parseGlyph("""<!-- <circle r="1" /> --><path d="M0 0" /><![CDATA[<rect width="1" />]]>"""),
        )
        assertEquals(listOf(shape("path", "d" to "M0 0")), parseGlyph("""<path d="M0 0" /><!-- <path d="M1 1" />"""))
        assertEquals(emptyList(), parseGlyph("""<pa<!-- -->th d="M0 0"/>"""))
        assertEquals(emptyList(), parseGlyph("""<<!-- -->path d="M0 0"/>"""))
        assertEquals(
            listOf(shape("path", "d" to "M0 0"), shape("circle", "r" to "1")),
            parseGlyph("""<path<!-- a -->d="M0 0"<!-- b -->/><!-- c --><circle r="1"/>"""),
        )
    }

    @Test
    fun aTagTheMarkupNeverEndsIsNoTag() {
        assertEquals(listOf(shape("path", "d" to "M0 0")), parseGlyph("""<a "<path d="M0 0" />"""))
        assertEquals(listOf(shape("circle", "r" to "1")), parseGlyph("""<circle r="1" /><path d="M0 0"""))
        assertEquals(emptyList(), parseGlyph("< path d=\"M0 0\" />"))
        assertEquals(emptyList(), parseGlyph(""))
        // A long run of tags that never end is read in one pass.
        assertEquals(emptyList(), parseGlyph("<a \"".repeat(20_000)))
    }

    @Test
    fun aStrokeIsKeptOnlyAsTheCurrentColourOrNoneAndNeverAStrokesWidth() {
        assertEquals(
            listOf(
                shape("path", "d" to "M0 0h1", "stroke" to "CurrentColor"),
                shape(
                    "circle",
                    "r" to "1",
                    "stroke" to "NONE",
                ),
            ),
            parseGlyph("""<path d="M0 0h1" stroke="CurrentColor"/><circle r="1" stroke=" NONE "/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0h1"), shape("path", "d" to "M1 1h1")),
            parseGlyph("""<path d="M0 0h1" stroke="#e9501e"/><path d="M1 1h1" stroke="url(#paint)"/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0h1", "stroke" to "currentColor")),
            parseGlyph("""<path d="M0 0h1" stroke="currentColor" stroke-width="4" STROKE-WIDTH="1"/>"""),
        )
        // A tab and a line feed are white space around `=` and at a keyword's ends.
        assertEquals(
            listOf(shape("path", "d" to "M0 0", "stroke" to "none")),
            parseGlyph("<path d=\"M0 0\" stroke\t=\n\"\tnone\n\"/>"),
        )
    }

    @Test
    fun aCapAndAJoinAreKeptOnlyAsTheKeywordsSvgNamesThemWithInAnyCase() {
        assertEquals(
            listOf(shape("path", "d" to "M0 0h1", "stroke-linecap" to "Square", "stroke-linejoin" to "bevel")),
            parseGlyph("""<path d="M0 0h1" stroke-linecap="Square" stroke-linejoin=" bevel "/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0h1", "stroke-linecap" to "BUTT", "stroke-linejoin" to "Miter-Clip")),
            parseGlyph("""<path d="M0 0h1" stroke-linecap="BUTT" stroke-linejoin="Miter-Clip"/>"""),
        )
        assertEquals(
            listOf(
                shape("path", "d" to "M0 0h1", "stroke-linejoin" to "arcs"),
                shape("path", "d" to "M0 0", "stroke-linejoin" to "round"),
            ),
            parseGlyph("""<path d="M0 0h1" stroke-linejoin="arcs"/><path d="M0 0" stroke-linejoin="round"/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M1 1h1")),
            parseGlyph("""<path d="M1 1h1" stroke-linecap="flat" stroke-linejoin="sharp"/>"""),
        )
        // A letter outside ASCII is no keyword's, whatever a full case mapping folds it to.
        assertEquals(listOf(shape("path", "d" to "M0 0")), parseGlyph("<path d=\"M0 0\" stroke-linejoin=\"mİter\"/>"))
    }

    @Test
    fun aMiterLimitIsKeptOnlyAsAPlainNumberOfAtLeast1AsWritten() {
        fun limit(written: String): String? =
            parseGlyph(
                "<path d=\"M0 0\" stroke-miterlimit=\"$written\"/>",
            ).firstOrNull()?.attributes?.get("stroke-miterlimit")

        assertEquals("1", limit("1"))
        assertEquals("4.5", limit(" 4.5 "))
        assertEquals("4.", limit("4."))
        assertEquals("10", limit("10"))
        assertEquals("007", limit("007"))
        assertEquals("1.0", limit("1.0"))
        for (dropped in listOf("0.5", ".5", "0", "10px", "+10", "-1", "1e1", "1.5.5", ".", "", "inf", "0x10")) {
            assertEquals(null, limit(dropped), dropped)
        }
        // A digit of another script is no ASCII digit.
        assertEquals(null, limit("٤"))
    }

    @Test
    fun anAttributeWrittenTwiceKeepsTheLaterOfItsValuesAGlyphMayCarry() {
        assertEquals(listOf(shape("path", "d" to "M5 5h1")), parseGlyph("""<path d="M0 0h1" d="M5 5h1"/>"""))
        assertEquals(
            listOf(shape("path", "d" to "M0 0", "stroke" to "currentColor")),
            parseGlyph("""<path d="M0 0" stroke="none" stroke="currentColor"/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0", "stroke" to "none")),
            parseGlyph("""<path d="M0 0" stroke="none" stroke="#fff"/>"""),
        )
        assertEquals(
            listOf(shape("path", "d" to "M0 0", "stroke-miterlimit" to "8")),
            parseGlyph("""<path d="M0 0" stroke-miterlimit="8" stroke-miterlimit="0.5"/>"""),
        )
    }

    @Test
    fun aTagWhoseNameCarriesANamespacePrefixReadsAsThePrefix() {
        assertEquals(emptyList(), parseGlyph("""<svg:path d="M0 0h1"/>"""))
        assertEquals(
            listOf(shape("path", "d" to "M0 0")),
            parseGlyph("""<svg:g transform="scale(2)"><path d="M0 0"/></svg:g>"""),
        )
        assertEquals(listOf(shape("path", "d" to "M0 0")), parseGlyph("""<dc:title>hut</dc:title><path d="M0 0"/>"""))
    }

    @Test
    fun aValueTheParseKeepsIsTheOneKeptGlyphValueKeepsWhoeverBuiltTheShape() {
        assertEquals(" M0 0 ", keptGlyphValue("d", " M0 0 "))
        assertEquals("currentColor", keptGlyphValue("fill", " currentColor "))
        assertEquals(null, keptGlyphValue("stroke", "red"))
        assertEquals("round", keptGlyphValue("stroke-linecap", "round"))
        assertEquals("4", keptGlyphValue("stroke-miterlimit", "4"))
        assertEquals(null, keptGlyphValue("stroke-miterlimit", "0.99"))
    }

    @Test
    fun theMarkupIsReadCharacterByCharacter() {
        assertEquals(listOf(shape("circle", "cx" to "1", "r" to "2")), parseGlyph("<circle\r\ncx=\"1\"\r\nr=\"2\"/>"))
        assertEquals(listOf(shape("path", "d" to "M0 0́")), parseGlyph("<path d=\"M0 0́\" />"))
    }

    @Test
    fun theListsAreTheOnesARendererCovers() {
        assertEquals(23, glyphAttributes.size)
        assertTrue(
            glyphAttributes.containsAll(listOf("stroke", "stroke-linecap", "stroke-linejoin", "stroke-miterlimit")),
        )
        assertFalse("stroke-width" in glyphAttributes)
        assertEquals(setOf("circle", "ellipse", "line", "path", "polygon", "polyline", "rect"), glyphShapeTags)
        // Each shape is read as a shape when it is written alone.
        for (tag in glyphShapeTags) assertEquals(listOf(shape(tag)), parseGlyph("<$tag/>"), tag)
    }
}
