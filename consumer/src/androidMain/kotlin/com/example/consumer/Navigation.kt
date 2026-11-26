package com.example.consumer

import android.content.Context
import com.mountaya.client.Credential
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.FollowedRoute
import com.mountaya.geo.NavigationEngine
import com.mountaya.geo.NavigationInstruction
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.geo.NavigationRoute
import com.mountaya.geo.readDirectionsSteps
import com.mountaya.navigation.DeviceLocationSource
import com.mountaya.navigation.DeviceNavigationEngine
import com.mountaya.navigation.FlowLocationSource
import com.mountaya.navigation.InertLocationSource
import com.mountaya.navigation.InertNavigationVoice
import com.mountaya.navigation.LocationAccuracy
import com.mountaya.navigation.LocationAuthorization
import com.mountaya.navigation.LocationException
import com.mountaya.navigation.LocationRate
import com.mountaya.navigation.LocationSource
import com.mountaya.navigation.LocationStanding
import com.mountaya.navigation.NavigationVoice
import com.mountaya.navigation.ReplayLocationSource
import com.mountaya.navigation.SpeechException
import com.mountaya.navigation.SpeechNavigationVoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Navigation reads `mountaya-navigation` as an Android app does: the device's engine over the app's own
 * client, every seam it ships, and an app's own location source and voice, implemented from outside.
 */
object Navigation {
    /**
     * Returns the README's engine, as the app writes it: the route source over its own client, the device's positions
     * and a voice in its own words.
     */
    fun guided(
        scope: CoroutineScope,
        context: Context,
    ): DeviceNavigationEngine<List<List<Double>>> {
        val mountaya = Mountaya(Credential.Publishable("pk_live_0a1b", SessionToken.Value("sess_2c3d")))
        // Your words for each instruction, which is a copy key and its arguments, in a voice made up front and
        // prepared, so a speech engine that cannot start is told before the first maneuver.
        val voice = SpeechNavigationVoice(context, "fr-FR") { instruction -> sentence(instruction) }
        scope.launch { voice.prepare() }

        val navigation =
            DeviceNavigationEngine<List<List<Double>>>(
                scope = scope,
                // Your route source: one Data API request, its steps read into one vocabulary.
                route = { waypoints ->
                    val found =
                        mountaya.data.directions(
                            DataSchema.DirectionsInput(
                                activity = DataSchema.ActivitySlug.HikingAndTrail,
                                waypoints = waypoints,
                            ),
                        )
                    val route = found.routes?.firstOrNull() ?: throw NoRoute()
                    val line = route.geometry as? GeoJsonLineString ?: throw NoRoute()
                    val steps = readDirectionsSteps(route.steps, coordinateCount = line.coordinates.size)
                    NavigationRoute(line.coordinates, steps.steps, route.duration)
                },
                location = DeviceLocationSource(context),
                voice = voice,
            )
        navigation.start(listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)))
        return navigation
    }

    /** Returns every seam the module ships, and the app's own, handed to the engine. */
    fun seams(
        scope: CoroutineScope,
        context: Context,
    ) {
        val device = DeviceLocationSource(context, onError = { failure -> seen((failure as? LocationException)?.kind) })
        // A walker's rate: fewer fixes, still one between 55 and 50 m before each maneuver at a walking pace.
        val walking = DeviceLocationSource(context, rate = LocationRate.Walking)
        seen(walking.rate, device.rate == LocationRate.Navigation)
        // With the screen off, where the app's manifest declares it; refused as the source starts where it does not.
        val background =
            DeviceLocationSource(context, backgroundUpdates = true) { failure ->
                seen((failure as? LocationException)?.kind == LocationException.Kind.BackgroundUndeclared)
            }
        seen(background.backgroundUpdates)
        seen(LocationException.Kind.PlatformFailed, LocationException.Kind.AudioUndeclared)
        // From the result of the app's own permission request.
        val standing: LocationStanding = device.recheckPermission()
        seen(standing.authorization == LocationAuthorization.Always, standing.accuracy == LocationAccuracy.Full)
        val held: StateFlow<LocationStanding?> = device.standing
        seen(held.value)
        // The device's own clock, which a test hands its scheduler's in place of.
        val clock: TimeSource = TimeSource.Monotonic
        val replay = ReplayLocationSource(scope, metersPerSecond = 1.4, interval = 500.milliseconds, timeSource = clock)
        // A position the navigation refuses and a failure of the flow reach the app's handler, never its scope.
        val own = FlowLocationSource(scope, flowOf(listOf(6.8652, 45.9237))) { failure -> seen(failure.message) }
        val sources: List<LocationSource> = listOf(device, replay, own, InertLocationSource(), Track())
        // A speech engine that fails to start is told, a SpeechException, rather than dropping every sentence, and
        // so is each refusal of the audio focus, the sentence said anyway.
        val voice =
            SpeechNavigationVoice(
                context,
                "rm-CH",
                fallbackLanguage = "en-GB",
                onError = { failure ->
                    when ((failure as? SpeechException)?.kind) {
                        SpeechException.Kind.EngineUnavailable -> seen("no voice")
                        SpeechException.Kind.AudioRefused -> seen("said over other apps")
                        else -> seen(failure)
                    }
                },
            ) { instruction -> sentence(instruction) }
        val voices: List<NavigationVoice> = listOf(voice, InertNavigationVoice(), Subtitles())
        for (location in sources) {
            for (said in voices) {
                val engine: NavigationEngine<String> =
                    DeviceNavigationEngine(scope, { NavigationRoute(listOf(listOf(6.8652, 45.9237))) }, location, said)
                engine.start("summit")
                seen(engine.state.value.status)
                engine.close()
            }
        }
        voice.close()
        seen(DeviceNavigationEngine<String>(scope, { throw NoRoute() }).state.value)
    }

    /** The app's own words for an instruction, in its own strings. */
    private fun sentence(instruction: NavigationInstruction): String? =
        when (instruction.key) {
            NavigationInstructionKey.TurnLeft -> "Turn left onto ${instruction.name}"
            NavigationInstructionKey.TurnLeftPlain -> "Turn left"
            NavigationInstructionKey.RoundaboutExit -> "Take exit ${instruction.exit} onto ${instruction.name}"
            NavigationInstructionKey.Goal, NavigationInstructionKey.GoalPlain -> "You have arrived"
            else -> null
        }

    private class NoRoute : Exception("no route between the waypoints")
}

/** Track is an app's own location source: a recorded track, stated position by position. */
class Track : LocationSource {
    private var deliver: ((List<Double>) -> Unit)? = null

    override fun start(deliver: (position: List<Double>) -> Unit) {
        this.deliver = deliver
        deliver(listOf(6.8652, 45.9237, 1_035.0))
    }

    override fun stop() {
        deliver = null
    }

    override fun follow(route: FollowedRoute?) {
        seen(route?.line)
    }
}

/** Subtitles is an app's own voice, which writes each maneuver on the screen rather than saying it. */
class Subtitles : NavigationVoice {
    private val shown = mutableListOf<NavigationInstructionKey>()

    override fun say(instruction: NavigationInstruction) {
        shown += instruction.key
    }

    override fun hush() {
        shown.clear()
    }
}
