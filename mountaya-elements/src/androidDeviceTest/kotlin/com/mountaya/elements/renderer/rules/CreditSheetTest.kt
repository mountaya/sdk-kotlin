package com.mountaya.elements

import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.view.ContextThemeWrapper
import android.widget.ArrayAdapter
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The sheet a map's credit button opens, on the device's own dialogs. */
class CreditSheetTest {
    @Test
    fun aCreditReadsAsTheTextItsMarkupShowsAndOpensItsFirstLink() {
        val mountaya = creditLink("""<a href="https://mountaya.com/" target="_blank">&copy; Mountaya</a>""")
        assertEquals("© Mountaya", mountaya.first)
        assertEquals("https://mountaya.com/", mountaya.second)
        val quoted = creditLink("<a href='https://osm.org/copyright'>&#169; OSM &amp; friends</a>")
        assertEquals("© OSM & friends", quoted.first)
        assertEquals("https://osm.org/copyright", quoted.second)
        val plain = creditLink("  Imagery &copy; Satellite Provider &#x2014; &unknown; ")
        assertEquals("Imagery © Satellite Provider \u2014 &unknown;", plain.first)
        assertNull(plain.second)
    }

    @Test
    fun theSheetHoldsOneRowPerCreditInOrderAndAButtonThatClosesIt() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context =
            ContextThemeWrapper(instrumentation.targetContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        val opened = mutableListOf<Uri>()
        var sheet: AlertDialog? = null
        instrumentation.runOnMainSync {
            sheet =
                creditSheet(context, listOf("""<a href="https://mountaya.com/">&copy; Mountaya</a>""", "Imagery")) {
                    opened += it
                }.create()
            // A dialog lays its rows and its button out as it is created.
            sheet?.create()
        }
        val shown = assertNotNull(sheet, "the credits sheet is built")
        assertTrue(opened.isEmpty())
        instrumentation.runOnMainSync {
            val rows = shown.listView.adapter as ArrayAdapter<*>
            assertEquals(listOf("© Mountaya", "Imagery"), (0 until rows.count).map { rows.getItem(it).toString() })
            assertEquals(
                context.getString(android.R.string.ok),
                shown.getButton(DialogInterface.BUTTON_POSITIVE).text.toString(),
            )
        }
    }
}
