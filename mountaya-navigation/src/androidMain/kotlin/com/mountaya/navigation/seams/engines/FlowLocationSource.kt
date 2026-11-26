package com.mountaya.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * FlowLocationSource states each position an app's own [positions] emit between a start and a stop, for
 * an app that already reads the device's location through its own stack: a `callbackFlow` over its fused
 * location client, or a test's flow of fixes. It is `sdk-dart`'s `LocationSource.fromStream` and
 * `sdk-swift`'s `StreamLocationSource`.
 *
 * Each start collects [positions] in a coroutine of [scope], the caller's, and each stop cancels it, so a
 * cold flow runs only while a navigation does, and a navigation started again collects it again: a cold
 * flow from its beginning, a shared one from where it stands. [scope]'s dispatcher is the thread the
 * engine is driven from, the main thread for `viewModelScope` or `lifecycleScope`, so each position is
 * stated there.
 *
 * Nothing it hears reaches [scope] as a failure, so a navigation never takes the app's screen down with
 * it. A position the engine refuses, one that names no place, is told to [onError] and the next one is
 * read; a failure of [positions], an `Error` it throws among them and a cancellation of its own, ends the
 * positions and is told to [onError] too, the cancellation of a stop aside. With no [onError], each is
 * dropped.
 *
 * ```kotlin
 * val location =
 *     FlowLocationSource(viewModelScope, yourFixes.map { listOf(it.longitude, it.latitude) }) { failure ->
 *         report(failure)
 *     }
 * ```
 *
 * @param scope Where [positions] is collected, the caller's.
 * @param positions The app's positions, `[longitude, latitude]` in degrees.
 * @param onError Told each position the engine refuses and the failure that ends [positions], on [scope]'s
 * dispatcher, or null to drop each.
 */
public class FlowLocationSource(
    private val scope: CoroutineScope,
    private val positions: Flow<List<Double>>,
    private val onError: ((Throwable) -> Unit)? = null,
) : LocationSource {
    private var collecting: Job? = null

    /** Begins stating each position [positions] emits. Starting a source that is started does nothing. */
    override fun start(deliver: (position: List<Double>) -> Unit) {
        if (collecting != null) return
        collecting =
            scope.launch {
                try {
                    positions.collect { position ->
                        try {
                            deliver(position)
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (refused: Exception) {
                            // A position the engine refuses ends nothing: the next one is read.
                            onError?.invoke(refused)
                        }
                    }
                } catch (failure: Throwable) {
                    // A stop's cancellation ends the positions and is no failure; anything else the flow
                    // threw, a cancellation of its own among them, is told.
                    currentCoroutineContext().ensureActive()
                    onError?.invoke(failure)
                }
            }
    }

    /** Ends the collection: nothing more is stated until the next [start]. */
    override fun stop() {
        collecting?.cancel()
        collecting = null
    }

    override fun toString(): String = "FlowLocationSource(collecting=${collecting != null})"
}
