package com.mountaya.geo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * GeolocateControlEngine is the state of a control that centres a map on the device
 * (`geo.controls.geolocate-state`): whether a fix is out, whether location stands refused, and whether a
 * press has a map to ask.
 *
 * - **Busy from a press until its fix settles.** Each press asks the map for one fix, and the control is busy
 *   while any fix it asked for is out, a map replaced meanwhile included.
 * - **Live after a refusal.** A refusal changes nothing a press needs, so the control stays pressable: a
 *   disabled control offers no way back, where a live one can say where the setting is and be pressed again
 *   once it changed.
 * - **Blocked only on the permission's word.** The standing permission is read through the map, which prompts
 *   nobody, when the control comes to stand over a map and again once each fix asked of that map settles while
 *   it still stands over it, and the control says location is blocked while the latest read answers refused. A
 *   fix that timed out or found no signal fails with location still allowed, and is no refusal of the
 *   permission; a fix refused while the permission was never asked, which nothing prompts for, leaves it never
 *   asked. A read that throws says nothing is refused, one that never answers leaves what was said as it stood,
 *   an answer from an earlier read or from a map the control no longer stands over lands nowhere, and a control
 *   standing over another map, or none, says nothing is blocked until its permission is read.
 *
 * ```kotlin
 * val control = GeolocateControlEngine(scope, map)
 * control.locate(onError = { error -> shown = error })
 * ```
 *
 * It reads the permission as soon as it is handed a map, by its constructor as by [configure], and keeps no
 * time. Each read and each fix runs in a coroutine of its own in [scope], the caller's, whose dispatcher is the
 * thread the engine and its map are driven from, the main thread in a composition. [state] holds a state only
 * where it differs from the one before it. Closing the engine cancels nothing it asked for and lands nothing
 * more; a closed engine is inert. It is an `AutoCloseable`, as every engine is, so `use` closes it.
 */
public class GeolocateControlEngine(
    private val scope: CoroutineScope,
    map: GeolocateControlTarget? = null,
) : AutoCloseable {
    private val held = MutableStateFlow(GeolocateControlState(isLive = map != null))

    /** What the control shows. */
    public val state: StateFlow<GeolocateControlState> = held.asStateFlow()

    /** The map the control stands over, or null for none. */
    private var map: GeolocateControlTarget? = map

    /** How many fixes the control asked for are still out. */
    private var fixesOut = 0

    /** The number of the latest permission read, the one alone whose answer lands. */
    private var latestRead = 0
    private var closed = false

    init {
        readPermission()
    }

    /**
     * Stands the control over [map]. The map it stands over already changes nothing; another one, null included, drops
     * every read still out, says nothing is blocked, and reads the new map's permission. A fix still out stays the
     * control's own.
     */
    public fun configure(map: GeolocateControlTarget?) {
        if (closed || map === this.map) return
        this.map = map
        latestRead += 1
        held.value = GeolocateControlState(isBusy = held.value.isBusy, isBlocked = false, isLive = map != null)
        readPermission()
    }

    /**
     * Asks the map the control stands over for one fix, and does nothing with no map. Once the fix settles the
     * permission is read again through that map, while the control still stands over it, and a fix that failed is
     * handed to [onError] with what the map threw, unless the engine was closed by then.
     */
    public fun locate(onError: ((Throwable) -> Unit)? = null) {
        val map = map ?: return
        if (closed) return
        fixesOut += 1
        held.value = GeolocateControlState(isBusy = true, isBlocked = held.value.isBlocked, isLive = held.value.isLive)
        scope.launch {
            val failure =
                try {
                    map.locate()
                    null
                } catch (failure: Throwable) {
                    // A fix the scope cancelled settles for nobody; any other failure is the fix's.
                    currentCoroutineContext().ensureActive()
                    failure
                }
            if (closed) return@launch
            fixesOut -= 1
            val state = held.value
            held.value =
                GeolocateControlState(isBusy = fixesOut > 0, isBlocked = state.isBlocked, isLive = state.isLive)
            // A fix that settled may have changed the standing answer: a prompt the reader refused now stands
            // refused, and a setting they changed back stands allowed.
            if (map === this@GeolocateControlEngine.map) readPermission()
            if (failure != null) onError?.invoke(failure)
        }
    }

    /**
     * Stops the engine for good: no answer lands after it, no failure is handed on, and every later command does
     * nothing. The state stays as it was. Closing twice does nothing more.
     */
    override fun close() {
        if (closed) return
        closed = true
        latestRead += 1
        map = null
    }

    /** Reads the standing permission of the map the control stands over, and writes its answer while the read is still the latest. */
    private fun readPermission() {
        val map = map ?: return
        if (closed) return
        latestRead += 1
        val read = latestRead
        scope.launch {
            val isRefused =
                try {
                    map.isLocationRefused()
                } catch (failure: Throwable) {
                    currentCoroutineContext().ensureActive()
                    false
                }
            if (closed || latestRead != read) return@launch
            val state = held.value
            held.value = GeolocateControlState(isBusy = state.isBusy, isBlocked = isRefused, isLive = state.isLive)
        }
    }
}
