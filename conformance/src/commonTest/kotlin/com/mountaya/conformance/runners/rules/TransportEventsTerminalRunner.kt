package com.mountaya.conformance

import kotlinx.coroutines.test.TestScope
import kotlin.time.Duration

/**
 * `transport.events.terminal`: the case's frames read off one open of a printed stream method,
 * answered with where the stream ended and how, what it surfaced, the events its reader was handed,
 * by the name each is sent under, and whether and from where it opened the stream again, the reopen
 * being answered with a 404 that ends it.
 */
object TransportEventsTerminalRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val stream = input.text("stream")
        val frames =
            input.items("frames").map {
                it as? FixtureValue.Object
                    ?: throw AssertionError("a frame is no object")
            }
        val text =
            frames.joinToString("") { frame ->
                Streams.sse(
                    frame.textOrNull("event"),
                    frame.textOrNull("id"),
                    frame["data"]?.let { fixtureJson(it).toString() },
                )
            }
        val exchange =
            Scripted.Exchange(
                scope,
                listOf(
                    Scripted.Reply.Stream(
                        200,
                        Scripted.BodyScript(
                            listOf(Duration.ZERO to text.encodeToByteArray()),
                            Scripted.BodyScript.End.Closes(Duration.ZERO),
                        ),
                    ),
                    Scripted.Reply.Answer(404, body = Scripted.errorBody("NOT_FOUND")),
                ),
            )
        val client = Scripted.client(scope, exchange, Scripted.session)
        val followed = Streams.follow(stream, client, input.numberOrNull("handlerFailsAt")?.toInt())
        var endedAt: FixtureValue = FixtureValue.Null
        if (stream == "agent" && followed.reason != null) {
            val index = frames.indexOfFirst { it.textOrNull("event") == "done" }
            if (index < 0) throw AssertionError("the stream ended with no `done` among its frames")
            endedAt = Scripted.number(index)
        }
        val reopened = exchange.sent.size > 1
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "endedAt" to endedAt,
                "handed" to textArray(followed.types),
                "reason" to Scripted.text(followed.reason),
                "reopened" to FixtureValue.Bool(reopened),
                "resumedFrom" to
                    (
                        if (reopened) {
                            Scripted.text(
                                exchange.sent[1].request.header("Last-Event-ID"),
                            )
                        } else {
                            FixtureValue.Null
                        }
                    ),
                "surfaced" to FixtureValue.Array(followed.surfaced.map { FixtureValue.Text(it) }),
            ),
        )
    }
}
