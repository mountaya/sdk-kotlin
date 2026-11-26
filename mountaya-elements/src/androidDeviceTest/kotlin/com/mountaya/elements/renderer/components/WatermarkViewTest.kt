package com.mountaya.elements

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.OrganizationRead
import com.mountaya.geo.watermarkOf
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** The watermark drawn natively on the device, from the contract's drawing. */
class WatermarkViewTest {
    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val mark =
        assertNotNull(watermarkOf(OrganizationRead.NoKey, RestSchema.Language.Fr), "a map with no key draws the mark")

    /** Whether [view], drawn as it stands, paints the mark's white letters. */
    private fun paintsTheMark(view: View): Boolean {
        val image = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(image))
        val pixels = IntArray(view.width * view.height)
        image.getPixels(pixels, 0, view.width, 0, 0, view.width, view.height)
        return pixels.any { (it ushr 24) > 200 && (it shr 16 and 0xff) > 200 }
    }

    @Test
    fun theMarkIsDrawnWhiteOverItsShadowAndAPressOpensItsLink() {
        val opened = mutableListOf<String>()
        val view = WatermarkView(context, mark, "Visiter Mountaya") { opened += it }
        val width = 360
        val height = (width * mark.height / mark.width).toInt()
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, width, height)
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(image))

        // The word's letters are white, laid at the drawing's opacity; the shadow darkens the margin round them.
        val pixels = IntArray(width * height).also { image.getPixels(it, 0, width, 0, 0, width, height) }
        assertTrue(pixels.any { (it ushr 24) > 200 && (it shr 16 and 0xff) > 200 }, "no white is drawn")
        assertTrue(pixels.any { (it ushr 24) in 1..200 && (it shr 16 and 0xff) < 60 }, "no shadow is drawn")
        assertEquals("Visiter Mountaya", view.contentDescription)
        view.performClick()
        assertEquals(listOf("https://www.mountaya.com/fr/"), opened)
    }

    /**
     * A mark taken off its window and laid again at the size it kept, as the renderer's layer may lay it, paints its
     * mark again: its size is told no change, so the image it let go of is drawn again as it comes back.
     */
    @Test
    fun aMarkTakenOffItsWindowAndLaidAgainStillPaintsItsMark() {
        var layer: FrameLayout? = null
        var shown: WatermarkView? = null
        compose.setContent {
            AndroidView(factory = { hosting ->
                FrameLayout(hosting).also { frame ->
                    val view = WatermarkView(hosting, mark, "Visiter Mountaya") {}
                    frame.addView(view, FrameLayout.LayoutParams(360, (360 * mark.height / mark.width).toInt()))
                    layer = frame
                    shown = view
                }
            })
        }
        compose.waitUntil(5_000L) {
            compose.runOnUiThread { shown.let { view -> view != null && view.isAttachedToWindow && view.width > 0 } }
        }

        compose.runOnUiThread {
            val frame = assertNotNull(layer, "the layer is composed")
            val view = assertNotNull(shown, "the mark is laid on the layer")
            assertTrue(paintsTheMark(view), "the mark is painted once laid")
            val params = view.layoutParams
            frame.removeView(view)
            frame.addView(view, 0, params)
            assertTrue(view.isAttachedToWindow, "the mark is laid again")
            assertTrue(paintsTheMark(view), "the mark is painted again at the size it kept")
        }
    }

    /**
     * The map's mark opens its link through the platform's own opener, an `ACTION_VIEW` intent on the link in a task of
     * its own, and never through a handler the composition provides, so no app redirects or swallows it.
     */
    @Test
    fun theMapsMarkOpensItsLinkThroughThePlatformsOwnOpener() {
        val started = mutableListOf<Intent>()
        val recording =
            object : ContextWrapper(context) {
                override fun startActivity(intent: Intent) {
                    started += intent
                }
            }
        val view = watermarkViewOf(recording, mark, "Visiter Mountaya")
        view.performClick()

        val intent = assertNotNull(started.singleOrNull(), "a press starts one activity")
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Uri.parse("https://www.mountaya.com/fr/"), intent.data)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0, "the link opens in a task of its own")
    }
}
