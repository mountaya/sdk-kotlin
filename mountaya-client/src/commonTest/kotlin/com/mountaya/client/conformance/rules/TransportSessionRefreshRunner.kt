package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant

/**
 * `transport.session.refresh`: when a token is minted again. A case naming `consecutiveSpent`
 * asks the spent-token wait; any other asks for a fresh token's delay, with no `lifetimeMs`
 * meaning an expiry that did not parse. The fixture's `constants` are held to the schedule's
 * own, so the constants are proven too.
 *
 * A case that puts the machine to sleep is walked down hop by hop, as the engine walks it: each
 * hop is armed from the wall clock, a timer runs only while the machine is awake and keeps the time
 * it had left across the sleep, and the wall clock moves on through the sleep. The answer then
 * carries how long the machine was awake before the hop that found the deadline passed.
 *
 * A case naming `mintEndsWith` runs a `SessionTokenEngine`, held by a subscription on the test's
 * virtual time, whose first mint ends that way: a failure of the application's own, a `withTimeout`
 * the application set on its call running out, or the application cancelling its call, which throws
 * a `CancellationException` out of the mint. It answers whether the engine called the mint again
 * once the margin passed.
 *
 * A case naming `mints` plays a run of them on an engine held the same way, each ending as listed,
 * a spent token answering an expiry of now and a healthy one living `lifetimeMs`, an hour where the
 * case names none, a cancelled one throwing the `CancellationException` an application's own abort is
 * in Kotlin, with a reason of its own, and stamps each mint's call and end on the virtual clock: it
 * answers the time from each mint's end to the next mint's call; where the run's last mint ends with no
 * token after an earlier one answered one, whether the engine still holds that token; and where the
 * last is cancelled, whether the mint is called again once the margin has passed.
 */
object TransportSessionRefreshRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val constants = case.root("constants")
        agree("refreshMarginMs", constants.number("refreshMarginMs"), RefreshSchedule.margin)
        agree("minTimerMs", constants.number("minTimerMs"), RefreshSchedule.floor)
        agree("maxTimerMs", constants.number("maxTimerMs"), RefreshSchedule.longestHop)

        val input = case.fields
        if (input["mints"] != null) {
            val endings =
                input.items("mints").map { ending ->
                    (ending as? FixtureValue.Text)?.value ?: throw AssertionError("a mint ends with no word")
                }
            val lifetime = input.numberOrNull("lifetimeMs")?.milliseconds ?: 1.hours
            return ConformanceOutcome.Value(playedRun(scope, endings, lifetime))
        }
        val ends = input.textOrNull("mintEndsWith")
        if (ends != null) {
            return ConformanceOutcome.Value(
                Scripted.objectOf("mintedAgain" to FixtureValue.Bool(mintedAgain(scope, ends))),
            )
        }
        val spent = input.numberOrNull("consecutiveSpent")
        val schedule =
            if (spent != null) {
                RefreshSchedule.afterSpentTokens(spent.toInt())
            } else {
                RefreshSchedule.forLifetime(input.numberOrNull("lifetimeMs")?.milliseconds)
            }
        val answer =
            mutableListOf(
                "armed" to FixtureValue.Bool(schedule.isArmed),
                "delayMs" to FixtureValue.Number(schedule.delay.toDouble(DurationUnit.MILLISECONDS)),
            )
        val sleepsAt = input.numberOrNull("sleepsAtMs")
        if (sleepsAt != null) {
            val awake =
                awakeBeforeRefresh(schedule.delay, sleepsAt.milliseconds, input.number("sleepsForMs").milliseconds)
            answer += "awakeMsBeforeRefresh" to FixtureValue.Number(awake.toDouble(DurationUnit.MILLISECONDS))
        }
        return ConformanceOutcome.Value(FixtureValue.Object(answer))
    }

    /**
     * Walks a wait of [delay] down in hops from a mint at the epoch, the machine going to sleep once
     * it has been awake [sleepsAt] and waking [sleepsFor] later, and returns how long it was awake
     * when the next hop found the deadline passed. A machine that falls asleep at the very instant a
     * hop ends is asleep for the next hop.
     */
    private fun awakeBeforeRefresh(
        delay: Duration,
        sleepsAt: Duration,
        sleepsFor: Duration,
    ): Duration {
        val deadline = Instant.fromEpochMilliseconds(0) + delay
        var wall = Instant.fromEpochMilliseconds(0)
        var awake = Duration.ZERO
        var slept = false
        var hops = 0
        while (true) {
            val hop = RefreshSchedule.nextHop(deadline, wall)
            if (hop == Duration.ZERO) return awake
            hops += 1
            if (hops > 10_000) throw AssertionError("the wait was never walked down")
            if (!slept && awake <= sleepsAt && sleepsAt < awake + hop) {
                slept = true
                wall += sleepsFor
            }
            awake += hop
            wall += hop
        }
    }

    /** Whether an engine whose first mint [ends] as the case says calls its mint again within the margin. */
    private suspend fun mintedAgain(
        scope: TestScope,
        ends: String,
    ): Boolean {
        if (ends !in setOf("failure", "timeout", "cancellation")) throw AssertionError("no mint ends with `$ends`")
        var mints = 0
        val clock = Scripted.clock(scope)
        val engine =
            SessionTokenEngine(
                scope.backgroundScope,
                mint = {
                    mints += 1
                    if (mints == 1) {
                        when (ends) {
                            "failure" -> throw IllegalStateException("the application's endpoint failed")
                            "timeout" -> withTimeout(10.seconds) { awaitCancellation() }
                            else -> throw CancellationException("the application cancelled its call")
                        }
                    }
                    RestSchema.Session(expires_at = clock.now() + 1.hours, token = "sess_abc", user_id = null)
                },
                clock = clock,
            )
        val subscription = scope.backgroundScope.launch { engine.snapshot.collect() }
        delay(RefreshSchedule.margin)
        subscription.cancel()
        return mints > 1
    }

    /**
     * Plays a run of mints on an engine held by a subscription on the test's virtual time, each ending as
     * [endings] lists in order, a healthy token living [lifetime], and answers the time from each mint's end
     * to the next mint's call, for every mint but the last. Where the last ends with no token after an
     * earlier one answered one, it answers too whether the engine still holds that token.
     */
    private suspend fun playedRun(
        scope: TestScope,
        endings: List<String>,
        lifetime: Duration,
    ): FixtureValue {
        val known = setOf("failure", "timeout", "spent", "healthy", "cancelled")
        endings.firstOrNull { it !in known }?.let { throw AssertionError("no mint ends with `$it`") }
        if (endings.isEmpty()) throw AssertionError("the run plays no mint")
        val clock = Scripted.clock(scope)
        val calls = mutableListOf<Duration>()
        val ends = mutableListOf<Duration>()
        val played = CompletableDeferred<Unit>()
        val engine =
            SessionTokenEngine(
                scope.backgroundScope,
                mint = {
                    val index = calls.size
                    calls += Scripted.elapsed(scope)
                    // A mint past the run's last is the next refresh, which the case does not wait for.
                    val ending = endings.getOrNull(index) ?: awaitCancellation()
                    try {
                        when (ending) {
                            "failure" -> throw IllegalStateException("the application's endpoint failed")

                            "timeout" -> withTimeout(10.seconds) { awaitCancellation() }

                            // The application's own abort, Kotlin's idiom for it, with a reason of its own.
                            "cancelled" -> throw CancellationException("the application stopped asking: the user left")

                            "spent" -> session(clock.now(), index)

                            else -> session(clock.now() + lifetime, index)
                        }
                    } finally {
                        ends += Scripted.elapsed(scope)
                        if (index == endings.lastIndex) played.complete(Unit)
                    }
                },
                clock = clock,
            )
        val subscription = scope.backgroundScope.launch { engine.snapshot.collect() }
        played.await()
        // The engine reads the last mint's end before the case reads what it holds.
        scope.testScheduler.runCurrent()
        val answer =
            mutableListOf<Pair<String, FixtureValue>>(
                "waitsMs" to
                    FixtureValue.Array(
                        (0 until endings.lastIndex).map { index ->
                            Scripted.milliseconds(calls[index + 1] - ends[index])
                        },
                    ),
            )
        val answered = endings.dropLast(1).indexOfLast { it == "spent" || it == "healthy" }
        if (endings.last() in setOf("failure", "timeout", "cancelled") && answered >= 0) {
            answer.add(0, "keepsToken" to FixtureValue.Bool(engine.snapshot.value.token == "sess_$answered"))
        }
        // A cancellation ends the run, which the case asks of once the margin has passed.
        if (endings.last() == "cancelled") {
            delay(RefreshSchedule.margin)
            answer.add(0, "mintedAgain" to FixtureValue.Bool(calls.size > endings.size))
        }
        subscription.cancel()
        return FixtureValue.Object(answer)
    }

    /** The session the mint [index] of a run answers, expiring at [expiresAt]. */
    private fun session(
        expiresAt: Instant,
        index: Int,
    ): RestSchema.Session = RestSchema.Session(expires_at = expiresAt, token = "sess_$index", user_id = null)

    private fun agree(
        name: String,
        fixture: Double,
        schedule: Duration,
    ) {
        val own = schedule.toDouble(DurationUnit.MILLISECONDS)
        if (fixture != own) throw AssertionError("the fixture's $name is $fixture, the schedule's is $own")
    }
}
