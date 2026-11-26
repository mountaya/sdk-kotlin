package com.mountaya.conformance

import com.mountaya.client.ApiException
import com.mountaya.client.Credential
import com.mountaya.client.MountayaException
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

/**
 * `transport.events.open-errors`: a printed stream method whose open fails as the case says,
 * answered with the error that failure raised and whether it ended the stream.
 *
 * An open that ends the stream throws its error from the collection. One that reopens it does not:
 * an agent run's stream is then answered the same way until it stops, after six reopens or, for a
 * 503, two minutes from the first, and throws the failure its last open failed with, and a collaboration feed, which never stops on its own, is
 * answered with a 404 on its reopen, the error the case's answer raises being read off the same
 * answer to a request, which is the contract's statement of what it is.
 */
object TransportEventsOpenErrorsRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val stream = input.text("stream")
        val refusedLocally = Scripted.flag(input, "refusedLocally")
        val status = input.numberOrNull("status")?.toInt()
        val reply =
            if (status == null) {
                Scripted.Reply.Drop()
            } else {
                Scripted.Reply.Answer(status, body = Scripted.errorBody(input.textOrNull("code")))
            }
        val notFound = Scripted.Reply.Answer(404, body = Scripted.errorBody("NOT_FOUND"))
        val exchange =
            Scripted.Exchange(
                scope,
                if (stream ==
                    "collaboration"
                ) {
                    listOf(reply, notFound)
                } else {
                    emptyList()
                },
                exhausted = reply,
            )
        val credential = if (refusedLocally) Credential.Publishable("pk_test_abc") else Scripted.session
        // Every wait is drawn at its ceiling, so the opens an agent run's reader holds to two minutes
        // move the test's clock toward them; a draw of zero would wait no time, and never reach them.
        val followed =
            Streams.follow(
                stream,
                Scripted.client(scope, exchange, credential, random = Scripted.Random(1.0)),
                null,
            )
        val opens = exchange.sent.size
        val ends = opens <= 1
        val error =
            if (stream == "collaboration" && !ends) {
                val request = Scripted.Exchange(scope, listOf(reply))
                try {
                    Scripted
                        .client(scope, request, Scripted.session, Scripted.noRetries)
                        .send(aRead, "/v1/things", JsonElement.serializer(), JsonElement.serializer().nullable)
                    null
                } catch (raised: MountayaException) {
                    raised
                }
            } else {
                followed.failure
            } ?: throw AssertionError("the open raised nothing")
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "class" to FixtureValue.Text(error.name),
                "code" to Scripted.text((error as? ApiException)?.code?.value),
                "ends" to FixtureValue.Bool(ends),
            ),
        )
    }
}
