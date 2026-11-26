package com.mountaya.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.mountaya.client.generated.RestSchema
import com.mountaya.elements.Controls.Drawn
import com.mountaya.elements.Controls.color
import com.mountaya.elements.Controls.help
import com.mountaya.elements.Controls.image
import com.mountaya.elements.Controls.ink
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.GeolocateControlEngine
import com.mountaya.geo.GeolocateControlTarget
import com.mountaya.geo.scopeOf
import kotlinx.coroutines.awaitCancellation
import org.junit.Rule
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GeolocateControlTest {
    @get:Rule
    val compose = createComposeRule()

    private val label = "Show my location"

    @Test
    fun aPressAsksTheMapForOneFixBusyUntilItSettlesEachFailureHandedOn() {
        val geolocation =
            Controls.Located(
                GeolocationFix.Refused(GeolocationRefusal.Denied),
                GeolocationPermission.Prompt,
            )
        val map = Controls.engine(geolocation = geolocation)
        val failures = Collections.synchronizedList(mutableListOf<Throwable>())
        compose.setContent { GeolocateControl(map, onError = { failures += it }) }

        compose.onNodeWithContentDescription(label).performClick()
        compose.waitUntil(TIMEOUT) { failures.size == 1 }
        compose.waitForIdle()

        assertEquals(1, geolocation.asked)
        assertEquals<List<Throwable>>(
            listOf(MapException(MapException.Kind.FixRefused, "locate", GeolocationRefusal.Denied)),
            failures,
        )
        assertEquals(
            "Centre the map on where you are.",
            compose.help(label),
            "a fix refused while the permission was never asked",
        )
    }

    @Test
    fun aHandlerStandsInForTheMapsFixWhichTheControlThenNeverAsksFor() {
        val geolocation =
            Controls.Located(
                GeolocationFix.Located(listOf(6.86, 45.83), 10.0),
                GeolocationPermission.Granted,
            )
        val map = Controls.engine(geolocation = geolocation)
        var clicks = 0
        compose.setContent { GeolocateControl(map, onClick = { clicks += 1 }) }

        compose.onNodeWithContentDescription(label).performClick()
        compose.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(0, geolocation.asked)
        }
    }

    @Test
    fun theControlSaysLocationIsBlockedWhileThePermissionAnswersRefusedAlone() {
        val refused =
            Controls.engine(
                geolocation =
                    Controls.Located(
                        GeolocationFix.Refused(GeolocationRefusal.Denied),
                        GeolocationPermission.Denied,
                    ),
            )
        compose.setContent { GeolocateControl(refused) }

        compose.waitUntil(TIMEOUT) { compose.help(label) == "Location is blocked. Allow it in your settings." }
        assertEquals(
            "Location is blocked. Allow it in your settings.",
            geolocateDescription(true, ElementsScope.standard),
        )
        assertEquals("Centre the map on where you are.", geolocateDescription(false, ElementsScope.standard))
        val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language("fr")))
        assertEquals("Localisation bloquée\u00A0: autorisez-la dans vos réglages.", geolocateDescription(true, french))
    }

    /** A map whose fix never answers, so the control stays busy. */
    private class HeldFix : GeolocateControlTarget {
        override suspend fun locate(): List<Double> = awaitCancellation()

        override suspend fun isLocationRefused(): Boolean = false
    }

    @Test
    fun aBusyControlIsDrawnInTheAccentAndOneWithNoMapAndNoHandlerIsInert() {
        val map =
            Controls.engine(
                geolocation =
                    Controls.Located(
                        GeolocationFix.Refused(GeolocationRefusal.Unavailable),
                        GeolocationPermission.Granted,
                    ),
            )
        compose.setContent {
            Column {
                Drawn("busy") {
                    val scope = rememberCoroutineScope()
                    val engine = remember { GeolocateControlEngine(scope, HeldFix()).also { it.locate() } }
                    GeolocateControl(map, engine)
                }
                Drawn("idle") { GeolocateControl(map) }
                Drawn("inert") { GeolocateControl(null) }
                Drawn("owned") { GeolocateControl(null, isLocating = true, onClick = {}) }
            }
        }
        compose.waitForIdle()

        val accent = color("#1c7ed6")
        assertTrue(compose.ink(compose.image("busy")).isNear(accent))
        assertTrue(compose.ink(compose.image("idle")).isNear(color("#212529")))
        assertTrue(compose.ink(compose.image("inert")).isNear(color("#adb5bd")))
        assertTrue(compose.ink(compose.image("owned")).isNear(accent), "the app's own fix, said out")
    }

    private companion object {
        /** How long a test waits for a fix or a read the engine sends on the main thread. */
        const val TIMEOUT = 5_000L
    }
}
