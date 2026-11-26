package com.mountaya.navigation

import com.mountaya.geo.NavigationEngine
import com.mountaya.geo.NavigationRouteSource
import com.mountaya.geo.NavigationState
import com.mountaya.geo.RouteFollowingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * DeviceNavigationEngine is the [NavigationEngine] an Android app navigates with: Geo's machine,
 * `RouteFollowingEngine`, with the device beside it, and guidance that says each maneuver aloud before
 * the traveller reaches it.
 *
 * Every number and every word it reports is Geo's, so it answers exactly as `ScriptedNavigationEngine`
 * answers in an app's tests and as the other SDKs' engines answer: the status, the progress, the
 * arrival and the route requests are the machine's, a traveller is off the route past 50 m
 * (`isOffRouteAt`), and [state] is the machine's own. It differs from the machine only in where a
 * position comes from and in what it says. Each route followed is planned as the other SDKs plan it:
 * each maneuver is announced from the step that leads to it, 50 m before that step's end, or as the step
 * begins where it is shorter, and the departure as the first step begins. At each state the machine
 * commits, the prompt due is read on the step Geo's progress names, from the distance left to its end,
 * and is said once per route, only while the navigation is `navigating`, so a prompt falls due at the
 * same metre in every SDK. A route that lands while a position is held says nothing until that position
 * is read on it, and a new route, a stop and a close hush the voice.
 *
 * Its seams are given at construction:
 *
 * - [route], the host's call for a route, one line over its own client, answering the
 *   `NavigationRoute` to follow.
 * - [location], where positions come from: `DeviceLocationSource` for the device's own,
 *   `ReplayLocationSource` to walk each route followed, `FlowLocationSource` over the app's own flow of
 *   positions, the app's own [LocationSource], or the default `InertLocationSource`, where the app hands
 *   each position to [setPosition]. It runs from the first [start] to [stop], started before the route
 *   is asked for, so a route that lands at once finds it started, and is told of each route followed
 *   once its guidance is laid out, so a position it states at once, as a replay states the route's
 *   start, is read on that route rather than one held from a navigation before.
 * - [voice], which says each maneuver: `SpeechNavigationVoice` in the host's words, the app's own
 *   [NavigationVoice], or the default `InertNavigationVoice`, which says nothing.
 *
 * It never asks for location access and never starts a foreground service: both are the app's. It
 * holds what it was given and closes none of it, so a voice the app made is the app's to close.
 *
 * ```kotlin
 * val navigation =
 *     DeviceNavigationEngine<List<List<Double>>>(
 *         scope = viewModelScope,
 *         route = { waypoints -> routeFromYourClient(waypoints) },
 *         location = DeviceLocationSource(context),
 *     )
 * navigation.start(listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)))
 * ```
 *
 * It is driven from the main thread, as Compose's state and a `ViewModel`'s are, and reports its state
 * as a `StateFlow` a composable collects. The machine tells it each state it commits inline, before its
 * state flow holds it, so the guidance and the location source have kept to a route before any
 * collector sees it land, whatever dispatcher the collector runs on.
 *
 * @param scope Where each route request runs, the caller's, whose dispatcher is the main thread, so an
 * answer is acted on there: `viewModelScope` or `lifecycleScope`.
 * @param route The host's call for each route.
 * @param location Where the traveller's positions come from.
 * @param voice What says each maneuver.
 * @param Request What the app asks a route for.
 */
public class DeviceNavigationEngine<Request>(
    scope: CoroutineScope,
    route: NavigationRouteSource<Request>,
    private val location: LocationSource = InertLocationSource(),
    voice: NavigationVoice = InertNavigationVoice(),
) : NavigationEngine<Request> {
    /** The route guided, its plan and the prompts said along it. */
    private val guidance = GuidanceEngine(voice)

    /** The machine, which tells [changed] each state it commits, inline, before its state flow holds it. */
    private val following = RouteFollowingEngine(scope, route, ::changed)

    /** Whether the machine holds a position, which it reads against each route that lands. */
    private var holdsPosition = false

    /** Whether the location source runs, from the first start to a stop. */
    private var locating = false

    /** True while a stop hands the machine its command, whose state [halt] keeps to. */
    private var halting = false

    private var closed = false

    /** The navigation as it stands, `NavigationState.idle` before the first command. */
    override val state: StateFlow<NavigationState>
        get() = following.state

    /**
     * Asks for a route to follow, and follows it once it lands; the first one starts the location source,
     * before the route is asked for.
     */
    override fun start(request: Request) {
        if (closed) return
        // Started first, so a route that lands within the request, from a source that answers without
        // suspending on a dispatcher that runs it where it is launched, finds the source started: what the
        // source states as it is told of that route is where the route is read, and never a position held
        // from a navigation before, which would read a replay's last walk at its arrival.
        if (!locating) {
            locating = true
            location.start { position -> setPosition(position) }
            // A source may close the engine as it starts, through a collector handed what it states.
            if (closed) return
        }
        following.start(request)
    }

    /** Asks for a new route where the navigation is `off-route`, and does nothing in any other status. */
    override fun reroute(request: Request) {
        if (closed) return
        following.reroute(request)
    }

    /**
     * States where the traveller is and reads it against the route followed, or keeps it for the route
     * that lands next, and says the prompt it finds due.
     *
     * @throws IllegalArgumentException where the position names no place.
     */
    override fun setPosition(position: List<Double>) {
        if (closed) return
        following.setPosition(position)
        holdsPosition = true
    }

    /** Ends the navigation, abandoning the request in flight and the one waiting, stopping the location source and what the voice says. */
    override fun stop() {
        if (closed) return
        halting = true
        try {
            following.stop()
        } finally {
            halting = false
        }
        // A collector may close the engine as it is handed the stop's state, and the close halted it then.
        if (!closed) halt()
    }

    /** Stops the navigation and makes every later command do nothing. */
    override fun close() {
        if (closed) return
        closed = true
        following.close()
        halt()
    }

    /**
     * Is told each state the machine commits, in order and before its state flow is: the guidance keeps to
     * the route followed, the location source is told of a route that lands, and the prompt due is said.
     */
    private fun changed(state: NavigationState) {
        if (closed || halting) return
        if (guidance.follow(state.route)) location.follow(state.route)
        // A route that lands while a position is held is read at that position next, and guided there, so
        // the route unread says no prompt.
        if (state.route != null && state.progress == null && holdsPosition) return
        guidance.guide(state)
    }

    /** Stops the positions and what is being said, and forgets the route guided. */
    private fun halt() {
        if (locating) {
            locating = false
            location.stop()
        }
        guidance.halt()
        location.follow(null)
    }
}
