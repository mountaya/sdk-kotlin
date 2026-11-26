package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant

/**
 * `transport.retry.backoff`: the wait before the next attempt, in milliseconds, for the draw the
 * case names, or null where that wait does not fit the call's budget. The fixture's `defaults` and
 * its jitter are held to the standard policy's own, so the defaults are proven too.
 *
 * Every attempt before the case's is answered with a 429 naming no wait, so each waited its
 * schedule with the same draw; those waits are taken through the same rule and counted against the
 * budget before the case's own.
 *
 * A case naming its `Retry-After` as an HTTP date is played through the client's own retry loop on
 * the test's virtual time, the client's wall clock reading `answeredAtMs` as the case's answer
 * arrives, and is answered with the time from that answer to the next attempt: the date's distance
 * in whole seconds rounded up, as the error a 429 raises carries it, plus the draw.
 */
object TransportRetryBackoffRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val defaults = case.root("defaults")
        val standard = RetryPolicy.standard
        agree("maxRetries", defaults.number("maxRetries"), standard.maxRetries.toDouble())
        agree("baseDelayMs", defaults.number("baseDelayMs"), standard.baseDelay.milliseconds())
        agree("maxDelayMs", defaults.number("maxDelayMs"), standard.maxDelay.milliseconds())
        agree("maxWaitMs", defaults.number("maxWaitMs"), standard.waitBudget.milliseconds())
        agree(
            "retryAfterJitterMs",
            case.root("constants").number("retryAfterJitterMs"),
            retryAfterJitter.milliseconds(),
        )

        val input = case.fields
        val policy =
            RetryPolicy(
                baseDelay = input.number("baseDelayMs").milliseconds,
                maxDelay = input.number("maxDelayMs").milliseconds,
                waitBudget = input.numberOrNull("maxWaitMs")?.milliseconds ?: standard.waitBudget,
            )
        val attempt = input.number("attempt").toInt()
        val draw = input.number("random")
        input.textOrNull("retryAfter")?.let { date ->
            val answeredAt = input.number("answeredAtMs").toLong()
            return ConformanceOutcome.Value(throughTheLoop(scope, policy, attempt, draw, date, answeredAt))
        }
        var waited = Duration.ZERO
        for (earlier in 0 until attempt) {
            waited += backoffDelay(earlier, draw, null, policy, waited)
                ?: throw AssertionError("an attempt before the case's was not repeated")
        }
        val delay = backoffDelay(attempt, draw, input.numberOrNull("retryAfterSeconds")?.seconds, policy, waited)
        return ConformanceOutcome.Value(delay?.let { FixtureValue.Number(it.milliseconds()) } ?: FixtureValue.Null)
    }

    /**
     * The wait the client's own retry loop takes after attempt [attempt], answered 429 naming [date] as its
     * `Retry-After`, every attempt before it answered 429 naming none: the time from that answer to the next
     * attempt, or null where no next attempt was sent. The client's wall clock reads [answeredAt] as that answer
     * arrives, and moves on with the test's virtual time.
     */
    private suspend fun throughTheLoop(
        scope: TestScope,
        policy: RetryPolicy,
        attempt: Int,
        draw: Double,
        date: String,
        answeredAt: Long,
    ): FixtureValue {
        val refused = Scripted.errorBody("RATE_LIMITED")
        val replies =
            List(attempt) { Scripted.Reply.Answer(429, body = refused) } +
                Scripted.Reply.Answer(429, mapOf("Retry-After" to date), refused) +
                Scripted.Reply.Answer(200, body = Scripted.emptySuccess)
        val exchange = Scripted.Exchange(scope, replies)
        val clock =
            object : Clock {
                override fun now(): Instant {
                    val answered = exchange.sent.getOrNull(attempt)?.at ?: Scripted.elapsed(scope)
                    return Instant.fromEpochMilliseconds(answeredAt) + (Scripted.elapsed(scope) - answered)
                }
            }
        val client = Scripted.client(scope, exchange, retry = policy, random = Scripted.Random(draw), clock = clock)
        try {
            client.send(
                OperationDeclaration("callThing", "GET", idempotent = true),
                "/v1/things",
                JsonElement.serializer(),
                JsonElement.serializer().nullable,
            )
        } catch (refusal: MountayaException) {
            // The answer that asked for a wait the budget does not hold is the call's error.
            if (refusal !is ApiException || refusal.status != 429) throw refusal
        }
        val next = exchange.sent.getOrNull(attempt + 1) ?: return FixtureValue.Null
        return FixtureValue.Number((next.at - exchange.sent[attempt].at).milliseconds())
    }

    private fun Duration.milliseconds(): Double = toDouble(DurationUnit.MILLISECONDS)

    private fun agree(
        name: String,
        fixture: Double,
        policy: Double,
    ) {
        if (fixture != policy) throw AssertionError("the fixture's $name is $fixture, the standard policy's is $policy")
    }
}
