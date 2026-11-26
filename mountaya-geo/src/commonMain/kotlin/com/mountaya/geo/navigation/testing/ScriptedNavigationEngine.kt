package com.mountaya.geo

import kotlinx.coroutines.flow.StateFlow

/**
 * ScriptedNavigationEngine is a [NavigationEngine] for an app's own unit tests: its route requests are
 * answered by the test, when the test says and as it says, and everything else is the contract's.
 *
 * It follows a route exactly as every engine does, over the same rules, so a state it reports is one a
 * navigation reports: the status the contract's table moves it to, the progress and the instruction a
 * position reads, the arrival. It loads no native library and starts no coroutine, so it runs on the
 * host JVM and in plain tests, and each command has its effect before it returns: [state]'s value
 * after a command is the state it left.
 *
 * Each request it sends is in [sent], in the order they left, and the test answers one with a route or
 * a failure. At most one is in flight, as the contract says: a start made meanwhile waits and is sent
 * once the answer lands, and a stop or a close abandons the request in flight, whose answer is then
 * dropped.
 *
 * ```kotlin
 * val navigation = ScriptedNavigationEngine<String>()
 * navigation.start("summit")
 * navigation.sent.single().answer(NavigationRoute(listOf(listOf(0.0, 0.0), listOf(0.01, 0.0)), duration = 600.0))
 * navigation.setPosition(listOf(0.005, 0.0))
 * check(navigation.state.value.status == NavigationStatus.Navigating)
 * ```
 *
 * It is driven from one thread, as every engine is.
 *
 * @param Request What the app asks a route for, kept as it was made in each [ScriptedRouteRequest].
 */
public class ScriptedNavigationEngine<Request> : NavigationEngine<Request> {
    private val requests = ArrayList<ScriptedRouteRequest<Request>>()

    /** Each request sent, by the ticket it left under: a request a stop abandoned before it left skips its ticket. */
    private val byTicket = HashMap<Int, ScriptedRouteRequest<Request>>()

    private val following: RouteFollowingEngine<Request> =
        RouteFollowingEngine(
            send = { request, ticket ->
                val sent = ScriptedRouteRequest(request) { answer -> answered(ticket, answer) }
                requests += sent
                byTicket[ticket] = sent
            },
            abandon = { ticket -> byTicket[ticket]?.abandon() },
        )

    /** Every route request the engine sent, in the order they left, each answered when and how the test says. */
    public val sent: List<ScriptedRouteRequest<Request>>
        get() = requests.toList()

    override val state: StateFlow<NavigationState>
        get() = following.state

    override fun start(request: Request): Unit = following.start(request)

    override fun reroute(request: Request): Unit = following.reroute(request)

    override fun setPosition(position: List<Double>): Unit = following.setPosition(position)

    override fun stop(): Unit = following.stop()

    override fun close(): Unit = following.close()

    private fun answered(
        ticket: Int,
        answer: RouteFollowingEngine.Answer,
    ) = following.answered(ticket, answer)
}

/**
 * ScriptedRouteRequest is one route request a [ScriptedNavigationEngine] sent: the [request] the app
 * made, and how the test answers it, once, with [answer] or [fail].
 *
 * An answer to a request the engine abandoned, by a stop or a close, or to one a later request replaced
 * while it was in flight, is dropped, as an answer to such a request is by every engine.
 */
public class ScriptedRouteRequest<out Request> internal constructor(
    /** What the app asked a route for, as it made the request. */
    public val request: Request,
    private val deliver: (RouteFollowingEngine.Answer) -> Unit,
) {
    /** Whether a stop or a close abandoned the request, so its answer is dropped. */
    public var isAbandoned: Boolean = false
        private set

    /** Whether the test answered the request, with a route or a failure. */
    public var isAnswered: Boolean = false
        private set

    /**
     * Answers the request with [route], as a route source answers one.
     *
     * @throws IllegalStateException where the request was answered already.
     */
    public fun answer(route: NavigationRoute) {
        settle(RouteFollowingEngine.Answer.Route(route))
    }

    /**
     * Answers the request with a failure, as a route source throws [cause].
     *
     * @throws IllegalStateException where the request was answered already.
     */
    public fun fail(cause: Throwable) {
        settle(RouteFollowingEngine.Answer.Failure(cause))
    }

    internal fun abandon() {
        isAbandoned = true
    }

    private fun settle(answer: RouteFollowingEngine.Answer) {
        check(!isAnswered) { "ScriptedRouteRequest: the request for $request was answered already; answer each once." }
        isAnswered = true
        deliver(answer)
    }

    override fun toString(): String = "ScriptedRouteRequest($request, answered=$isAnswered, abandoned=$isAbandoned)"
}
