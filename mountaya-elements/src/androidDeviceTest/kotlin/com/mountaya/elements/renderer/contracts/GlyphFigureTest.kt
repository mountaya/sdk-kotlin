package com.mountaya.elements

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import com.mountaya.geo.GlyphSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A figure of a glyph, on the device whose paths it holds. */
class GlyphFigureTest {
    @Test
    fun aFigureKeepsItsOutlineApartFromItsTransformSoAStrokeScalesAsSvgScalesIt() {
        val set =
            assertNotNull(
                GlyphSet.of(
                    "test",
                    GlyphSet.Paint.Stroke,
                    markup =
                        mapOf("dash" to """<path d="M0 0h1" transform="scale(4)"/>"""),
                ),
                "the test set is a set",
            )
        val figure = assertNotNull(glyphFigures("dash", set)?.firstOrNull(), "the dash is drawn as a figure")

        assertEquals(Rect(0f, 0f, 1f, 0f), figure.path.getBounds())
        assertTrue(figure.transform.values.contentEquals(Matrix().apply { scale(4f, 4f) }.values))
        assertEquals(2.0, figure.paint.strokeWidth)
    }
}
