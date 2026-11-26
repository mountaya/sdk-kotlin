package com.mountaya.conformance

import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration.Companion.milliseconds

/**
 * `transport.idempotency.key-in-use`: a write with the fixture's key whose first attempts are
 * answered that the key is in use, answered with how many attempts it made, the waits between them
 * on the test's virtual time, and the error it raised with the key that error carries.
 */
object TransportIdempotencyKeyInUseRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val key = case.root("constants").text("idempotencyKey")
        val input = case.fields
        val headers = input.textOrNull("retryAfterHeader")?.let { mapOf("Retry-After" to it) }.orEmpty()
        val inUse = Scripted.Reply.Answer(409, headers, Scripted.errorBody("IDEMPOTENCY_KEY_IN_USE"))
        val created = Scripted.Reply.Answer(201, body = Scripted.emptySuccess)
        val count = input.numberOrNull("inUse")?.toInt()
        val exchange =
            if (count != null) {
                Scripted.Exchange(scope, List(count) { inUse } + created)
            } else {
                Scripted.Exchange(scope, emptyList(), exhausted = inUse)
            }
        val retry = policy(input.number("maxRetries").toInt(), input.number("maxWaitMs").milliseconds)
        val client = Scripted.client(scope, exchange, retry = retry, random = Scripted.Random(input.number("random")))
        val error =
            try {
                client.send(
                    OperationDeclaration("postThing", "POST", declaresIdempotencyKey = true),
                    "/v1/things",
                    JsonElement.serializer(),
                    JsonElement.serializer().nullable,
                    idempotencyKey = key,
                )
                null
            } catch (raised: MountayaException) {
                raised
            }
        if (exchange.sent.any {
                it.request.header("Idempotency-Key") != key
            }
        ) {
            throw AssertionError("an attempt sent another key")
        }
        val times = exchange.sent.map { it.at }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "attempts" to Scripted.number(times.size),
                "class" to (error?.let { FixtureValue.Text(it.name) } ?: FixtureValue.Null),
                "idempotencyKey" to Scripted.text(error?.idempotencyKey),
                "waitsMs" to
                    FixtureValue.Array(times.zipWithNext { earlier, later -> Scripted.milliseconds(later - earlier) }),
            ),
        )
    }
}
