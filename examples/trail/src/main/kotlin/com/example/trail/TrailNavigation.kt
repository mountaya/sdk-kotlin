package com.example.trail

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.DateLength
import com.mountaya.geo.FollowedRoute
import com.mountaya.geo.NavigationInstruction
import com.mountaya.geo.NavigationProgress
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.TimeLength
import com.mountaya.geo.UnitPreferences
import com.mountaya.geo.formatDateTime
import com.mountaya.geo.formatDistance
import com.mountaya.geo.formatDuration
import com.mountaya.geo.readItineraryRoute
import com.mountaya.geo.stepInstruction
import com.mountaya.navigation.DeviceLocationSource
import com.mountaya.navigation.DeviceNavigationEngine
import com.mountaya.navigation.LocationRate
import com.mountaya.navigation.ReplayLocationSource
import com.mountaya.navigation.SpeechNavigationVoice
import kotlinx.coroutines.CoroutineScope
import java.time.ZoneId
import java.util.TimeZone
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * TrailRun is one navigation, from Start to Stop: the engine following the whole itinerary, saying its prompts in the
 * app's one voice, and the device's location source where Replay was off, which the app's location request rechecks.
 */
class TrailRun(
    /** The engine following the itinerary, by its id. */
    val engine: DeviceNavigationEngine<String>,
    /** The device's location source, or null where the replay walks the route. */
    val device: DeviceLocationSource?,
) {
    private var stopped = false

    /**
     * Ends the navigation: the engine stops, which stops the positions and hushes the voice, and is let go, the voice
     * kept for the next Start. Ending one that has ended does nothing.
     */
    fun stop() {
        // trail: trail.stop
        if (stopped) return
        stopped = true
        engine.stop()
        engine.close()
    }

    /** The start of a navigation. */
    companion object {
        /**
         * Starts following [itineraryId] end to end with one engine, whose route source answers the one route
         * `readItineraryRoute` joins from the itinerary's segments and waypoints, positions from the replay at [settings]'
         * speed where [replay] is on and from the device otherwise, and each prompt said by [voice], the app's own.
         */
        fun start(
            context: Context,
            scope: CoroutineScope,
            itinerary: TrailItinerary,
            itineraryId: String,
            replay: Boolean,
            settings: TrailSettings,
            voice: SpeechNavigationVoice,
        ): TrailRun {
            // trail: trail.navigation
            // trail: trail.location
            val device = if (replay) null else DeviceLocationSource(context, rate = LocationRate.Walking)
            val engine =
                DeviceNavigationEngine<String>(
                    scope = scope,
                    route = { id -> joinedRoute(itinerary.itinerary(id)) },
                    location = device ?: ReplayLocationSource(scope, metersPerSecond = settings.replayMetersPerSecond),
                    voice = voice,
                )
            engine.start(itineraryId)
            return TrailRun(engine, device)
        }
    }
}

/** NoRoute says there is no route to follow: the itinerary holds no segment with a line. */
class NoRoute(
    itineraryId: String,
) : Exception("The itinerary $itineraryId holds no segment with a line to follow.")

/**
 * joinedRoute answers the one route `readItineraryRoute` joins from [itinerary]'s segments and waypoints, each waypoint
 * passed said on the way.
 */
fun joinedRoute(itinerary: RestSchema.ItineraryWithDetails): NavigationRoute =
    readItineraryRoute(itinerary.segments, itinerary.waypoints) ?: throw NoRoute(itinerary.id)

/**
 * instructionAhead answers the instruction of the maneuver ahead on [route]: the departure's before the first position
 * is read, then the maneuver at the end of the step the traveller is in, the arrival's in the last.
 */
fun instructionAhead(
    route: FollowedRoute,
    progress: NavigationProgress?,
): NavigationInstruction {
    // trail: trail.progress
    val step = if (progress == null) route.steps.first() else route.steps[progress.next ?: route.steps.lastIndex]
    return stepInstruction(step)
}

/**
 * progressLine writes what is left of the route at [progress], in [units]: the distance and the time left, and the
 * arrival from [now] as its time of day alone, short, in [timeZone], each by Geo's formatters. The time and the arrival
 * are left out where a step states no time, and the arrival alone where the platform knows no zone of that name, never
 * written in another zone in its place.
 */
fun progressLine(
    progress: NavigationProgress,
    units: UnitPreferences,
    timeZone: String,
    now: Instant,
): String {
    val left = progress.toArrival
    val duration = left.duration?.takeIf { it.isFinite() }
    val arrival =
        duration?.let { formatDateTime(now + it.seconds, units.language, timeZone, timeLength = TimeLength.Short) }
    return Words.progress(formatDistance(left.distance, units), formatDuration(duration, units), arrival)
}

/**
 * cutoffWriter answers how Trail writes a place's cut-off, its date, medium, and its time of day, short, in [language]
 * and [timeZone] by Geo's `formatDateTime`, or null where the platform knows no zone of that name: then no cut-off is
 * written at all, the profile marking none and the waypoint table leaving each out, and nothing stands in its place,
 * neither the instant written in UTC or in ISO 8601 nor an empty word.
 */
fun cutoffWriter(
    language: String,
    timeZone: String,
): ((Instant) -> String)? {
    // Whether the platform knows a zone does not hang on the instant, so one reading answers for every cut-off.
    formatDateTime(Instant.fromEpochSeconds(0), language, timeZone) ?: return null
    return { at ->
        checkNotNull(formatDateTime(at, language, timeZone, DateLength.Medium, TimeLength.Short)) {
            "The platform knew the zone $timeZone when the cut-off's writer was made."
        }
    }
}

/**
 * deviceTimeZone answers the device's time zone, as its reader set it: java.time's from API 26, and java.util's on the
 * releases before it.
 */
fun deviceTimeZone(): String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ZoneId.systemDefault().id else TimeZone.getDefault().id

/**
 * holdsFineLocation says whether the app holds full-accuracy location access, which the device's location source reads
 * positions under.
 */
fun holdsFineLocation(context: Context): Boolean {
    // trail: trail.location
    return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

/**
 * locationPermissions is the permissions Start asks for with Replay off: full accuracy, and the approximate one
 * Android 12 asks beside it.
 */
val locationPermissions: Array<String> =
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
