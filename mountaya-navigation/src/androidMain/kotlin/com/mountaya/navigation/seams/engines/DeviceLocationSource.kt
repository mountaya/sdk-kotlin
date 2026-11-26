package com.mountaya.navigation

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * DeviceLocationSource states where the device is, from the platform's satellite positioning, at the
 * [rate] the host chooses: every fix the platform reads for a navigation, a fix a second whatever the
 * distance moved, by default, or a walker's, a fix every two seconds once the device moved 5 m
 * ([LocationRate]).
 *
 * It never asks for location access: it reads positions only while the app holds
 * `ACCESS_FINE_LOCATION`, so the prompt, its wording and its moment stay the app's. An approximate
 * location alone places a traveller kilometres from where they are, which no navigation reads, so it
 * reads nothing then. [standing] says where the app stands, read as the source starts, again whenever
 * one of the app's activities resumes while it is started, which is where a permission granted in the
 * system's settings or in the app's own prompt becomes known, and on [recheckPermission], which an app
 * that asks for the permission while a navigation runs calls from its request's result. It never starts
 * a foreground service either, which an app that navigates with the screen off runs itself, with the
 * background permission its review needs.
 *
 * [backgroundUpdates] says the host keeps reading with the screen off. Since Android 10 the platform hands a
 * backgrounded app fixes only where it holds `ACCESS_BACKGROUND_LOCATION` or runs a foreground service of
 * type `location` of its own, so as the source starts it reads the app's merged manifest, and where the app
 * declares neither, the request is refused as a [LocationException] of kind
 * `LocationException.Kind.BackgroundUndeclared`, and the fixes go on while the app is in use.
 *
 * A failure of the reader, a device with no satellite positioning, a permission revoked as the reader
 * starts, the device's location turned off, or any other failure of the platform's reader, is told to the
 * `onError` it was made with as a [LocationException] of that kind while the source is started, and the
 * source goes on: a recheck asks the reader again, and fixes resume once the location is on again. With no
 * `onError`, each failure is dropped, never thrown, and a debuggable app logs it once with `Log.w`. A fix
 * the engine refuses names no place, which the platform never reports, and is dropped.
 *
 * It is driven from the main thread, as the engine it feeds is, and the platform hands it each fix and
 * each resume there.
 */
public class DeviceLocationSource : LocationSource {
    /** What the source reads of the platform: where the app stands, the fixes, the reader's failures and the app's resumes. */
    internal interface Platform {
        /** Where the app stands on location access, read from the permissions it holds. */
        fun standing(): LocationStanding

        /**
         * Begins handing each fix read at [rate] to [receive], as `[longitude, latitude]`, and each failure of
         * the reader to [fail], and answers whether the reader started: a device with no satellite
         * positioning, or a permission revoked as it starts, starts none.
         */
        fun request(
            rate: LocationRate,
            receive: (position: List<Double>) -> Unit,
            fail: (failure: LocationException) -> Unit,
        ): Boolean

        /** Ends the fixes. */
        fun remove()

        /** Tells [resumed] each time one of the app's activities resumes, until [unwatch]. */
        fun watch(resumed: () -> Unit)

        /** Ends what [watch] tells. */
        fun unwatch()

        /**
         * Whether the app's merged manifest declares reading in the background: `ACCESS_BACKGROUND_LOCATION`, or,
         * from Android 10, a foreground service of type `location`.
         */
        fun declaresBackgroundLocation(): Boolean

        /** Logs [failure], which no `onError` was given for, where the app is debuggable. */
        fun dropped(failure: Throwable)
    }

    private val platform: Platform

    /** How often the source reads the device: a navigation's every fix, or a walker's fewer. */
    public val rate: LocationRate

    /**
     * Whether the host keeps reading with the screen off, which the source holds to what the app's manifest
     * declares as it starts.
     */
    public val backgroundUpdates: Boolean

    /** Told each failure of the reader while the source is started, or null to drop each. */
    private val onError: ((Throwable) -> Unit)?

    private val held = MutableStateFlow<LocationStanding?>(null)
    private var deliver: ((List<Double>) -> Unit)? = null
    private var updating = false

