package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * `transport.retry.eligibility`: whether an attempt is followed by another, its answer being one the
 * contract repeats and the wait that answer names fitting the call's budget. An absent status is a
 * network failure.
 *
 * The fixture's policy waits no time of its own, so the only wait is the one `Retry-After` names,
 * and its draw is taken at zero, the wait the answer names; no case sits where the draw would
 * decide. The fixture names no wait before the attempt, so none is counted.
 */
object TransportRetryEligibilityRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val policy =
            RetryPolicy(
                maxRetries = input.number("maxRetries").toInt(),
                baseDelay = Duration.ZERO,
                maxDelay = Duration.ZERO,
            )
        val attempt = input.number("attempt").toInt()
        val repeated =
            isRetryable(
                attempt = attempt,
                status = input.numberOrNull("status")?.toInt(),
                code = input.textOrNull("code")?.let(::ApiErrorCode),
                idempotent = input.bool("idempotent"),
                keyed = input.bool("keyed"),
                policy = policy,
            )
        val retryAfter =
            input
                .textOrNull(
                    "retryAfterHeader",
                )?.let { parseRetryAfter(it, Instant.fromEpochMilliseconds(0)) }
        val fits = backoffDelay(attempt, draw = 0.0, retryAfter = retryAfter, policy = policy) != null
        return ConformanceOutcome.Value(FixtureValue.Bool(repeated && fits))
    }
}
