package com.mountaya.elements

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** A link the map opens leaves the application through the platform's own opener. */
class OpenLinkOutsideTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val link = Uri.parse("https://www.mountaya.com/en/")

    @Test
    fun theIntentViewsTheLinkInATaskOfItsOwn() {
        val intent = outsideLinkIntent(link)

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(link, intent.data)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0, "the link opens in a task of its own")
    }

    @Test
    fun theLinkIsStartedThroughTheContextAndADeviceWithNoOpenerOpensNothing() {
        val started = mutableListOf<Intent>()
        val recording =
            object : ContextWrapper(context) {
                override fun startActivity(intent: Intent) {
                    started += intent
                }
            }
        openLinkOutside(recording, link)

        assertEquals(link, assertNotNull(started.singleOrNull(), "one activity is started").data)
        val refusing =
            object : ContextWrapper(context) {
                override fun startActivity(intent: Intent): Unit = throw ActivityNotFoundException("no browser")
            }
        openLinkOutside(refusing, link)
    }
}
