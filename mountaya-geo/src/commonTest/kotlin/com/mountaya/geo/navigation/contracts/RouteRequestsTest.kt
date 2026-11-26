package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RouteRequestsTest {
    @Test
    fun aNavigationAtRestHasSentNothingAndHoldsNothing() {
        val rest = RouteRequests.atRest

        assertNull(rest.inFlight)
        assertEquals(0, rest.sent)
        assertNull(rest.waiting)
    }

    /** A request that is null is still a request: it waits as one, wrapped. */
    @Test
    fun aWaitingRequestIsHeldAsItWasMade() {
        val waiting = RouteRequests<String?>(inFlight = 1, sent = 1, waiting = RouteRequests.Waiting(null))

        assertNull(waiting.waiting?.request)
        assertEquals(1, waiting.inFlight)
        assertEquals("b", RouteRequests.Send("b", 2).request)
        assertEquals(2, RouteRequests.Send("b", 2).ticket)
    }
}
