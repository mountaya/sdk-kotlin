package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * SessionTokenEngine keeps a session token fresh: it mints one through the application's own
 * endpoint, and mints the next before the token expires, never after a 401.
 *
 * It never mints by itself: minting exchanges a secret key, which belongs on a server, so the
 * engine is given [mint], a function calling the application's endpoint, which answers what
 * `postSession` answers: the token, its expiry and the user it was minted for.
 *
 * It is read by its [snapshot], one `StateFlow` as an engine's `state` is: its `value` is the state now,
 * readable from any thread without suspending, and its collection is the subscription. The
 * subscriptions are its lifecycle: it mints only while one is collected, and the last one ending parks
 * it, the token kept, so a credential nothing reads costs no call, and reading the snapshot's `value`
 * subscribes to nothing. A `Mountaya` whose credential carries it collects it for as long as the client
 * is open.
 *
 * A mint that fails keeps the token held, its failure the snapshot's last error, and waits as a token
 * that arrived already spent does: the first failed or spent mint of a run waits the schedule's floor,
 * five seconds, and each one after it twice as long as the one before, up to the schedule's margin, a
 * minute. A token the schedule arms a timer for ends the run, so the next failure waits the floor
 * again. A timeout the application set on its own call, the `TimeoutCancellationException` its
 * `withTimeout` throws, is a failure like any other, waited and minted again on that schedule. Any
 * other cancellation the mint throws, the application cancelling its own call, is not minted again:
 * the cancellation is the snapshot's last error, nothing is minting, and the next mint is a
 * [refresh]'s, the next subscription's or an [enable]'s, a subscription arriving then minting
 * whatever other subscription was held all along.
 *
 * It runs in the caller's [scope], which a consumer cancels to stop it for good, the mint in flight
 * cancelled with it, and waits on that scope's dispatcher. [clock] is the wall clock an expiry is read against, re-read before every
 * hop of a wait, so a device that slept through its timer mints on waking.
 */
