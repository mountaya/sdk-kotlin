package com.mountaya.elements

import android.Manifest
import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** The device's geolocation, over a platform a test scripts, and over the emulator's own location services. */
class DeviceGeolocationTest {
    /** A platform answering what the test states, and counting what it is asked. */
    private class Scripted(
        val permission: GeolocationPermission,
        val held: Pair<GeolocationFix.Located, Duration>? = null,
        val answer: ((GeolocationFix.Located?) -> Unit) -> Unit = {},
    ) : DeviceGeolocation.Platform {
        var requested = 0
        var stopped = 0

        override fun permission(): GeolocationPermission = permission

        override fun lastFix(): Pair<GeolocationFix.Located, Duration>? = held

        override fun request(receive: (GeolocationFix.Located?) -> Unit): () -> Unit {
            requested += 1
            answer(receive)
            return { stopped += 1 }
        }
    }

    private val hut = GeolocationFix.Located(listOf(6.86, 45.83), 25.0)

    @Test
    fun aPermissionNeverAskedIsDeniedAndReadsAsPromptAndNothingIsRequested() =
        runTest {
            val platform = Scripted(GeolocationPermission.Prompt, held = hut to 1.seconds)
            val geolocation = DeviceGeolocation(platform, 10.seconds)

            assertEquals(GeolocationFix.Refused(GeolocationRefusal.Denied), geolocation.locate())
            assertEquals(GeolocationPermission.Prompt, geolocation.permission())
            assertEquals(0, platform.requested)
        }

    @Test
    fun anApproximateLocationAloneIsDeniedAndReadsAsDenied() =
        runTest {
            val platform = Scripted(GeolocationPermission.Denied, held = hut to 1.seconds)
            val geolocation = DeviceGeolocation(platform, 10.seconds)

            assertEquals(GeolocationFix.Refused(GeolocationRefusal.Denied), geolocation.locate())
            assertEquals(GeolocationPermission.Denied, geolocation.permission())
            assertEquals(0, platform.requested)
        }

    @Test
    fun aFixOfTheLastMinuteIsAnsweredAtOnceWithItsAccuracy() =
        runTest {
            val platform = Scripted(GeolocationPermission.Granted, held = hut to 59.seconds)

            assertEquals(hut, DeviceGeolocation(platform, 10.seconds).locate())
            assertEquals(0, platform.requested)
        }

    @Test
    fun anOlderFixIsAskedAgainAndTheFixThePlatformReportsIsAnswered() =
        runTest {
            val fresh = GeolocationFix.Located(listOf(6.87, 45.84), 12.0)
            val platform =
                Scripted(GeolocationPermission.Granted, held = hut to 61.seconds) { receive -> receive(fresh) }

            assertEquals(fresh, DeviceGeolocation(platform, 10.seconds).locate())
            assertEquals(1, platform.requested)
        }

    @Test
    fun aPlatformThatFindsNoFixIsUnavailable() =
        runTest {
            val platform = Scripted(GeolocationPermission.Granted) { receive -> receive(null) }

            assertEquals(
                GeolocationFix.Refused(GeolocationRefusal.Unavailable),
                DeviceGeolocation(platform, 10.seconds).locate(),
            )
        }

    @Test
    fun aFixThatDoesNotArriveInTimeIsUnavailableAndTheRequestIsStopped() =
        runTest {
            val platform = Scripted(GeolocationPermission.Granted)

            assertEquals(
                GeolocationFix.Refused(GeolocationRefusal.Unavailable),
                DeviceGeolocation(platform, 20.milliseconds).locate(),
            )
            assertEquals(1, platform.requested)
            assertEquals(1, platform.stopped)
        }

    @Test
    fun theEmulatorsLocationServicesAreReadAsTheAppsPermissionStandsWithoutAPrompt() {
        // The instrumentation installs the test app with the permissions its manifest names already granted, so
        // the reading is held to what the platform says the app holds, waited for on the device's own clock.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fine =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse =
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val geolocation = DeviceGeolocation(context)
        val expected =
            when {
                fine -> GeolocationPermission.Granted
                coarse -> GeolocationPermission.Denied
                else -> GeolocationPermission.Prompt
            }

        runBlocking {
            assertEquals(expected, geolocation.permission())
            val fix = geolocation.locate()
            if (fine) {
                assertTrue(
                    fix is GeolocationFix.Located || fix == GeolocationFix.Refused(GeolocationRefusal.Unavailable),
                    "$fix",
                )
            } else {
                assertEquals(GeolocationFix.Refused(GeolocationRefusal.Denied), fix)
            }
        }
    }
}
