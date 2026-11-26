package com.mountaya.elements

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds

/**
 * DeviceGeolocation answers the device's position from the platform's location services, one fix at a time,
 * for a map to frame (`map.camera.locate`). `MapView` hands one over its context to the engine it draws where
 * the app gave the engine no geolocation of its own.
 *
 * It never asks for location access: it reads a fix only while the app holds `ACCESS_FINE_LOCATION`, so the
 * prompt, its wording and its moment stay the app's. A reader who allowed only an approximate location is
 * located nowhere rather than kilometres away: with the coarse permission alone it answers
 * `GeolocationRefusal.Denied`, and its permission reads `GeolocationPermission.Denied`. With neither, it
 * answers `GeolocationRefusal.Denied` too, and its permission reads `GeolocationPermission.Prompt`: Android
 * tells a permission never asked from one refused only to the app's own activity, and shows its dialog only
 * where the reader has not refused it for good, so the app's own flow is the one to ask. A fix the platform
 * made within `locateMaxFixAge`, a minute, is answered at once; otherwise it asks the platform for one and
 * answers `GeolocationRefusal.Unavailable` where none arrives within `locateFixWait`, ten seconds. It asks for the
 * precision every SDK's map asks one fix at, about a hundred metres, `QUALITY_BALANCED_POWER_ACCURACY` from API 31, as
 * `sdk-swift` asks iOS for `kCLLocationAccuracyHundredMeters`; below API 31 the platform takes no precision for a
 * provider's fix, and the provider's own stands.
 *
 * It reads the platform on the main thread, where the platform hands it each fix, and makes nothing until
 * it is asked, so a map that never locates touches no location service.
 */
public class DeviceGeolocation : Geolocation {
    /** What the geolocation reads of the platform's location services and asks of them. */
    internal interface Platform {
        /** What asking for a fix would do. */
        fun permission(): GeolocationPermission

        /** The newest fix the platform holds, and how long ago it was made, or null where it holds none. */
        fun lastFix(): Pair<GeolocationFix.Located, Duration>?

        /** Asks the platform for one fix, handing it to [receive], or null where it found none, and answers how to stop asking. */
        fun request(receive: (GeolocationFix.Located?) -> Unit): () -> Unit
    }

    private val platform: Platform
    private val timeout: Duration

    /** Creates a geolocation over [context]'s location services, which it holds as the application's own. */
    public constructor(context: Context) {
        platform = SystemPlatform(context.applicationContext)
        timeout = locateFixWait
    }

    /** Creates a geolocation over [platform], which a test answers, waiting [timeout] for a fix. */
    internal constructor(platform: Platform, timeout: Duration) {
        this.platform = platform
        this.timeout = timeout
    }

    /** Answers the device's position, or the reason there is none. */
    override suspend fun locate(): GeolocationFix {
        if (platform.permission() !=
            GeolocationPermission.Granted
        ) {
            return GeolocationFix.Refused(GeolocationRefusal.Denied)
        }
        platform.lastFix()?.let { (fix, age) -> if (age <= locateMaxFixAge) return fix }
        val fix =
            withTimeoutOrNull(timeout) {
                suspendCancellableCoroutine<GeolocationFix.Located?> { waiting ->
                    var answered = false
                    val stop =
                        platform.request { fix ->
                            if (!answered && waiting.isActive) {
                                answered = true
                                waiting.resume(fix)
                            }
                        }
                    waiting.invokeOnCancellation { stop() }
                }
            }
        return fix ?: GeolocationFix.Refused(GeolocationRefusal.Unavailable)
    }

    /**
     * Answers what asking for a fix would do: `GeolocationPermission.Granted` where the app holds the precise
     * permission, `GeolocationPermission.Denied` where it holds the approximate one alone, and
     * `GeolocationPermission.Prompt` where it holds neither, which the app's own flow asks for.
     */
    override suspend fun permission(): GeolocationPermission = platform.permission()

    override fun toString(): String = "DeviceGeolocation"

    /** The platform's location services, read on the main thread. */
    private class SystemPlatform(
        private val context: Context,
    ) : Platform {
        private val manager: LocationManager? by lazy { context.getSystemService(LocationManager::class.java) }

        override fun permission(): GeolocationPermission =
            when {
                isGranted(Manifest.permission.ACCESS_FINE_LOCATION) -> GeolocationPermission.Granted
                isGranted(Manifest.permission.ACCESS_COARSE_LOCATION) -> GeolocationPermission.Denied
                else -> GeolocationPermission.Prompt
            }

        override fun lastFix(): Pair<GeolocationFix.Located, Duration>? {
            val manager = manager ?: return null
            val newest =
                try {
                    manager
                        .getProviders(
                            true,
                        ).mapNotNull { manager.getLastKnownLocation(it) }
                        .maxByOrNull { it.elapsedRealtimeNanos }
                } catch (revoked: SecurityException) {
                    // The permission went between the check and the read: nothing is read.
                    null
                } ?: return null
            if (!newest.hasAccuracy()) return null
            return fixOf(newest) to (SystemClock.elapsedRealtimeNanos() - newest.elapsedRealtimeNanos).nanoseconds
        }

        override fun request(receive: (GeolocationFix.Located?) -> Unit): () -> Unit {
            val manager = manager
            val enabled = manager?.getProviders(true).orEmpty()
            val provider = providers.firstOrNull { it in enabled }
            if (manager == null || provider == null) {
                receive(null)
                return {}
            }
            val listener = OneFix(receive)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // The precision every SDK's map asks for one fix, about a hundred metres, as iOS's is asked.
                    val request =
                        LocationRequest
                            .Builder(0L)
                            .setQuality(LocationRequest.QUALITY_BALANCED_POWER_ACCURACY)
                            .build()
                    manager.requestLocationUpdates(provider, request, context.mainExecutor, listener)
                } else {
                    manager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                }
            } catch (revoked: SecurityException) {
                // The permission went between the check and the request: no fix comes.
                receive(null)
                return {}
            }
            return { manager.removeUpdates(listener) }
        }

        private fun isGranted(permission: String): Boolean =
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Hands the first fix the platform reports on, as a fix of its accuracy, a fix of no stated accuracy as one
     * of none. Below API 30 the platform calls every member of its listener, none of them a default there, so
     * each is written.
     */
    private class OneFix(
        private val receive: (GeolocationFix.Located?) -> Unit,
    ) : LocationListener {
        override fun onLocationChanged(location: Location) {
            receive(fixOf(location))
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {
            receive(null)
        }

        @Deprecated("The platform calls it below API 29 alone, and a fix says all it does.")
        override fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?,
        ) {}
    }

    private companion object {
        /** The providers a fix is asked of, the first the device holds enabled: the fused one, then the satellites', then the network's. */
        val providers: List<String> = listOf("fused", LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

        /** A fix of [location]: its position, and how far from it the device may stand, 0 where it states nothing. */
        fun fixOf(location: Location): GeolocationFix.Located =
            GeolocationFix.Located(
                listOf(location.longitude, location.latitude),
                if (location.hasAccuracy()) location.accuracy.toDouble() else 0.0,
            )
    }
}
