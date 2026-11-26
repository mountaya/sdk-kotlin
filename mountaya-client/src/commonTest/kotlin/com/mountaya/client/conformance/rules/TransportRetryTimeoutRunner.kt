package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * `transport.retry.timeout`: one call on the test's virtual time with the client's budget, the
 * operation declaring none of its own, answered with how many attempts were sent, how much time the
 * call took and how it settled.
 *
 * A keyed case declares the `Idempotency-Key` header, so the client draws one. A case that cancels
 * the call does it from a coroutine beside it, at the case's instant, which is the cancellation the
 * contract's abort stands for (SPEC.md section 9).
 */
object TransportRetryTimeoutRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        if (case.root("defaults").number("timeoutMs").milliseconds != AttemptBudget.clientDefault) {
            throw AssertionError("the fixture's default budget is not the client's")
        }
        val input = case.fields
        val timeout = input.numberOrNull("timeoutMs")?.milliseconds
        val answersAfter = input.numberOrNull("answersAfterMs")?.milliseconds
        val reply =
            answersAfter?.let { Scripted.Reply.Answer(200, body = Scripted.emptySuccess, after = it) }
                ?: Scripted.Reply.Never
        val abortAfter = input.numberOrNull("abortAfterMs")?.milliseconds
        val declaration =
            OperationDeclaration(
                "callThing",
                "POST",
                idempotent = Scripted.flag(input, "idempotent"),
                declaresIdempotencyKey = Scripted.flag(input, "keyed"),
            )
        val policy =
            RetryPolicy(
                maxRetries = input.number("maxRetries").toInt(),
                baseDelay = Duration.ZERO,
                maxDelay = Duration.ZERO,
            )
        val exchange = Scripted.Exchange(scope, emptyList(), exhausted = reply)
        val client = Scripted.client(scope, exchange, retry = policy, timeout = timeout ?: AttemptBudget.clientDefault)
        val started = Scripted.elapsed(scope)
        var outcome = "unsettled"
        val call =
            scope.launch {
                outcome =
                    try {
                        client.send(
                            declaration,
                            "/v1/things",
                            JsonElement.serializer(),
                            JsonElement.serializer().nullable,
                        )
                        "answered"
                    } catch (error: Exception) {
                        Scripted.className(error)
                    }
            }
        if (abortAfter != null) {
            scope.launch {
                delay(abortAfter)
                call.cancel()
            }
        }
        call.join()
        if (call.isCancelled && outcome == "unsettled") outcome = "aborted"
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "attempts" to Scripted.number(exchange.sent.size),
                "elapsedMs" to Scripted.milliseconds(Scripted.elapsed(scope) - started),
                "outcome" to FixtureValue.Text(outcome),
            ),
        )
    }
}
