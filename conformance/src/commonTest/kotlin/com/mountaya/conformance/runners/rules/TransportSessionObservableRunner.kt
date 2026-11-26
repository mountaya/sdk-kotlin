package com.mountaya.conformance

import com.mountaya.client.SessionTokenEngine
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlin.time.Duration.Companion.hours

/**
 * `transport.session.observable`: a `SessionTokenEngine` whose mint hands back the case's tokens
 * in order, each only once the runner lets it land, answered with what its snapshot and its
 * collectors report.
 *
 * The engine runs in the test's background scope, on its virtual time and a wall clock that reads
 * it; every wait here is for a change the runner caused, bounded so a collector that is never told
 * fails the case rather than hanging it. `late` and `behind` are reads of a collector's own: what a
 * collector that subscribes once a token has landed reads first and last, and what one that stops
 * reading once it has read the first token reads once it reads again, after two more have landed.
 */
object TransportSessionObservableRunner : ConformanceRunner {
    /** Reader collects the engine's state in a job of its own, keeping each token it is told of. */
    private class Reader(
        scope: TestScope,
        engine: SessionTokenEngine,
    ) {
        val tokens = mutableListOf<String?>()
        private val job: Job = scope.backgroundScope.launch { engine.snapshot.collect { tokens += it.token } }

        fun stop() = job.cancel()
    }

    /** Whether [condition] holds within a virtual minute of the runner's own waits. */
    private suspend fun waits(condition: () -> Boolean): Boolean {
        repeat(600) {
            if (condition()) return true
            delay(100)
        }
        return condition()
    }

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val tokens = ArrayDeque(fixtureTexts(input.items("tokens"), "`tokens`"))
        val first = tokens.first()
        val second = tokens.getOrNull(1)
        val third = tokens.getOrNull(2)
        val gate = Channel<Unit>(Channel.UNLIMITED)
        val clock = Scripted.clock(scope)
        val engine =
            SessionTokenEngine(scope.backgroundScope, mint = {
                gate.receive()
                val token =
                    tokens.removeFirstOrNull()
                        ?: throw AssertionError("the engine minted more tokens than the case holds")
                RestSchema.Session(
                    expires_at = clock.now() + 1.hours,
                    token = token,
                    user_id = "7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1",
                )
            }, clock = clock)

        return when (val ask = input.text("ask")) {
            "read" -> {
                ConformanceOutcome.Value(Scripted.objectOf("token" to Scripted.text(engine.snapshot.value.token)))
            }

            "observe" -> {
                val reader = Reader(scope, engine)
                val before = engine.snapshot.value.token
                gate.send(Unit)
                val notified = waits { first in reader.tokens }
                ConformanceOutcome.Value(
                    Scripted.objectOf(
                        "after" to Scripted.text(engine.snapshot.value.token),
                        "before" to Scripted.text(before),
                        "notified" to FixtureValue.Bool(notified),
                    ),
                )
            }

            "stable" -> {
                val reader = Reader(scope, engine)
                val earlier = engine.snapshot.value
                val later = engine.snapshot.value
                gate.send(Unit)
                waits { first in reader.tokens }
                val changed = engine.snapshot.value
                reader.stop()
                ConformanceOutcome.Value(
                    Scripted.objectOf(
                        "changedAfterAChange" to FixtureValue.Bool(changed != later),
                        "sameBetweenChanges" to FixtureValue.Bool(earlier == later),
                    ),
                )
            }

            "unsubscribe" -> {
                if (second == null) throw AssertionError("the case mints fewer than two tokens")
                val leaving = Reader(scope, engine)
                val staying = Reader(scope, engine)
                gate.send(Unit)
                val whileSubscribed = waits { first in leaving.tokens }
                waits { first in staying.tokens }
                val toldBefore = leaving.tokens.size
                leaving.stop()
                engine.refresh()
                gate.send(Unit)
                val remaining = waits { second in staying.tokens }
                val toldAfter = leaving.tokens.size
                staying.stop()
                ConformanceOutcome.Value(
                    Scripted.objectOf(
                        "after" to Scripted.text(engine.snapshot.value.token),
                        "notifiedAfterUnsubscribe" to FixtureValue.Bool(toldAfter > toldBefore),
                        "notifiedWhileSubscribed" to FixtureValue.Bool(whileSubscribed),
                        "remainingSubscriberNotified" to FixtureValue.Bool(remaining),
                    ),
                )
            }

            "late" -> {
                if (second == null) throw AssertionError("the case mints fewer than two tokens")
                val early = Reader(scope, engine)
                gate.send(Unit)
                waits { first in early.tokens }
                val late = Reader(scope, engine)
                waits { late.tokens.isNotEmpty() }
                val firstRead = late.tokens.firstOrNull()
                engine.refresh()
                gate.send(Unit)
                waits { second in late.tokens }
                val lastRead = late.tokens.lastOrNull()
                early.stop()
                late.stop()
                ConformanceOutcome.Value(
                    Scripted.objectOf(
                        "firstRead" to Scripted.text(firstRead),
                        "lastRead" to Scripted.text(lastRead),
                    ),
                )
            }

            "behind" -> {
                if (second == null || third == null) throw AssertionError("the case mints fewer than three tokens")
                val resume = Channel<Unit>(Channel.UNLIMITED)
                var readBefore: String? = null
                var readAfter: String? = null
                var hasRead = false
                val reader =
                    scope.backgroundScope.launch {
                        engine.snapshot.collect { snapshot ->
                            if (!hasRead) {
                                if (snapshot.token == first) {
                                    readBefore = snapshot.token
                                    hasRead = true
                                    // The reader reads nothing until the runner lets it read again.
                                    resume.receive()
                                }
                            } else if (readAfter == null) {
                                readAfter = snapshot.token
                            }
                        }
                    }
                gate.send(Unit)
                waits { hasRead }
                engine.refresh()
                gate.send(Unit)
                waits { engine.snapshot.value.token == second && !engine.snapshot.value.isMinting }
                engine.refresh()
                gate.send(Unit)
                waits { engine.snapshot.value.token == third && !engine.snapshot.value.isMinting }
                resume.send(Unit)
                waits { readAfter != null }
                reader.cancel()
                ConformanceOutcome.Value(
                    Scripted.objectOf(
                        "readAfterFallingBehind" to Scripted.text(readAfter),
                        "readBeforeFallingBehind" to Scripted.text(readBefore),
                    ),
                )
            }

            else -> {
                throw AssertionError("no ask is called `$ask`")
            }
        }
    }
}
