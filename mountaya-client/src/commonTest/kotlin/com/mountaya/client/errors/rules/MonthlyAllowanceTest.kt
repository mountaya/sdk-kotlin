package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * A `429` refused for a spent monthly allowance names when the allowance renews, never a wait to sit
 * out: nothing lifts it before the next billing period but a larger allowance.
 */
class MonthlyAllowanceTest {
    /** A quarter of a second past midnight on 2026-11-14, UTC, which the instant named truncates to its second. */
    private val now = Instant.fromEpochMilliseconds(1_794_614_400_250)

    /** The policy an organization's allowance is counted under, a month long. */
    private val monthPolicy = "\"month\";q=300000;w=2678400"

    /** The error a `429` the API answers with [headers] raises at [now]. */
    private fun refusal(
        headers: Map<String, String>,
        code: String = "TOO_MANY_REQUESTS",
    ): ApiException =
        AnswerError.exception(
            429,
            FoldedHeaders(headers),
            ErrorEnvelope.read(
                """{"errors":[{"message":"Allowance used.","extensions":{"code":"$code"}}]}""".encodeToByteArray(),
            ),
            now,
            null,
        )

    private fun state(vararg policies: Pair<String, Long?>): RateLimitState =
        RateLimitState(policies.map { (name, remaining) -> RateLimitPolicy(name, 1, remaining = remaining) })

    @Test
    fun theAllowanceIsSpentWhereTheMonthHasNothingLeftAndNoOtherPolicyIsSpent() {
        assertTrue(MonthlyAllowance.isSpent(state("month" to 0L)))
        assertTrue(MonthlyAllowance.isSpent(state("month" to 0L, "address" to 4L)))
        assertTrue(MonthlyAllowance.isSpent(state("month" to 0L, "address" to null)))
        assertFalse(MonthlyAllowance.isSpent(state("month" to 0L, "minute" to 0L)))
        assertFalse(MonthlyAllowance.isSpent(state("month" to 41L)))
        assertFalse(MonthlyAllowance.isSpent(state("month" to null)))
        assertFalse(MonthlyAllowance.isSpent(state("minute" to 0L)))
        assertFalse(MonthlyAllowance.isSpent(state()))
        assertFalse(MonthlyAllowance.isSpent(null))
    }

    @Test
    fun theStepNamesTheSecondsItsWaitNamesAndTheInstantTheyEndToTheSecond() {
        assertEquals(
            "The organization's monthly allowance on this API is spent: it renews when the next billing period " +
                "begins " +
                "in 1036800 seconds (at 2026-11-26T00:00:00Z), so this call is not repeated before then. A larger " +
                "allowance is set in the organization's agreement with Mountaya (https://mountaya.com/en/contact/).",
            MonthlyAllowance.step(1_036_800.seconds, now),
        )
        assertTrue("begins in 1 second (at 2026-11-14T00:00:01Z), so" in MonthlyAllowance.step(1.seconds, now))
    }

    @Test
    fun aWaitItDoesNotKnowNamesNoSecondsAndAnInstantItCannotPlaceNamesNone() {
        val bare =
            "The organization's monthly allowance on this API is spent: it renews when the next billing period " +
                "begins, " +
                "so this call is not repeated before then."

        assertTrue(MonthlyAllowance.step(null, now).startsWith(bare))
        assertTrue(MonthlyAllowance.step(Duration.ZERO, now).startsWith(bare))
        assertTrue("begins in 30 seconds, so" in MonthlyAllowance.step(30.seconds, null))
        assertTrue(
            "begins in 9999999999999 seconds, so this call is not repeated before then." in
                MonthlyAllowance.step(9_999_999_999_999.seconds, now),
        )
    }

    @Test
    fun a429RefusedForTheSpentAllowanceSaysWhenItRenewsAfterTheApisWords() {
        val spent =
            refusal(
                mapOf(
                    "Retry-After" to "1036800",
                    "RateLimit" to "\"month\";r=0;t=1036800",
                    "RateLimit-Policy" to monthPolicy,
                ),
            )

        assertIs<RateLimitException>(spent)
        assertEquals(1_036_800.seconds, spent.retryAfter)
        assertEquals(
            "429 TOO_MANY_REQUESTS: Allowance used. The organization's monthly allowance on this API is spent: " +
                "it renews " +
                "when the next billing period begins in 1036800 seconds (at 2026-11-26T00:00:00Z), so this call " +
                "is not " +
                "repeated before then. A larger allowance is set in the organization's agreement with Mountaya " +
                "(https://mountaya.com/en/contact/).",
            spent.message,
        )
        // In the last seconds of a period the wait is shorter than a minute's: the state, not the wait,
        // says it is the allowance.
        val last =
            refusal(mapOf("Retry-After" to "1", "RateLimit" to "\"month\";r=0;t=1", "RateLimit-Policy" to monthPolicy))
        assertTrue("begins in 1 second (at 2026-11-14T00:00:01Z), so" in last.message)
    }

    @Test
    fun aPerMinuteRefusalAndAMonthWithSomethingLeftNameAWaitAndACeilingItsOwn() {
        val minute =
            refusal(
                mapOf(
                    "Retry-After" to "23",
                    "RateLimit" to "\"month\";r=0;t=1036800, \"minute\";r=0;t=23",
                    "RateLimit-Policy" to "$monthPolicy, \"minute\";q=60;w=60",
                ),
            )
        val left =
            refusal(
                mapOf(
                    "Retry-After" to "23",
                    "RateLimit" to "\"month\";r=41;t=1036800",
                    "RateLimit-Policy" to monthPolicy,
                ),
            )
        val ceiling =
            refusal(
                mapOf("RateLimit" to "\"month\";r=0;t=1036800", "RateLimit-Policy" to monthPolicy),
                code = "LIMIT_REACHED",
            )

        assertEquals("429 TOO_MANY_REQUESTS: Allowance used. Wait 23 seconds before calling again.", minute.message)
        assertEquals("429 TOO_MANY_REQUESTS: Allowance used. Wait 23 seconds before calling again.", left.message)
        assertEquals("429 LIMIT_REACHED: Allowance used. ${NextStep.ceiling}", ceiling.message)
    }

    @Test
    fun anErrorACallerBuildsNamesTheSecondsOfItsWaitAndNoInstantItCannotPlace() {
        val error =
            RateLimitException(
                listOf(ErrorEntry("Allowance used", ApiErrorCode("TOO_MANY_REQUESTS"))),
                retryAfter = 60.seconds,
                rateLimit = state("month" to 0L),
            )

        assertTrue(
            error.message.startsWith(
                "429 TOO_MANY_REQUESTS: Allowance used. The organization's monthly allowance on this API is " +
                    "spent: it " +
                    "renews when the next billing period begins in 60 seconds, so",
            ),
            error.message,
        )
    }
}