public class SessionTokenEngine(
    scope: CoroutineScope,
    mint: suspend () -> RestSchema.Session,
    private val clock: Clock = SystemDefaults.clock,
) : SessionTokenSource {
    /** What decides whether the loop runs: enabled or not, and how many refreshes were asked for. */
    private class Control(
        val isEnabled: Boolean,
        val refreshes: Int,
    )

    private val state = MutableStateFlow(SessionSnapshot())
    private val control = MutableStateFlow(Control(isEnabled = true, refreshes = 0))

    /**
     * Moves whenever a loop is stopped, so a mint that lands after its loop was told to stop
     * changes nothing.
     */
    private val generation = MutableStateFlow(0)

    /**
     * Moves each time a subscription arrives, read off the subscriptions' count rising, so a run the
     * application's cancellation ended starts again for the next one.
     */
    private val arrivals = MutableStateFlow(0)

    /**
     * The engine's state: its `value` is the token now, its expiry, the user it names, whether a mint is
     * on its way and the last mint's failure, read without minting anything, and collecting it is the
     * subscription that keeps the engine minting.
     */
    override val snapshot: StateFlow<SessionSnapshot> = state.asStateFlow()

    /**
     * The function the engine mints through. Replacing it does not mint: a composable that passes a
     * new lambda on every recomposition would otherwise call the endpoint on every recomposition.
     */
    @Volatile
    public var mint: suspend () -> RestSchema.Session = mint

    private val scope = scope

    init {
        scope.launch {
            var held = 0
            state.subscriptionCount.collect { count ->
                if (count > held) arrivals.update { it + 1 }
                held = count
            }
        }
        scope.launch {
            var seenRefreshes = 0
            combine(state.subscriptionCount.map { it > 0 }.distinctUntilChanged(), control) { subscribed, control ->
                subscribed to control
            }.collectLatest { (subscribed, control) ->
                val mintAtOnce = control.refreshes != seenRefreshes
                seenRefreshes = control.refreshes
                if (subscribed && control.isEnabled) {
                    val running = generation.value
                    run(running, mintAtOnce)
                } else {
                    generation.update { it + 1 }
                }
            }
        }
    }

    /**
     * Mints a token now, whatever the schedule says, such as after the application's user signed
     * in. It mints only while a subscription is held: with none, the next one starts from the
     * schedule.
     */
    public fun refresh() {
        generation.update { it + 1 }
        control.update { Control(it.isEnabled, it.refreshes + 1) }
    }

    /**
     * Stops minting and clears the token, its expiry, its user and the last error, so a signed-out
     * user's credential cannot be read off the snapshot.
     */
    public fun disable() {
        control.update { Control(isEnabled = false, refreshes = it.refreshes) }
        generation.update { it + 1 }
        state.value = SessionSnapshot()
    }

    /** Lets the engine mint again after [disable], from the next subscription on or at once where one is held. */
    public fun enable() {
        control.update { Control(isEnabled = true, refreshes = it.refreshes) }
    }

    /** Collects the engine's state in its own scope until the returned job is cancelled, which keeps it minting. */
    internal fun hold(): Job = scope.launch { state.collect {} }

    /**
     * Mints, then walks the wait down to the next mint in hops that re-read the wall clock, for as
     * long as the loop is not cancelled. [mintAtOnce] mints first; otherwise a token held with an
     * expiry is minted again on its schedule. A mint the application cancelled parks the run until a
     * subscription arrives, which starts it again as a run that does not mint at once starts.
     */
    private suspend fun run(
        running: Int,
        mintAtOnce: Boolean,
    ) {
        var spent = 0
        var deadline: Instant? = null
        var plan = !mintAtOnce
        while (true) {
            if (plan) {
                plan = false
                spent = 0
                deadline = null
                val held = state.value
                val expiresAt = held.expiresAt
                if (held.token != null && expiresAt != null) {
                    val (next, count) = schedule(expiresAt, spent)
                    deadline = next
                    spent = count
                }
            }
            deadline?.let { due ->
                while (true) {
                    val hop = RefreshSchedule.nextHop(due, clock.now())
                    if (hop <= Duration.ZERO) break
                    delay(hop)
                }
            }
            val minting = mint
            if (!write(running) { SessionSnapshot(it.token, it.expiresAt, it.userId, isMinting = true) }) return
            try {
                val session = minting()
                currentCoroutineContext().ensureActive()
                if (!write(running) { SessionSnapshot(session.token, session.expires_at, session.user_id) }) return
                val (next, count) = schedule(session.expires_at, spent)
                deadline = next
                spent = count
            } catch (timeout: TimeoutCancellationException) {
                // A deadline the application set on its own call is a failed mint, which the schedule
                // waits out and mints again: it is the call's own end, and the engine's coroutine goes on.
                // The engine's own cancellation, arriving with it, ends the loop as it stands.
                currentCoroutineContext().ensureActive()
                if (!write(running) { SessionSnapshot(it.token, it.expiresAt, it.userId, error = timeout) }) return
                spent += 1
                deadline = clock.now() + RefreshSchedule.afterSpentTokens(spent).delay
            } catch (cancelled: CancellationException) {
                // Any other cancellation is never a failed mint, and none is repeated: the engine's own,
                // its scope cancelled or this run superseded, ends the loop as it stands, passing through
                // as the transport and the stream reader pass one through. One the mint raised of its
                // own, the application cancelling its call, ends this run of minting with it as the last
                // error and nothing minting, so a request awaiting a first token goes on at once; the next
                // mint is a refresh's or an enable's, each of which starts the loop anew, or the next
                // subscription's, which this run waits for, whatever subscription was held all along.
                if (!currentCoroutineContext().isActive) throw cancelled
                val seen = arrivals.value
                if (!write(running) { SessionSnapshot(it.token, it.expiresAt, it.userId, error = cancelled) }) return
                arrivals.first { it != seen }
                plan = true
            } catch (failure: Exception) {
                // The engine's own cancellation ends the loop here, where the mint failed as it came.
                currentCoroutineContext().ensureActive()
                if (!write(running) { SessionSnapshot(it.token, it.expiresAt, it.userId, error = failure) }) return
                spent += 1
                deadline = clock.now() + RefreshSchedule.afterSpentTokens(spent).delay
            }
        }
    }

    /**
     * Returns when a token expiring at [expiresAt] is minted again and the run of spent tokens it
     * leaves, counting it where it arrived already spent.
     */
    private fun schedule(
        expiresAt: Instant,
        spent: Int,
    ): Pair<Instant, Int> {
        val now = clock.now()
        val planned = RefreshSchedule.forLifetime(expiresAt - now)
        if (planned.isArmed) return (now + planned.delay) to 0
        val count = spent + 1
        return (now + RefreshSchedule.afterSpentTokens(count).delay) to count
    }

    /**
     * Replaces the snapshot with what [change] makes of it where [running] is still the loop's
     * generation, and returns whether it did.
     */
    private fun write(
        running: Int,
        change: (SessionSnapshot) -> SessionSnapshot,
    ): Boolean {
        var written = false
        state.update { current ->
            written = generation.value == running
            if (written) change(current) else current
        }
        return written
    }
}
