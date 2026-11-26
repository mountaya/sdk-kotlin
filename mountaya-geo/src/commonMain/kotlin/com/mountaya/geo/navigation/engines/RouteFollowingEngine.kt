package com.mountaya.geo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * RouteFollowingEngine is the whole of following a route over the contract's rules: the route
 * requests, the status, the progress and the arrival, and the state a renderer draws. Every engine is
 * this machine with a way to send a request: the scripted one hands each to a test, and one built over
 * a [NavigationRouteSource] to the host's own call, in a coroutine of its own.
 *
 * Every decision it makes is a rule of the contract's, read where it is stated: which request is sent
 * and which answer is acted on ([routeRequestsAfter]), the status each event moves it to
 * ([navigationStatusAfter]), the steps a route is followed by ([followedRouteOf]), where the
 * traveller stands and what is left (`navigationProgressAt`), and whether they are off the route
 * (`isOffRouteAt`), the last two read in one walk of the route's line per position, which answers both
 * rules' own numbers. It holds the state those rules are handed and nothing they do not decide. How it
 * walks a route is the contract's too (`geo.navigation.engine-walk`): the arrival is raised where the
 * traveller is on the route and their progress stands at its end, progress is read on every position
 * while a route is held, and a new route is asked for by [reroute] alone, never again on its own after
 * one fails.
 *
 * It is published as the building block every engine stands on, `mountaya-navigation`'s among them,
 * and it is a [NavigationEngine] of its own: built over a route source, it follows a route on every
 * target this module builds for, with positions from wherever the host reads them, which it hands to
 * [setPosition].
 *
 * ```kotlin
 * val navigation =
 *     RouteFollowingEngine<List<List<Double>>>(viewModelScope) { waypoints -> routeFromYourClient(waypoints) }
 * navigation.start(listOf(listOf(6.8652, 45.9237), listOf(6.9271, 45.9682)))
 * navigation.setPosition(listOf(6.8655, 45.9239))
 * ```
 *
 * It is driven from one thread, the one its commands are called on, and holds no lock: each command
 * has its effect before it returns, and a route request's answer is acted on where the scope's
 * dispatcher resumes the coroutine asking, which is that thread for a scope that dispatches there. An
 * engine built over it that must act on each state before any collector sees it is told each one by
 * `onChange`, inline, before [state] holds it.
 *
 * @param Request What the host asks a route for, handed to the route source as it is.
 */
public class RouteFollowingEngine<Request> : NavigationEngine<Request> {
    /** A route request's answer: the route, or what its source failed with. */
    internal sealed interface Answer {
        /** The source answered [route]. */
        class Route(
            /** The route, as the source answered it. */
            val route: NavigationRoute,
        ) : Answer

        /** The source failed with [cause]. */
        class Failure(
            /** What the source threw. */
            val cause: Throwable,
        ) : Answer
    }

    /** Sends a request under the ticket its answer is to name, through [answered]. */
    private val send: (request: Request, ticket: Int) -> Unit

    /**
     * Abandons the request sent under a ticket: a stop or a close no longer awaits it, and an answer
     * to it is dropped whatever it is. A ticket that never left is never handed to it.
     */
    private val abandon: (ticket: Int) -> Unit

    /** Is told each state the machine commits, inline, before [state] holds it. */
    private val onChange: ((NavigationState) -> Unit)?

    /**
     * Creates a machine that sends each request through [send], abandons one through [abandon], and
     * tells [onChange] each state it commits: the scripted engine's wiring, which hands each request to
     * a test.
     */
    internal constructor(
        send: (request: Request, ticket: Int) -> Unit,
        abandon: (ticket: Int) -> Unit,
        onChange: ((NavigationState) -> Unit)? = null,
    ) {
        this.send = send
        this.abandon = abandon
        this.onChange = onChange
    }

    /**
     * Creates an engine that is idle, and asks [route] for each route it follows, one request at a
     * time, each in a coroutine of its own in [scope]: a stop or a close cancels the coroutine asking
     * for the route it abandons, and drops its answer.
     *
     * [scope] is the caller's, whose dispatcher is the thread the engine is driven from, so an answer
     * is acted on there: `viewModelScope` or `lifecycleScope` on Android, a test's own scope under
     * `runTest`. Cancelling it ends every request in flight, whose answers then never land.
     */
    public constructor(scope: CoroutineScope, route: NavigationRouteSource<Request>) : this(scope, route, null)

