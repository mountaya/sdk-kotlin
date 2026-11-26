package com.mountaya.geo

import com.mountaya.geo.generated.ElementTokens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GlyphDrawingTest {
    @Test
    fun theCompassRoseFollowsTheSchemeInItsDiscAndLetterAndFixesItsNeedlesColour() {
        val rose = assertNotNull(GlyphDrawing.artwork["compass-rose"], "the glyph table draws `compass-rose`")

        assertEquals(listOf(0.0, 0.0, 32.0, 32.0), rose.viewBox)
        assertEquals(4, rose.shapes.size)
        assertEquals(GlyphDrawing.Paint.Token(ElementTokens::controlBg, opacity = 1.0), rose.shapes.first().fill)
        assertEquals(GlyphDrawing.Paint.Color(HexColor("#e9501e")), rose.shapes[1].fill)
        assertEquals(GlyphDrawing.Paint.Token(ElementTokens::controlFg, opacity = 0.45), rose.shapes.last().fill)
        assertTrue(rose.shapes.all { it.stroke == null && it.strokeWidth == null })
    }

    @Test
    fun theProfilesMarkerIsFilledWhiteAndStrokedInATranslucentBlackOneUnitWide() {
        val marker = assertNotNull(GlyphDrawing.artwork["profile-marker"], "the glyph table draws `profile-marker`")

        assertEquals(listOf(0.0, 0.0, 24.0, 33.0), marker.viewBox)
        assertEquals(GlyphDrawing.Paint.Color(HexColor("#ffffff")), marker.shapes.first().fill)
        assertEquals(GlyphDrawing.Paint.Color(HexColor("#000000", opacity = 0.25)), marker.shapes.first().stroke)
        assertEquals(1.0, marker.shapes.first().strokeWidth)
    }

    @Test
    fun everyPrintedShapeIsMarkupTheGlyphReaderReadsIntoShapes() {
        assertEquals(setOf("compass-rose", "profile-marker", "watermark"), GlyphDrawing.artwork.keys)
        for ((name, drawing) in GlyphDrawing.artwork) {
            for (shape in drawing.shapes) assertTrue(parseGlyph(shape.markup).isNotEmpty(), name)
        }
    }

    @Test
    fun theWatermarkIsLaidOverTheMapAtItsOpacityAboveAShadowItsMarkFilledWithGradients() {
        val watermark = assertNotNull(GlyphDrawing.artwork["watermark"], "the glyph table draws `watermark`")

        assertEquals(listOf(0.0, 0.0, 138.0, 58.0), watermark.viewBox)
        assertEquals(0.9, watermark.opacity)
        assertEquals(GlyphDrawing.Shadow(12.1452, 0.0, 0.0, HexColor("#000000", opacity = 0.55)), watermark.shadow)
        val gradient =
            assertNotNull(
                watermark.shapes.first().fill as? GlyphDrawing.Paint.LinearGradient,
                "the first shape is filled by a linear gradient",
            )
        assertEquals(listOf(0.0, 1.0), gradient.stops.map { it.offset })
        assertEquals(GlyphDrawing.Paint.Color(HexColor("#ffffff")), watermark.shapes.last().fill)
    }

    @Test
    fun twoGradientsAndTwoDrawingsAreOneWhereEveryMemberIs() {
        fun gradient(end: Double) =
            GlyphDrawing.Paint.LinearGradient(
                listOf(0.0, 0.0),
                listOf(end, 1.0),
                listOf(
                    GlyphDrawing.GradientStop(0.0, HexColor("#000000")),
                    GlyphDrawing.GradientStop(1.0, HexColor("#ffffff")),
                ),
            )
        val box = listOf(0.0, 0.0, 2.0, 2.0)

        assertEquals(gradient(1.0), gradient(1.0))
        assertEquals(gradient(1.0).hashCode(), gradient(1.0).hashCode())
        assertNotEquals(gradient(1.0), gradient(2.0))
        assertNotEquals(GlyphDrawing(box, emptyList(), opacity = 0.9), GlyphDrawing(box, emptyList()))
        assertNotEquals(
            GlyphDrawing(box, emptyList(), shadow = GlyphDrawing.Shadow(1.0, 0.0, 0.0, HexColor("#000000"))),
            GlyphDrawing(box, emptyList()),
        )
    }

    @Test
    fun twoPaintsAreOneWhereTheyNameTheSameColourOrTheSameTokenAtOneOpacity() {
        val accent = GlyphDrawing.Paint.Token(ElementTokens::accent, opacity = 1.0)

        assertEquals(GlyphDrawing.Paint.Token(ElementTokens::accent, opacity = 1.0), accent)
        assertEquals(GlyphDrawing.Paint.Token(ElementTokens::accent, opacity = 1.0).hashCode(), accent.hashCode())
        assertNotEquals<GlyphDrawing.Paint>(GlyphDrawing.Paint.Token(ElementTokens::accent, opacity = 0.5), accent)
        assertNotEquals<GlyphDrawing.Paint>(GlyphDrawing.Paint.Token(ElementTokens::controlBg, opacity = 1.0), accent)
        assertNotEquals<GlyphDrawing.Paint>(
            GlyphDrawing.Paint.Color(HexColor("#ffffff")),
            GlyphDrawing.Paint.Token(ElementTokens::panelBg, opacity = 1.0),
        )
        assertEquals(HexColor("#1c7ed6"), accent.token.get(ElementTokens.standard).light)
    }

    @Test
    fun twoShapesAreOneWhereTheirStrokesAreTheSameNumberNanAsNanAnd0ApartFromMinus0() {
        fun shape(width: Double?): GlyphDrawing.PaintedShape =
            GlyphDrawing.PaintedShape(
                markup = "<circle cx=\"12\" cy=\"12\" r=\"10\" />",
                stroke = GlyphDrawing.Paint.Color(HexColor("#000000")),
                strokeWidth = width,
            )

        assertEquals(shape(1.0), shape(1.0))
        assertEquals(shape(Double.NaN), shape(Double.NaN))
        assertEquals(shape(Double.NaN).hashCode(), shape(Double.NaN).hashCode())
        assertNotEquals(shape(0.0), shape(-0.0))
        assertEquals(shape(null), shape(null))
        assertNotEquals(shape(null), shape(0.0))

        val box = listOf(0.0, 0.0, 24.0, 24.0)
        assertEquals(GlyphDrawing(box, listOf(shape(Double.NaN))), GlyphDrawing(box, listOf(shape(Double.NaN))))
        assertNotEquals(GlyphDrawing(box, listOf(shape(0.0))), GlyphDrawing(box, listOf(shape(-0.0))))
    }
}
