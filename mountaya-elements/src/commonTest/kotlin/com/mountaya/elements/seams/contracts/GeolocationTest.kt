package com.mountaya.elements

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class GeolocationTest {
    /** A geolocation of an app's own, answering a fix its own location stack holds. */
    private class Held(
        private val fix: GeolocationFix,
    ) : Geolocation {
        override suspend fun locate(): GeolocationFix = fix

        override suspend fun permission(): GeolocationPermission = GeolocationPermission.Granted
    }

    @Test
    fun anAppHandsAMapAGeolocationOfItsOwn() =
        runTest {
            val held: Geolocation = Held(GeolocationFix.Located(listOf(6.86, 45.83), 12.0))

            assertEquals<GeolocationFix>(GeolocationFix.Located(listOf(6.86, 45.83), 12.0), held.locate())
            assertEquals(GeolocationPermission.Granted, held.permission())
        }

    /**
     * The device's own geolocation answers a fix the platform made within a minute at once, and waits ten seconds
     * at most for one: `map/camera-locate.json`'s `maxFixAgeMs` and `fixWaitMs`.
     */
    @Test
    fun theDevicesOwnGeolocationAnswersAFixAMinuteOldAndWaitsTenSecondsForOne() {
        assertEquals(60.seconds, locateMaxFixAge)
        assertEquals(10.seconds, locateFixWait)
    }
}