    /**
     * Creates an engine that is idle, asks [route] for each route it follows as the two-parameter
     * constructor does, in [scope], and tells [onChange] each state it commits.
     *
     * [onChange], where given, is told each state that differs from the one before it, inline and in
     * order, before [state] holds it, so before any collector can see it, whatever dispatcher the
     * collector runs on: a route that lands in a coroutine of its own as much as a command's state. It
     * is for an engine built over this one that keeps something of its own to each state, as a
     * navigation library's session is kept to the route followed. A command it makes on this engine
     * has its effect at once, and its state is handed on after the one [onChange] was told, to
     * [onChange] and then to [state]. A request is asked for before the state that asks for it is
     * committed, and leaves once that state was handed on, so a stop or a close [onChange] makes on
     * `routing` or `rerouting` abandons the request before it leaves, and it never leaves. Nothing is
     * told after a close, one [onChange] makes included. What it throws passes through the command
     * that committed the state, and [state] still reaches the machine's.
     *
     * ```kotlin
     * val navigation = RouteFollowingEngine(viewModelScope, source) { state -> keepYourOwnTo(state) }
     * ```
     *
     * `keepYourOwnTo` stands for what the app keeps of each state, which it is told before any collector.
     */
    public constructor(
        scope: CoroutineScope,
        route: NavigationRouteSource<Request>,
        onChange: ((NavigationState) -> Unit)?,
    ) {
        this.onChange = onChange
        // The coroutine asking for each request in flight, by its ticket.
        val flights = HashMap<Int, Job>()
        send = { request, ticket ->
            // Started once it is held, so a dispatcher that runs it at once still finds it here.
            val flight =
                scope.launch(start = CoroutineStart.LAZY) {
                    val answer =
                        try {
                            Answer.Route(route.route(request))
                        } catch (failure: Throwable) {
                            // An abandoned request's coroutine is cancelled, and its answer is
                            // dropped whatever it is: the check rethrows that cancellation, which
                            // is never read as a failure. A cancellation the source throws while it
                            // is still asked, a timeout of its own, is its failure, and so is an
                            // `Error` it throws, a `TODO()` among them, which would otherwise end
                            // the caller's scope and leave the request in flight for good.
                            currentCoroutineContext().ensureActive()
                            Answer.Failure(failure)
                        }
                    // A source that answers as it is abandoned answers nobody.
                    currentCoroutineContext().ensureActive()
                    flights.remove(ticket)
                    answered(ticket, answer)
                }
            flights[ticket] = flight
            flight.start()
        }
        abandon = { ticket -> flights.remove(ticket)?.cancel() }
    }

    /** The state the machine committed last, which every command reads and [state] holds once it is handed on. */
    private var held = NavigationState.idle

    private val published = MutableStateFlow(NavigationState.idle)

    /**
     * The navigation as it stands, [NavigationState.idle] before the first command: each state the
     * machine commits, once `onChange` was told it. A collector is handed the state it holds now, then
     * each one that differs, conflated as a state flow conflates.
     */
    override val state: StateFlow<NavigationState> = published.asStateFlow()

    /**
     * Every state committed and not yet handed on, in order: a command `onChange` makes, or a
     * collector that runs where [state] is set, commits while a state is being handed on, and its
     * state is handed on after it.
     */
    private val pending = ArrayDeque<NavigationState>()
    private var handing = false

    /** The route requests, as [routeRequestsAfter] leaves them. */
    private var requests: RouteRequests<Request> = RouteRequests.atRest

    /**
     * The ticket of the request a command asked for and has not sent yet, which leaves once the state
     * that asked for it was handed on, or null where none waits to leave.
     */
    private var unsent: Int? = null

    /** The last position the host stated, read again against a route that lands after it. */
    private var position: List<Double>? = null

    private var closed = false

    override fun start(request: Request) {
        if (closed) return
        // Asked before the state is committed, as `sdk-dart` asks, so a stop or a close `onChange`
        // makes on that state abandons the request before it leaves.
        val leaving = ask(request)
        try {
            commit(NavigationState(navigationStatusAfter(held.status, NavigationEvent.Start)))
        } finally {
            leave(leaving)
        }
    }

    override fun reroute(request: Request) {
        if (closed) return
        val now = held
        val rerouting = navigationStatusAfter(now.status, NavigationEvent.Reroute)
        // The table moves the status only off the route, which is the one place a request is owed.
        if (rerouting == now.status) return
        val leaving = ask(request)
        try {
            commit(NavigationState(rerouting, now.route, now.progress, now.error))
        } finally {
            leave(leaving)
        }
    }

    override fun setPosition(position: List<Double>) {
        if (closed) return
        require(namesPlace(position)) {
            "NavigationEngine.setPosition: position is $position; pass a [longitude, latitude] of two finite " +
                "numbers, in degrees."
        }
        // A copy, so a host that reuses one list for every fix moves nothing held here.
        val stated = listOf(position[0], position[1])
        this.position = stated
        read(stated)
    }

    override fun stop() {
        if (closed) return
        cancel()
        commit(NavigationState(navigationStatusAfter(held.status, NavigationEvent.Stop)))
    }

