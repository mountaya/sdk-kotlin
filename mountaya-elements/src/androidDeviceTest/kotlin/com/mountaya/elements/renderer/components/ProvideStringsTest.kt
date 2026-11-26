package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import com.mountaya.client.InternalMountayaApi
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.UnitPreferences
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ProvideStringsTest {
    @get:Rule
    val compose = createComposeRule()

    /**
     * Handed no strings, a composable provides the very scope around it, so nothing under it is composed again for
     * them; handed some, the scope around it with them layered over it, and a nested composable's own over those.
     */
    @OptIn(InternalMountayaApi::class)
    @Test
    fun theScopeProvidedIsTheOneAroundWithTheStringsLayeredOverIt() {
        val read = mutableListOf<ElementsScope>()
        compose.setContent {
            MountayaScope(ElementsScopeStatement(locale = "en-GB")) {
                read += LocalMountayaScope.current
                ProvideStrings(null) { read += LocalMountayaScope.current }
                ProvideStrings(mapOf("controls.zoomIn" to "Plus")) {
                    read += LocalMountayaScope.current
                    ProvideStrings(mapOf("controls.zoomOut" to "Minus")) { read += LocalMountayaScope.current }
                }
            }
        }
        compose.waitForIdle()

        val (outer, none, own, nested) = read.take(4)
        assertSame(outer, none)
        assertEquals("en-GB", own.locale)
        assertEquals("Plus", own.word("controls.zoomIn"))
        assertEquals("Zoom out", own.word("controls.zoomOut"))
        assertEquals("Plus", nested.word("controls.zoomIn"))
        assertEquals("Minus", nested.word("controls.zoomOut"))
        assertEquals(mapOf("controls.zoomIn" to "Plus"), own.strings)
    }

    /**
     * A control's own strings stand over the scope's words for it alone: the control beside it, handed none, reads
     * the scope's, and a word its strings do not name is the scope's.
     */
    @Test
    fun aControlsOwnStringsStandOverTheScopesWordsForItAlone() {
        compose.setContent {
            Column {
                ZoomControl(null, strings = mapOf("controls.zoomIn" to "Plus"))
                ZoomControl(null)
            }
        }

        compose.onAllNodesWithContentDescription("Plus").assertCountEquals(1)
        compose.onAllNodesWithContentDescription("Zoom in").assertCountEquals(1)
        compose.onAllNodesWithContentDescription("Zoom out").assertCountEquals(2)
    }

    /**
     * Every control reads the words its own strings name: the compass, the locate control, and the two action controls.
     */
    @Test
    fun everyControlReadsTheWordsItsOwnStringsName() {
        compose.setContent {
            Column {
                CompassControl(null, strings = mapOf("controls.compass" to "North"))
                GeolocateControl(null, strings = mapOf("controls.geolocate" to "Find me"))
                DownloadControl(onClick = {}, strings = mapOf("controls.download" to "Export"))
                DirectionsControl(onClick = {}, strings = mapOf("controls.directions" to "Route"))
            }
        }

        for (word in listOf("North", "Find me", "Export", "Route")) {
            compose.onNodeWithContentDescription(word).assertExists()
        }
    }

    /**
     * A stack's strings reach every control it holds, the app's own among them, and a control's own stand over the
     * stack's for it alone.
     */
    @Test
    fun aStacksStringsReachItsControlsAndAControlsOwnStandOverThem() {
        compose.setContent {
            MapControls(
                null,
                zoom = true,
                compass = true,
                strings = mapOf("controls.zoomIn" to "Plus", "controls.compass" to "North"),
            ) {
                ZoomControl(null, strings = mapOf("controls.zoomIn" to "Closer"))
            }
        }

        compose.onAllNodesWithContentDescription("Plus").assertCountEquals(1)
        compose.onAllNodesWithContentDescription("Closer").assertCountEquals(1)
        compose.onAllNodesWithContentDescription("North").assertCountEquals(1)
        compose.onAllNodesWithContentDescription("Zoom in").assertCountEquals(0)
    }

    /** A panel's own strings stand over the scope's words: the preferences panel's heading of the length system. */
    @Test
    fun aPanelsOwnStringsStandOverTheScopesWords() {
        compose.setContent {
            Column {
                PreferencesPanel(UnitPreferences("en"), {}, strings = mapOf("preferences.length" to "Distances"))
                PreferencesPanel(UnitPreferences("en"), {})
            }
        }

        compose.onAllNodesWithText("Distances").assertCountEquals(1)
        compose.onAllNodesWithText(ElementsScope.standard.word("preferences.length")).assertCountEquals(1)
    }
}
