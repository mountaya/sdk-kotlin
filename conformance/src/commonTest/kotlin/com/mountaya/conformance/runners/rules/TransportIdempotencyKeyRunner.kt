package com.mountaya.conformance

import com.mountaya.client.ConfigurationException
import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

/**
 * `transport.idempotency.key`: the case's calls made one after the other, each failing its first
 * attempts as the case says before one is answered, answered with the key every attempt sent, a
 * key the client drew written as a label of its order, and the refusal a call met.
 */
object TransportIdempotencyKeyRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val drawn = Regex(case.root("constants").text("drawnKeyPattern"))
        val input = case.fields
        val declaration =
            OperationDeclaration(
                operationId = "writeThing",
                method = input.text("method"),
                idempotent = Scripted.flag(input, "idempotent"),
                declaresIdempotencyKey = Scripted.flag(input, "declaresKey"),
            )
        val given = input.textOrNull("idempotencyKey")
        val calls = input.number("calls").toInt()
        val failures =
            input.items("failures").map { item ->
                val failure = item as? FixtureValue.Object ?: throw AssertionError("a failure is no object")
                val status = failure.numberOrNull("status")?.toInt() ?: return@map Scripted.Reply.Drop()
                val headers = failure.textOrNull("retryAfterHeader")?.let { mapOf("Retry-After" to it) }.orEmpty()
                Scripted.Reply.Answer(status, headers, Scripted.errorBody(failure.textOrNull("code")))
            }
        val script = List(calls) { failures + Scripted.Reply.Answer(200, body = Scripted.emptySuccess) }.flatten()
        val keys = (input["randomSource"] as? FixtureValue.Bool)?.value ?: true
        val exchange = Scripted.Exchange(scope, script)
        val client =
            Scripted.client(
                scope,
                exchange,
                retry = Scripted.twoQuickRetries,
                random = Scripted.Random(keys = keys),
            )
        var refusal: String? = null
        repeat(calls) {
            try {
                client.send(
                    declaration,
                    "/v1/things",
                    JsonElement.serializer(),
                    JsonElement.serializer().nullable,
                    idempotencyKey = given,
                )
            } catch (error: MountayaException) {
                if (error is ConfigurationException) refusal = error.name
            }
        }
        val labels = mutableMapOf<String, String>()
        val written =
            exchange.sent.map { sent ->
                val key = sent.request.header("Idempotency-Key") ?: return@map FixtureValue.Null
                if (key == given) return@map FixtureValue.Text(key)
                if (!drawn.matches(key)) throw AssertionError("the drawn key `$key` is no version 4 UUID in lowercase")
                FixtureValue.Text(labels.getOrPut(key) { "k${labels.size + 1}" })
            }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "keys" to FixtureValue.Array(written),
                "refused" to Scripted.text(refusal),
            ),
        )
    }
}
