package com.mountaya.geo

/**
 * RouteRequests is what a navigation holds of its route requests (`geo.navigation.route-request`):
 * the ticket of the one in flight, how many it has sent, and the one waiting behind it.
 *
 * A request leaves under a ticket, the count of requests sent with it, so an answer names the request
 * it answers by the ticket it left under. Internal, as the rule over it is: a consumer observes which
 * route an engine follows, never its tickets.
 */
internal class RouteRequests<out Request>(
    /** The ticket of the request in flight, or null where none is. */
    val inFlight: Int?,
    /** How many requests have been sent, which is the ticket the last one left under. */
    val sent: Int,
    /** The request waiting for the one in flight to answer, or null where none waits. */
    val waiting: Waiting<Request>?,
) {
    /** A request waiting to be sent, held as it was made, a null request included. */
    class Waiting<out Request>(
        /** The request, as it was made. */
        val request: Request,
    )

    /** What happens to a navigation's route requests. */
    sealed interface Event<out Request> {
        /** A request is made. */
        class Made<out Request>(
            /** The request, as it was made. */
            val request: Request,
        ) : Event<Request>

        /** The request sent under [ticket] answers, with a route or a failure alike. */
        class Answered(
            /** The ticket the answered request left under. */
            val ticket: Int,
        ) : Event<Nothing>

        /** Everything is abandoned: the request waiting, and the one in flight. */
        object Cancelled : Event<Nothing>
    }

    /** A request to send now, and the ticket it leaves under. */
    class Send<out Request>(
        /** The request, as it was made. */
        val request: Request,
        /** The ticket it leaves under, which its answer names. */
        val ticket: Int,
    )

    /** What an event leaves: the requests held, the one to send now, and whether the answer it carries is acted on. */
    class Outcome<out Request>(
        /** The requests held after the event. */
        val state: RouteRequests<Request>,
        /** The request to send now, or null where none leaves. */
        val send: Send<Request>?,
        /** Whether the answer the event carries is acted on. Only an answer is ever delivered. */
        val deliver: Boolean,
    )

    companion object {
        /** A navigation that has sent nothing and holds nothing waiting. */
        val atRest: RouteRequests<Nothing> = RouteRequests(inFlight = null, sent = 0, waiting = null)
    }
}
