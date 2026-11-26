package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope

/**
 * `geo.navigation.route-request`: which of the case's requests leave, and which answers are acted on,
 * as its `events` happen in order. A request is named by the case; an `answer` or a `fail` names the
 * request it answers, which the runner reads as the ticket that request last left under, and a route
 * and a failure are one answer to the rule, told apart only in what is delivered.
 */
object GeoNavigationRouteRequestRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        var requests: RouteRequests<String> = RouteRequests.atRest
        val tickets = mutableMapOf<String, Int>()
        val sent = mutableListOf<FixtureValue>()
        val delivered = mutableListOf<FixtureValue>()

        fun leave(send: RouteRequests.Send<String>?) {
            if (send == null) return
            tickets[send.request] = send.ticket
            sent += FixtureValue.Text(send.request)
        }

        for (item in case.fields.items("events")) {
            val event = item as? FixtureValue.Object ?: throw AssertionError("an event is not an object")
            when (val kind = event.text("event")) {
                "request" -> {
                    val outcome = routeRequestsAfter(requests, RouteRequests.Event.Made(event.text("request")))
                    requests = outcome.state
                    leave(outcome.send)
                }

                "answer", "fail" -> {
                    val request = event.text("request")
                    val ticket = tickets[request] ?: throw AssertionError("`$request` is answered and was never sent")
                    val outcome = routeRequestsAfter(requests, RouteRequests.Event.Answered(ticket))
                    requests = outcome.state
                    leave(outcome.send)
                    if (outcome.deliver) {
                        delivered +=
                            FixtureValue.Object(
                                listOf(
                                    "answer" to FixtureValue.Text(if (kind == "answer") "route" else "failure"),
                                    "request" to FixtureValue.Text(request),
                                ),
                            )
                    }
                }

                "cancel" -> {
                    requests = routeRequestsAfter(requests, RouteRequests.Event.Cancelled).state
                }

                else -> {
                    throw AssertionError("`$kind` is no event")
                }
            }
        }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "delivered" to FixtureValue.Array(delivered),
                    "sent" to FixtureValue.Array(sent),
                ),
            ),
        )
    }
}
