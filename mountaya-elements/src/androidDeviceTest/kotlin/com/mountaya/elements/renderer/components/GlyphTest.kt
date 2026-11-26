package com.mountaya.elements

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.GlyphSet
import com.mountaya.geo.ThemeScheme
import com.mountaya.geo.generated.ElementTokens
import org.junit.Rule
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The `Glyph` composable, drawn on the device over a white ground of the test's own. */
class GlyphTest {
    @get:Rule
    val compose = createComposeRule()

    /** A white ground behind the glyph, so a pixel it leaves is told apart from one it draws. */
    private val white = Modifier.drawBehind { drawRect(Color.White) }

    /** The node tagged [tag] as drawn on the screen, and the screen's density. */
    private fun captured(tag: String): Pair<Bitmap, Float> {
        val image = compose.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
        return image to compose.density.density
    }

    /** The pixel of [image] under ([x], [y]) in density-independent pixels, as ARGB. */
    private fun at(
        image: Pair<Bitmap, Float>,
        x: Float,
        y: Float,
    ): Int = image.first.getPixel((x * image.second).toInt(), (y * image.second).toInt())

    @Test
    fun aControlsGlyphIsDrawnFromTheControlSetInItsColourAtItsSize() {
        compose.setContent { Glyph("zoom-in", Modifier.testTag("glyph").then(white), size = 24.dp, color = Color.Red) }

        compose.onNodeWithTag("glyph").assertWidthIsEqualTo(24.dp).assertHeightIsEqualTo(24.dp)
        val image = captured("glyph")
        // The plus: its bars cross at the centre, and the corners stay the ground's.
        val centre = at(image, 12f, 12f)
        assertTrue(
            centre shr 16 and 0xff > 200 && centre shr 8 and 0xff < 120 && centre and 0xff < 120,
            Integer.toHexString(centre),
        )
        assertEquals(0xffffffff.toInt(), at(image, 2f, 2f))
    }

    /**
     * Handed no colour, a glyph is drawn in the controls' ink, the `control-fg` token under the scheme in force, so
     * under a dark scope in the dark one, where it was black whatever the scheme.
     */
    @Test
    fun aGlyphHandedNoColourIsDrawnInControlFgUnderTheSchemeInForce() {
        compose.setContent {
            Column {
                Glyph("zoom-in", Modifier.testTag("light").then(white), size = 24.dp)
                MountayaScope(scheme = ThemeScheme.Dark) {
                    Glyph("zoom-in", Modifier.testTag("dark").then(white), size = 24.dp)
                }
            }
        }

        for ((tag, scheme) in listOf("light" to ThemeScheme.Light, "dark" to ThemeScheme.Dark)) {
            val ink = tokenColor(ElementTokens.standard.controlFg, scheme).toArgb()
            val centre = at(captured(tag), 12f, 12f)
            for (shift in listOf(16, 8, 0)) {
                val drawn = centre shr shift and 0xff
                val expected = ink shr shift and 0xff
                assertTrue(
                    abs(drawn - expected) <= 2,
                    "$tag: ${Integer.toHexString(centre)} is not ${Integer.toHexString(ink)}",
                )
            }
        }
    }

    @Test
    fun aGlyphOfTheSetItIsHandedScaledFromTheSetsBoxToItsSize() {
        val block =
            assertNotNull(
                GlyphSet.of(
                    "block",
                    GlyphSet.Paint.Fill,
                    viewBox = 8.0,
                    markup =
                        mapOf("half" to """<rect width="8" height="4"/>"""),
                ),
                "the block set is a set",
            )
        compose.setContent {
            Glyph("half", Modifier.testTag("glyph").then(white), set = block, size = 16.dp, color = Color.Black)
        }

        val image = captured("glyph")
        assertEquals(0xff000000.toInt(), at(image, 8f, 4f))
        assertEquals(0xffffffff.toInt(), at(image, 8f, 12f))
    }

    @Test
    fun aGlyphHandedNoSetIsDrawnFromTheScopesControlSetWhichAHostRestylesOnce() {
        val block =
            assertNotNull(
                GlyphSet.of(
                    "block",
                    GlyphSet.Paint.Fill,
                    viewBox = 8.0,
                    markup = mapOf("zoom-in" to """<rect width="8" height="4"/>"""),
                ),
                "the block set is a set",
            )
        compose.setContent {
            MountayaScope(ElementsScopeStatement(controlGlyphs = block)) {
                Glyph("zoom-in", Modifier.testTag("scoped").then(white), size = 16.dp, color = Color.Black)
            }
        }

        val image = captured("scoped")
        // The scope's block fills the glyph's top half, where the bundled plus leaves a corner bare.
        assertEquals(0xff000000.toInt(), at(image, 1f, 1f))
        assertEquals(0xffffffff.toInt(), at(image, 8f, 12f))
    }

    @Test
    fun aGlyphTheSetDoesNotHoldIsDrawnAsNothingAndTakesNoRoom() {
        compose.setContent {
            Glyph("hut", Modifier.testTag("control"))
            Glyph("hut", Modifier.testTag("marker"), set = GlyphSet.marker, size = 24.dp)
        }

        compose.onNodeWithTag("control").assertDoesNotExist()
        compose.onNodeWithTag("marker").assertWidthIsEqualTo(24.dp)
    }
}
