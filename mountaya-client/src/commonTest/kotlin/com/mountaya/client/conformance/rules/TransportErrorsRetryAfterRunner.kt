package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope
import kotlin.time.DurationUnit
import kotlin.time.Instant

/**
 * `transport.errors.retry-after`: the wait a header asks for, in the whole seconds an error carries it in,
 * a date's distance rounded up, or null where it asks for none.
 */
object TransportErrorsRetryAfterRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val now = Instant.fromEpochMilliseconds(input.number("nowMs").toLong())
        val wait = AnswerError.retryAfter(FoldedHeaders(mapOf(RETRY_AFTER_HEADER to input.text("header"))), now)
        val seconds = wait?.let { FixtureValue.Number(it.toDouble(DurationUnit.SECONDS)) } ?: FixtureValue.Null
        return ConformanceOutcome.Value(seconds)
    }
}
