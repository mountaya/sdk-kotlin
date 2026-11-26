package com.mountaya.geo

/**
 * Returns what [event] leaves of a navigation's route requests [state]: the requests held, the one to
 * send now, and whether the answer the event carries is acted on (`geo.navigation.route-request`).
 *
 * At most one request is in flight. A request is sent at once where none is, and otherwise waits, in
 * the place of any already waiting, so only the newest waits: dropping it instead would lose the
 * newest destination a traveller asked for. An answer to the request in flight is acted on where
 * nothing waits; where a request waits, the answer is to a question already replaced, so it is
 * dropped and the waiting request is sent in its place. An answer to any other ticket, one a cancel
 * abandoned, is dropped. A cancel drops the waiting request and abandons the one in flight, so the
 * next request is sent at once.
 *
 * Stated as a pure transition over a value, so the engine that sends the requests holds one field
 * and decides nothing of this itself.
 */
internal fun <Request> routeRequestsAfter(
    state: RouteRequests<Request>,
    event: RouteRequests.Event<Request>,
): RouteRequests.Outcome<Request> =
    when (event) {
        is RouteRequests.Event.Made -> {
            if (state.inFlight == null) {
                sending(state, event.request)
            } else {
                RouteRequests.Outcome(
                    RouteRequests(state.inFlight, state.sent, RouteRequests.Waiting(event.request)),
                    send = null,
                    deliver = false,
                )
            }
        }

        is RouteRequests.Event.Answered -> {
            val waiting = state.waiting
            when {
                event.ticket != state.inFlight -> {
                    RouteRequests.Outcome(state, send = null, deliver = false)
                }

                waiting == null -> {
                    RouteRequests.Outcome(RouteRequests(null, state.sent, null), send = null, deliver = true)
                }

                else -> {
                    sending(state, waiting.request)
                }
            }
        }

        RouteRequests.Event.Cancelled -> {
            RouteRequests.Outcome(RouteRequests(null, state.sent, null), send = null, deliver = false)
        }
    }

/** Sends [request] under the next ticket, with nothing left waiting. */
private fun <Request> sending(
    state: RouteRequests<Request>,
    request: Request,
): RouteRequests.Outcome<Request> {
    val ticket = state.sent + 1
    return RouteRequests.Outcome(
        RouteRequests(inFlight = ticket, sent = ticket, waiting = null),
        send = RouteRequests.Send(request, ticket),
        deliver = false,
    )
}
