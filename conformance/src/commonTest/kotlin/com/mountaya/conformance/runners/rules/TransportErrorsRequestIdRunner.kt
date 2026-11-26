package com.mountaya.conformance

import com.mountaya.client.ApiException
import com.mountaya.client.MountayaException
import com.mountaya.client.ResponseReport
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.JsonElement

/**
 * `transport.errors.request-id`: the case's call sent through a scripted exchange that answers each
 * attempt as the case says, answered with what the `onResponse` hook was told and the request id
 * the error carries.
 *
 * A REST case calls an idempotent read by its declaration, a Data API case the printed
 * `directions`, and a stream case opens the printed collaboration feed of an itinerary.
 */
object TransportErrorsRequestIdRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val surface = input.text("surface")
        val replies =
            input.items("answers").map { item ->
                val answer = item as? FixtureValue.Object ?: throw AssertionError("an answer is no object")
                val status = answer.numberOrNull("status")?.toInt() ?: return@map Scripted.Reply.Drop()
                val headers = answer.textOrNull("requestId")?.let { mapOf("X-Request-Id" to it) }.orEmpty()
                val code = answer.textOrNull("code")
                val body =
                    when {
                        code != null -> Scripted.errorBody(code)
                        surface == "data" -> """{"data":{"directions":{"routes":[]}}}""".encodeToByteArray()
                        else -> Scripted.emptySuccess
                    }
                Scripted.Reply.Answer(status, headers, body)
            }
        val exchange = Scripted.Exchange(scope, replies, exhausted = Scripted.Reply.Drop())
        val reports = mutableListOf<ResponseReport>()
        val error =
            try {
                when (surface) {
                    "rest" -> {
                        Scripted
                            .client(scope, exchange, retry = Scripted.twoQuickRetries, reports = reports)
                            .send(aRead, "/v1/things", JsonElement.serializer(), JsonElement.serializer().nullable)
                    }

                    "data" -> {
                        Scripted
                            .client(scope, exchange, Scripted.session, Scripted.twoQuickRetries, reports = reports)
                            .data
                            .directions(directionsInput)
                    }

                    "stream" -> {
                        Scripted
                            .client(scope, exchange, Scripted.session, Scripted.twoQuickRetries, reports = reports)
                            .studio
                            .collab
                            .getItineraryEvents("it")
                            .collect()
                    }

                    else -> {
                        throw AssertionError("no surface is called `$surface`")
                    }
                }
                null
            } catch (raised: MountayaException) {
                raised
            }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "error" to
                    (
                        error?.let {
                            Scripted.objectOf(
                                "class" to FixtureValue.Text(it.name),
                                "requestId" to Scripted.text((it as? ApiException)?.requestId),
                            )
                        } ?: FixtureValue.Null
                    ),
                "reported" to
                    FixtureValue.Array(
                        reports.map {
                            Scripted.objectOf(
                                "attempt" to Scripted.number(it.attempt),
                                "requestId" to Scripted.text(it.requestId),
                                "status" to Scripted.number(it.status),
                            )
                        },
                    ),
            ),
        )
    }
}
