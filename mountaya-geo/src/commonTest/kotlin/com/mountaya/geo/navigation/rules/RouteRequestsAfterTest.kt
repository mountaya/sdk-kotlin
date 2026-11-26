package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteRequestsAfterTest {
    private fun made(
        state: RouteRequests<String?>,
        request: String?,
    ) = routeRequestsAfter(state, RouteRequests.Event.Made(request))

    private fun answered(
        state: RouteRequests<String?>,
        ticket: Int,
    ) = routeRequestsAfter(state, RouteRequests.Event.Answered(ticket))

    @Test
    fun aRequestIsSentAtOnceWhereNoneIsInFlightUnderTheNextTicket() {
        val first = made(RouteRequests.atRest, "a")

        assertEquals("a", first.send?.request)
        assertEquals(1, first.send?.ticket)
        assertEquals(1, first.state.inFlight)
        assertFalse(first.deliver)
    }

    @Test
    fun aRequestMadeInFlightWaitsAndTheNewestTakesItsPlace() {
        val a = made(RouteRequests.atRest, "a").state
        val b = made(a, "b")
        val c = made(b.state, "c")

        assertNull(b.send)
        assertEquals("b", b.state.waiting?.request)
        assertEquals("c", c.state.waiting?.request)
        assertEquals(1, c.state.inFlight)
    }

    /** The answer is to a question already replaced: dropped, and the waiting one sent in its place. */
    @Test
    fun anAnswerSomethingWaitsBehindIsDroppedForTheWaitingRequest() {
        val waiting = made(made(RouteRequests.atRest, "a").state, "b").state
        val outcome = answered(waiting, 1)

        assertFalse(outcome.deliver)
        assertEquals("b", outcome.send?.request)
        assertEquals(2, outcome.send?.ticket)
        assertNull(outcome.state.waiting)
        assertTrue(answered(outcome.state, 2).deliver)
    }

    @Test
    fun anAnswerWithNothingWaitingIsActedOnAndLeavesNothingInFlight() {
        val outcome = answered(made(RouteRequests.atRest, "a").state, 1)

        assertTrue(outcome.deliver)
        assertNull(outcome.send)
        assertNull(outcome.state.inFlight)
        assertEquals(1, outcome.state.sent)
    }

    /** A cancel abandons both; the next request leaves at once, and the abandoned answer is dropped. */
    @Test
    fun aCancelAbandonsWhatIsInFlightAndWhatWaits() {
        val cancelled =
            routeRequestsAfter(made(made(RouteRequests.atRest, "a").state, "b").state, RouteRequests.Event.Cancelled)

        assertNull(cancelled.state.inFlight)
        assertNull(cancelled.state.waiting)
        assertFalse(cancelled.deliver)
        assertFalse(answered(cancelled.state, 1).deliver)

        val next = made(cancelled.state, "c")
        assertEquals(2, next.send?.ticket)
        assertFalse(answered(next.state, 1).deliver)
        assertTrue(answered(next.state, 2).deliver)
    }

    /** A request that is null is still one, and waits as one. */
    @Test
    fun aNullRequestIsARequest() {
        val waiting = made(made(RouteRequests.atRest, "a").state, null)

        assertEquals(null, waiting.state.waiting?.request)
        assertTrue(waiting.state.waiting != null)
        assertEquals(null, answered(waiting.state, 1).send?.request)
        assertEquals(2, answered(waiting.state, 1).send?.ticket)
    }
}
