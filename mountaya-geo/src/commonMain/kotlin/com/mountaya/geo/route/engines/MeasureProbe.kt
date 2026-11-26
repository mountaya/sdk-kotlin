package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi

/**
 * MeasureProbe records every line `cumulativeMeters` measures while a test holds it, so a budget test reads what a
 * burst of work costs in measurements (`route-measure` and `replay-measure` in the contract's `sdk/hot-paths.yaml`): a
 * measurement is a haversine per vertex of the whole line, and a navigation that measured its route on every fix, or a
 * replay on every tick, would pay for its whole length again on the path every fix or tick takes.
 *
 * Nothing is recorded anywhere until a test runs its work through [measuring]. Without one, a measurement reads one
 * field, null, and allocates nothing. A test reads the lines it handed the work by identity, so a line measured
 * meanwhile by work of another test's, on another thread, is no line it counts.
 *
 * It is `InternalMountayaApi` rather than `internal` because a test of `mountaya-navigation` holds it too, the replay's
 * budget counting the measurements a walk makes, and Kotlin's `internal` stops at its module: no app reads it, and it
 * is no part of the published API.
 */
@InternalMountayaApi
public object MeasureProbe {
    /** The lines measured while a test holds the probe, in order, or null while none holds it. */
    private var measured: MutableList<List<List<Double>>>? = null

    /** Records a measurement of [line], where a test holds the probe. */
    internal fun record(line: List<List<Double>>) {
        measured?.add(line)
    }

    /** Runs [work] holding the probe, and answers every line measured meanwhile, in the order it was measured. */
    public fun measuring(work: () -> Unit): List<List<List<Double>>> {
        val lines = mutableListOf<List<List<Double>>>()
        measured = lines
        try {
            work()
        } finally {
            measured = null
        }
        return lines
    }
}
