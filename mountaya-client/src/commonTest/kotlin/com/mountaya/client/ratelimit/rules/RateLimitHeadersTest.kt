package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class RateLimitHeadersTest {
    @Test
    fun onePolicyPerStringMemberOfThePolicyFieldEachWithItsOwnRemainingFigures() {
        val state =
            assertNotNull(
                RateLimitHeaders.state(
                    """"minute";q=600;w=60, "month";q=1000000;w=2678400""",
                    """"month";r=420000;t=1209600, "minute";r=0;t=17, "hour";r=3""",
                ),
                "the headers state a rate limit",
            )
        assertEquals(listOf("minute", "month"), state.policies.map { it.name })
        assertEquals(RateLimitPolicy("minute", 600, 60.seconds, 0, 17.seconds), state.policies[0])
        assertEquals(420_000L, state.policies[1].remaining)
    }

    @Test
    fun aMemberThatIsNoStringOrHasNoWholeQuotaIsLeftOutAndNoneLeftIsNoState() {
        assertNull(RateLimitHeaders.state("""minute;q=60, "month";w=60""", null))
        assertNull(RateLimitHeaders.state(""""minute";q=60.5, "address";q=-1""", null))
        assertNull(RateLimitHeaders.state(null, """"minute";r=59"""))
    }

    @Test
    fun aRemainingFieldThatDoesNotParseLeavesEveryFigureUnstated() {
        val state =
            assertNotNull(
                RateLimitHeaders.state(""""minute";q=60;w=60""", """"minute";r=59;t="""),
                "the headers state a rate limit",
            )
        assertEquals(listOf(RateLimitPolicy("minute", 60, 60.seconds)), state.policies)
    }

    @Test
    fun aPolicyNamedTwiceIsKeptTwiceEachReadingTheFirstFiguresOfItsName() {
        val state =
            assertNotNull(
                RateLimitHeaders.state(""""minute";q=60, "minute";q=30""", """"minute";r=5, "minute";r=9"""),
                "the headers state a rate limit",
            )
        assertEquals(listOf(5L, 5L), state.policies.map { it.remaining })
        assertEquals(listOf(60L, 30L), state.policies.map { it.quota })
    }
}
