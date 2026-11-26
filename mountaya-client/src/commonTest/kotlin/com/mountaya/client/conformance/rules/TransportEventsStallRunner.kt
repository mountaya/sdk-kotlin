package com.mountaya.client

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * `transport.events.stall`: a printed stream opened through the reader on the test's virtual time,
 * the open answered and its stream delivering as the case says, answered with how the reader gave
 * the first open up and when, as its observer is told, or `open` where it still holds it when the
 * case stops watching.
 */
object TransportEventsStallRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val constants = case.root("constants")
        if (constants.number("stallWindowMs").milliseconds != EventStreamRules.stallWindow ||
            case.root("defaults").number("timeoutMs").milliseconds != AttemptBudget.clientDefault
        ) {
            throw AssertionError("the fixture's window or default budget is not the reader's")
        }
        val input = case.fields
        val stream = input.text("stream")
        val timeout = input.numberOrNull("timeoutMs")?.milliseconds
        val answersAfter = input.numberOrNull("answersAfterMs")?.milliseconds
        val reply =
            if (answersAfter == null) {
                Scripted.Reply.Never
            } else {
                val event =
                    if (stream == "agent") {
                        FixtureValue.Object(listOf("event" to FixtureValue.Text("message-delta")))
                    } else {
                        FixtureValue.Object(
                            listOf(
                                "event" to FixtureValue.Text("segment.changed"),
                                "id" to FixtureValue.Text("1-0"),
                            ),
                        )
                    }
                val chunks =
                    input.items("deliveries").map { item ->
                        val delivery = item as? FixtureValue.Object ?: throw AssertionError("a delivery is no object")
                        val at = delivery.number("atMs").milliseconds - answersAfter
                        when (val what = delivery.text("what")) {
                            "comment" -> at to Streams.heartbeat
                            "event" -> at to Streams.record(event).encodeToByteArray()
                            else -> throw AssertionError("no delivery is called `$what`")
                        }
                    }
                Scripted.Reply.Stream(
                    200,
                    Scripted.BodyScript(chunks, Scripted.BodyScript.End.StaysOpen),
                    after = answersAfter,
                )
            }
        val observedFor = input.numberOrNull("observedForMs")?.milliseconds
        val exchange = Scripted.Exchange(scope, listOf(reply), exhausted = Scripted.Reply.Never)
        val transport =
            Streams.transport(scope, exchange, Scripted.session, timeout = timeout ?: AttemptBudget.clientDefault)
        val started = Scripted.elapsed(scope)
        val ended = CompletableDeferred<Pair<String, Duration>>()
        val reader =
            scope.launch {
                try {
                    Streams
                        .frames(stream, transport) { failure ->
                            ended.complete((failure?.name ?: "closed") to Scripted.elapsed(scope) - started)
                        }.collect()
                } catch (error: Exception) {
                    ended.complete(Scripted.className(error) to Scripted.elapsed(scope) - started)
                }
            }
        val settled = withTimeoutOrNull(observedFor ?: Duration.INFINITE) { ended.await() }
        reader.cancel()
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "elapsedMs" to (settled?.second?.let(Scripted::milliseconds) ?: FixtureValue.Null),
                "outcome" to FixtureValue.Text(settled?.first ?: "open"),
            ),
        )
    }
}
