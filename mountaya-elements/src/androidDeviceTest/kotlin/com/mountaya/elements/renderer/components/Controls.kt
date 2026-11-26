package com.mountaya.elements

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.geo.CameraPose
import kotlin.test.assertNotNull

/**
 * Controls draws the elements' composables a test reads the pixels of, each under the light or the dark scheme, and
 * builds the map engine a control drives with no renderer and no network.
 */
internal object Controls {
    /**
     * Draws [content] in a box tagged [tag], under the dark scheme where [dark] says so and the light one otherwise, as
     * the platform's configuration states it to the composition.
     */
    @Composable
    fun Drawn(
        tag: String,
        dark: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        val configuration = LocalConfiguration.current
        val schemed =
            remember(configuration, dark) {
                Configuration(configuration).apply {
                    val night = if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
                }
            }
        CompositionLocalProvider(LocalConfiguration provides schemed) { Box(Modifier.testTag(tag)) { content() } }
    }

    /** The node tagged [tag] as drawn on the screen. */
    fun ComposeContentTestRule.image(tag: String): Bitmap = onNodeWithTag(tag).captureToImage().asAndroidBitmap()

    /** The pixel of [image] under ([x], [y]) in density-independent pixels. */
    fun ComposeContentTestRule.pixel(
        image: Bitmap,
        x: Double,
        y: Double,
    ): Pixels.Pixel =
        assertNotNull(Pixels.pixel(image, x, y, density.density.toDouble()), "($x, $y) is outside the image")

    /**
     * The colour a control's glyph is drawn in: the pixel of the face's middle row, 24 density-independent pixels down,
     * that stands farthest from the white ground, a stroke's middle, which no edge blends.
     */
    fun ComposeContentTestRule.ink(image: Bitmap): Pixels.Pixel {
        var farthest: Pixels.Pixel? = null
        var distance = -1
        var x = 10.0
        while (x <= 38.0) {
            val pixel = pixel(image, x, 24.0)
            val away = (255 - pixel.red) + (255 - pixel.green) + (255 - pixel.blue)
            if (away > distance) {
                farthest = pixel
                distance = away
            }
            x += 1.0 / 3.0
        }
        return assertNotNull(farthest, "a face's middle row holds a pixel")
    }

    /** The action a control's press reads out, as accessibility services read it, for the control named [label]. */
    fun ComposeContentTestRule.help(label: String): String? =
        onNodeWithContentDescription(label).fetchSemanticsNode().config[SemanticsActions.OnClick].label

    /** The pixel a CSS hexadecimal colour, `#rrggbb`, is drawn as, opaque. */
    fun color(hex: String): Pixels.Pixel {
        val value = hex.removePrefix("#").toInt(16)
        return Pixels.Pixel(value shr 16 and 0xff, value shr 8 and 0xff, value and 0xff, 255)
    }

    /** A map a control drives, seen from [camera], locating through [geolocation], with no renderer and no network. */
    fun engine(
        camera: CameraPose = CameraPose(listOf(6.86, 45.83), 12.0, 0.0, 0.0),
        geolocation: Geolocation = InertGeolocation(),
    ): MapEngine =
        MapEngine(Mountaya(Credential.Publishable("pk_test_controls")), camera = camera, geolocation = geolocation)

    /** Located is a geolocation of a test's own: a fix it answers at once, and a standing permission. */
    class Located(
        private val fix: GeolocationFix,
        private val standing: GeolocationPermission,
    ) : Geolocation {
        /** How many fixes were asked for. */
        @Volatile
        var asked = 0
            private set

        override suspend fun locate(): GeolocationFix {
            asked += 1
            return fix
        }

        override suspend fun permission(): GeolocationPermission = standing
    }
}
