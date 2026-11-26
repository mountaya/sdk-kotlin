package com.mountaya.conformance

import com.mountaya.client.ApiException
import com.mountaya.client.ConflictException
import com.mountaya.client.IdempotentAnswerUnavailableException
import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.RetryPolicy
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * `transport.idempotency.answer-unavailable`: a keyed write whose every attempt is answered with
 * the case's 409, answered with how many attempts the call made, the error it raised, whether it is
 * a `ConflictException`, and what that error carries of the answer the write was first given.
 */
object TransportIdempotencyAnswerUnavailableRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val code = input.text("code")
        val body =
            buildList {
                add(
                    "errors" to
                        FixtureValue.Array(
                            listOf(
                                Scripted.objectOf(
                                    "extensions" to Scripted.objectOf("code" to FixtureValue.Text(code)),
                                    "message" to FixtureValue.Text("scripted"),
                                ),
                            ),
                        ),
                )
                input["metadata"]?.takeIf { it !is FixtureValue.Null }?.let {
                    add("extensions" to Scripted.objectOf("metadata" to it))
                }
            }
        val headers = input.textOrNull("retryAfterHeader")?.let { mapOf("Retry-After" to it) }.orEmpty()
        val reply = Scripted.Reply.Answer(409, headers, Scripted.json(FixtureValue.Object(body)))
        val policy = policy(2, input.numberOrNull("maxWaitMs")?.milliseconds)
        val draw = input.numberOrNull("random") ?: 0.0
        val exchange = Scripted.Exchange(scope, emptyList(), exhausted = reply)
        val client = Scripted.client(scope, exchange, retry = policy, random = Scripted.Random(draw))
        val error =
            try {
                client.send(
                    OperationDeclaration("postThing", "POST", declaresIdempotencyKey = true),
                    "/v1/things",
                    JsonElement.serializer(),
                    JsonElement.serializer().nullable,
                )
                throw AssertionError("the write raised nothing")
            } catch (raised: MountayaException) {
                raised
            }
        val api = error as? ApiException ?: throw AssertionError("the write raised ${error.name}")
        val answer =
            (api as? IdempotentAnswerUnavailableException)?.answer?.let {
                Scripted.objectOf(
                    "etag" to Scripted.text(it.etag),
                    "location" to Scripted.text(it.location),
                    "status" to (it.status?.let { status -> Scripted.number(status) } ?: FixtureValue.Null),
                )
            } ?: FixtureValue.Null
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "answer" to answer,
                "attempts" to Scripted.number(exchange.sent.size),
                "class" to FixtureValue.Text(api.name),
                "conflict" to FixtureValue.Bool(api is ConflictException),
            ),
        )
    }
}

/** The fixtures' retry policy with [maxRetries] and the case's wait budget, or the standard one's. */
internal fun policy(
    maxRetries: Int,
    waitBudget: Duration?,
): RetryPolicy =
    RetryPolicy(
        maxRetries = maxRetries,
        baseDelay = Duration.ZERO,
        maxDelay = Duration.ZERO,
        waitBudget = waitBudget ?: RetryPolicy.standard.waitBudget,
    )
