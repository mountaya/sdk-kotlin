package com.mountaya.conformance

import com.mountaya.client.MountayaException
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

/**
 * `transport.events.method-refusal`: a buffered call by its declaration with the case's `Accept`,
 * answered with the refusal it met and whether anything was sent.
 */
object TransportEventsMethodRefusalRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val accept = case.fields.text("accept")
        val exchange = Scripted.Exchange(scope, listOf(Scripted.Reply.Answer(200, body = Scripted.emptySuccess)))
        val refusal =
            try {
                Scripted.client(scope, exchange).send(
                    aRead,
                    "/v1/things",
                    JsonElement.serializer(),
                    JsonElement.serializer().nullable,
                    headers = mapOf("Accept" to accept),
                )
                null
            } catch (error: MountayaException) {
                error
            }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "refused" to Scripted.text(refusal?.name),
                "sent" to FixtureValue.Bool(exchange.sent.isNotEmpty()),
            ),
        )
    }
}
