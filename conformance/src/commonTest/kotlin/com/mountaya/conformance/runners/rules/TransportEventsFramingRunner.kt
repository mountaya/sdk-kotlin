package com.mountaya.conformance

import com.mountaya.client.EventRecord
import com.mountaya.client.NotFoundException
import kotlinx.coroutines.test.TestScope
import kotlin.time.Duration

/**
 * `transport.events.framing`: a stream opened by its declaration on a scripted exchange whose first
 * open writes the case's chunks and closes, answered with the records the stream handed on and the
 * `Last-Event-ID` the reopen that follows sent, the reopen being answered with a 404 that ends the
 * stream.
 */
object TransportEventsFramingRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val chunks =
            case.fields.items("chunks").map { chunk ->
                when (chunk) {
                    is FixtureValue.Text -> {
                        chunk.value.encodeToByteArray()
                    }

                    is FixtureValue.Array -> {
                        chunk.items
                            .map { byte ->
                                val value =
                                    (byte as? FixtureValue.Number)?.value ?: throw AssertionError("a byte is no number")
                                if (value !in 0.0..255.0 || value != kotlin.math.floor(value)) {
                                    throw AssertionError("a byte is no number from 0 to 255")
                                }
                                value.toInt().toByte()
                            }.toByteArray()
                    }

                    else -> {
                        throw AssertionError("a chunk is neither text nor bytes")
                    }
                }
            }
        val exchange =
            Scripted.Exchange(
                scope,
                listOf(
                    Scripted.Reply.Stream(
                        200,
                        Scripted.BodyScript(
                            chunks.map { Duration.ZERO to it },
                            Scripted.BodyScript.End.Closes(Duration.ZERO),
                        ),
                    ),
                    Scripted.Reply.Answer(404, body = Scripted.errorBody("NOT_FOUND")),
                ),
            )
        val client = Scripted.client(scope, exchange, Scripted.session)
        val records = mutableListOf<EventRecord>()
        try {
            client.events(Streams.aStream, "/v1/things/events").collect { records += it.event }
        } catch (notFound: NotFoundException) {
            // The reopen's 404 ends the stream, which is where the case stops reading.
        }
        if (exchange.sent.size !=
            2
        ) {
            throw AssertionError("the stream was opened ${exchange.sent.size} times, not twice")
        }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "lastEventId" to Scripted.text(exchange.sent[1].request.header("Last-Event-ID")),
                "records" to
                    FixtureValue.Array(
                        records.map {
                            Scripted.objectOf(
                                "data" to FixtureValue.Text(it.data),
                                "event" to FixtureValue.Text(it.event ?: ""),
                                "id" to FixtureValue.Text(it.id ?: ""),
                            )
                        },
                    ),
            ),
        )
    }
}