    /**
     * Creates a source over [context]'s location services, which it holds as the application's own, reading
     * the device at [rate], a navigation's by default, with the screen off too where [backgroundUpdates] is
     * true and the app's manifest declares it, and telling [onError] each failure of the reader, or dropping
     * each where it is null.
     */
    public constructor(
        context: Context,
        rate: LocationRate = LocationRate.Navigation,
        backgroundUpdates: Boolean = false,
        onError: ((Throwable) -> Unit)? = null,
    ) {
        platform = SystemPlatform(context.applicationContext)
        this.rate = rate
        this.backgroundUpdates = backgroundUpdates
        this.onError = onError
    }

    /** Creates a source over [platform], which a test answers, reading at [rate]. */
    internal constructor(
        platform: Platform,
        rate: LocationRate = LocationRate.Navigation,
        backgroundUpdates: Boolean = false,
        onError: ((Throwable) -> Unit)? = null,
    ) {
        this.platform = platform
        this.rate = rate
        this.backgroundUpdates = backgroundUpdates
        this.onError = onError
    }

    /**
     * Where the app stands on location access as last read, or null before any read: as the source
     * starts, as one of the app's activities resumes while it is started, and by [recheckPermission].
     */
    public val standing: StateFlow<LocationStanding?> = held.asStateFlow()

    /**
     * Begins stating the device's positions, once the app holds the permission to read them at full
     * accuracy, reading where it stands now and again at each resume. Starting a source that is started
     * does nothing.
     */
    override fun start(deliver: (position: List<Double>) -> Unit) {
        if (this.deliver != null) return
        this.deliver = deliver
        platform.watch { recheck() }
        // The platform hands a backgrounded app fixes only where its manifest says it may read them, so a request
        // for background updates the app does not declare is refused as the source starts, and it reads while the
        // app is in use, as `sdk-dart`'s Android side refuses it.
        if (backgroundUpdates && !platform.declaresBackgroundLocation()) {
            fail(LocationException(LocationException.Kind.BackgroundUndeclared))
        }
        recheck()
    }

    /** Ends the positions and the rechecks at each resume. Stopping a source that is stopped does nothing. */
    override fun stop() {
        if (deliver == null) return
        deliver = null
        platform.unwatch()
        update(held.value)
    }

    /**
     * Reads where the app stands on location access again, holds it in [standing] and answers it: the
     * positions begin where the source is started and the app now reads at full accuracy, and end where
     * it no longer does. An app calls it from the result of its own permission request.
     */
    public fun recheckPermission(): LocationStanding = recheck()

    override fun toString(): String =
        "DeviceLocationSource(rate=$rate, backgroundUpdates=$backgroundUpdates, updating=$updating, " +
            "standing=${held.value})"

    /** States [position], a fix the platform reports, where the source is started. */
    internal fun receive(position: List<Double>) {
        val deliver = deliver ?: return
        try {
            deliver(position)
        } catch (refused: IllegalArgumentException) {
            // A fix the platform reports names a place, so none is refused; one that did not is dropped.
        }
    }

    /** Reads where the app stands, holds it, and has the reader start or stop as it now allows. */
    private fun recheck(): LocationStanding {
        val standing = platform.standing()
        held.value = standing
        update(standing)
        return standing
    }

    /** Reads positions where the source is started and [standing] reads at full accuracy, and none otherwise. */
    private fun update(standing: LocationStanding?) {
        val wanted = deliver != null && standing != null && readsFixes(standing)
        if (wanted == updating) return
        if (wanted) {
            // A reader that did not start is asked again at the next recheck.
            updating = platform.request(rate, ::receive, ::fail)
        } else {
            updating = false
            platform.remove()
        }
    }

    /** Tells [failure] to `onError` while the source is started; with none, drops it, logged if debuggable. */
    private fun fail(failure: LocationException) {
        if (deliver == null) return
        val told = onError
        if (told != null) told(failure) else platform.dropped(failure)
    }

    /** The platform's location services and the app's activities, read on the main thread. */
    private class SystemPlatform(
        private val context: Context,
    ) : Platform {
        private val manager = context.getSystemService(LocationManager::class.java)
        private var listener: LocationListener? = null
        private var resumes: Resumes? = null

        override fun standing(): LocationStanding {
            val fine = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
            // Below API 29 a location permission reads in the background too.
            val background =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    fine || coarse
                }
            return locationStandingOf(fine, coarse, background)
        }