    /**
     * Abandons the route requests and makes every later command do nothing; [state] stays as it was
     * last handed on, so a state committed and not yet handed on, by a close `onChange` makes, is
     * reported to no collector.
     */
    override fun close() {
        if (closed) return
        closed = true
        cancel()
        pending.clear()
    }

    /**
     * Acts on the [answer] to the request sent under [ticket], where the route requests deliver it,
     * and sends what they say to send in its place.
     */
    internal fun answered(
        ticket: Int,
        answer: Answer,
    ) {
        if (closed) return
        val outcome = routeRequestsAfter(requests, RouteRequests.Event.Answered(ticket))
        requests = outcome.state
        val next = outcome.send
        if (next != null) {
            send(next.request, next.ticket)
            return
        }
        if (!outcome.deliver) return
        when (answer) {
            is Answer.Route -> receive(answer.route)
            is Answer.Failure -> fail(NavigationError.RouteFailed(answer.cause))
        }
    }

    /** Drops the waiting request and abandons the one in flight, which a request that never left is not. */
    private fun cancel() {
        val inFlight = requests.inFlight
        requests = routeRequestsAfter(requests, RouteRequests.Event.Cancelled).state
        val left = inFlight != unsent
        unsent = null
        if (inFlight != null && left) abandon(inFlight)
    }

    /**
     * Tells the route requests a request was made, and answers what they say to send, which the command
     * hands to [leave] once its state was handed on.
     */
    private fun ask(request: Request): RouteRequests.Send<Request>? {
        val outcome = routeRequestsAfter(requests, RouteRequests.Event.Made(request))
        requests = outcome.state
        val leaving = outcome.send ?: return null
        unsent = leaving.ticket
        return leaving
    }

    /**
     * Sends [leaving], the request a command asked for, where it still waits to leave: a stop or a close
     * made as the command's state was handed on abandoned it, and it never leaves.
     */
    private fun leave(leaving: RouteRequests.Send<Request>?) {
        if (leaving == null || closed || unsent != leaving.ticket) return
        unsent = null
        send(leaving.request, leaving.ticket)
    }

    /**
     * Follows a route that landed, where the status table says it was awaited, and reads the last
     * position against it at once.
     */
    private fun receive(route: NavigationRoute) {
        val now = held
        val navigating = navigationStatusAfter(now.status, NavigationEvent.RouteReceived)
        // An answer the navigation is not awaiting moves nothing.
        if (navigating == now.status) return
        val followed = followedRouteOf(route)
        if (followed == null) {
            fail(NavigationError.EmptyRoute())
            return
        }
        commit(NavigationState(navigating, followed))
        position?.let(::read)
    }

    /** Records a route request that failed, where the status table says it was awaited. */
    private fun fail(error: NavigationError) {
        val now = held
        val failed = navigationStatusAfter(now.status, NavigationEvent.RouteFailed)
        // An answer the navigation is not awaiting moves nothing.
        if (failed == now.status) return
        commit(NavigationState(failed, now.route, now.progress, error))
    }

    /**
     * Reads [position] against the route followed, in one walk of its line: the progress from the last
     * one, whether the traveller is off the route, and, on it, whether their progress stands at its end.
     */
    private fun read(position: List<Double>) {
        val now = held
        val route = now.route ?: return
        val reading = routeReadingAt(route, position, now.progress?.along ?: 0.0)
        val off = isOffRoute(reading.meters)
        val placed = navigationStatusAfter(now.status, if (off) NavigationEvent.OffRoute else NavigationEvent.OnRoute)
        val next =
            if (!off && reading.progress.toArrival.distance == 0.0) {
                navigationStatusAfter(placed, NavigationEvent.Arrived)
            } else {
                placed
            }
        commit(NavigationState(next, route, reading.progress, now.error))
    }

    /**
     * Commits [next], where it differs from the state held, and hands it on: to `onChange`, then to
     * [state], each state committed while one is handed on after it, so both are handed the states in
     * the order they were committed. A closed engine commits nothing, so a close `onChange` makes ends
     * the command it was told from too.
     */
    private fun commit(next: NavigationState) {
        if (closed || next == held) return
        held = next
        pending.addLast(next)
        if (handing) return
        handing = true
        try {
            while (pending.isNotEmpty()) {
                val state = pending.removeFirst()
                try {
                    onChange?.invoke(state)
                } finally {
                    // A close `onChange` makes reports nothing more, the state it was told included.
                    if (!closed) published.value = state
                }
            }
        } finally {
            handing = false
            // Where `onChange` threw, the states committed after it reach the flow untold, so [state]
            // still holds the machine's.
            if (pending.isNotEmpty()) {
                pending.clear()
                if (!closed) published.value = held
            }
        }
    }
}