        override fun request(
            rate: LocationRate,
            receive: (position: List<Double>) -> Unit,
            fail: (failure: LocationException) -> Unit,
        ): Boolean {
            val manager = manager
            if (manager == null || LocationManager.GPS_PROVIDER !in manager.allProviders) {
                fail(LocationException(LocationException.Kind.NoProvider))
                return false
            }
            val listener = Fixes(receive, fail)
            try {
                manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    rate.intervalMilliseconds,
                    rate.distanceMeters,
                    listener,
                    Looper.getMainLooper(),
                )
            } catch (revoked: SecurityException) {
                // The permission went between the check and the request: nothing is read.
                fail(LocationException(LocationException.Kind.PermissionRevoked, revoked))
                return false
            } catch (failed: RuntimeException) {
                // Any other failure of the platform's reader, carried as it came, and asked again at the next recheck.
                fail(LocationException(LocationException.Kind.PlatformFailed, failed))
                return false
            }
            this.listener = listener
            // A provider turned off answers no fix until it is on again, which the reader is told now.
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                listener.onProviderDisabled(LocationManager.GPS_PROVIDER)
            }
            return true
        }

        override fun remove() {
            val listener = listener ?: return
            this.listener = null
            manager?.removeUpdates(listener)
        }

        override fun watch(resumed: () -> Unit) {
            unwatch()
            val application = context as? Application ?: return
            val resumes = Resumes(resumed)
            application.registerActivityLifecycleCallbacks(resumes)
            this.resumes = resumes
        }

        override fun unwatch() {
            val resumes = resumes ?: return
            this.resumes = null
            (context as? Application)?.unregisterActivityLifecycleCallbacks(resumes)
        }

        override fun declaresBackgroundLocation(): Boolean {
            val info = packageInfo(PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES)
            val permissions = info.requestedPermissions.orEmpty()
            if (Manifest.permission.ACCESS_BACKGROUND_LOCATION in permissions) return true
            // A foreground service's type is the platform's from Android 10, the release that asks for background
            // location apart; below it the permission alone is read, as `sdk-dart`'s Android side reads it.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
            return info.services.orEmpty().any { service ->
                (service.foregroundServiceType and ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION) != 0
            }
        }

        override fun dropped(failure: Throwable) {
            logDroppedFailure(context, "DeviceLocationSource", failure)
        }

        /** The app's own package, its manifest merged, read with [flags]. */
        private fun packageInfo(flags: Int): PackageInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val asked = PackageManager.PackageInfoFlags.of(flags.toLong())
                context.packageManager.getPackageInfo(context.packageName, asked)
            } else {
                packageInfoBefore33(flags)
            }

        /** Reads the app's own package as the floor below API 33 asks, the flags object of API 33 not there yet. */
        @Suppress("DEPRECATION")
        private fun packageInfoBefore33(flags: Int): PackageInfo =
            context.packageManager.getPackageInfo(context.packageName, flags)

        private fun isGranted(permission: String): Boolean =
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Hands each fix on, as `[longitude, latitude]`, and the provider turned off as a failure, once until it
     * is on again. Below API 30 the platform calls every member of its listener, none of them a default
     * there, so each is written.
     */
    private class Fixes(
        private val receive: (position: List<Double>) -> Unit,
        private val fail: (failure: LocationException) -> Unit,
    ) : LocationListener {
        private var disabled = false

        override fun onLocationChanged(location: Location) {
            receive(listOf(location.longitude, location.latitude))
        }

        override fun onProviderEnabled(provider: String) {
            disabled = false
        }

        override fun onProviderDisabled(provider: String) {
            if (disabled) return
            disabled = true
            fail(LocationException(LocationException.Kind.ProviderDisabled))
        }

        @Deprecated("The platform calls it below API 29 alone, and a fix says all it does.")
        override fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?,
        ) {}
    }

    /** Tells [resumed] of each of the app's activities resuming, and of nothing else. */
    private class Resumes(
        private val resumed: () -> Unit,
    ) : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            resumed()
        }

        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?,
        ) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle,
        ) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }
}

/** The authorizations under which the device's reader reads fixes. */
private val readingAuthorizations = setOf(LocationAuthorization.WhenInUse, LocationAuthorization.Always)

/** Whether the device's reader reads fixes under [standing]: allowed while in use or always, at full accuracy. */
private fun readsFixes(standing: LocationStanding): Boolean =
    standing.authorization in readingAuthorizations && standing.accuracy == LocationAccuracy.Full
