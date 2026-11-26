package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/**
 * `geo.controls.geolocate-state`: plays the case's `steps` over one locate control and the maps it names, every
 * permission read and every fix held until a step settles it, numbered across maps, and answers the map each read
 * and each fix was sent to and what the control shows after each step, with the failed fixes reported so far.
 *
 * A permission read answers whether the standing permission refuses a fix: `denied` does, and `granted`, `prompt`
 * and `unknown` do not; `fails` throws. A fix refused for any reason throws. The control's engine runs in the case's
 * background scope, so each step is settled by running what it queued, never by a clock. The entry is one a
 * consumer only observes, as a control's word, so it runs from Geo's own tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object GeoControlsGeolocateStateRunner : ConformanceRunner {
    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdNoConstants(case)
        val maps = LocateMaps()
        val engine = GeolocateControlEngine(scope.backgroundScope)
        var reported = 0
        val snapshots = mutableListOf<FixtureValue>()
        try {
            for (item in case.fields.items("steps")) {
                val step = item as? FixtureValue.Object ?: throw AssertionError("a step is not an object")
                when (val name = step.text("step")) {
                    "map" -> engine.configure(step.textOrNull("map")?.let(maps::map))
                    "press" -> engine.locate(onError = { reported += 1 })
                    "fix" -> maps.settle(whole(step, "fix"), step.text("answer"))
                    "permission" -> maps.answer(whole(step, "read"), step.text("answer"))
                    else -> throw AssertionError("`$name` is no step")
                }
                scope.runCurrent()
                val state = engine.state.value
                snapshots +=
                    FixtureValue.Object(
                        listOf(
                            "blocked" to FixtureValue.Bool(state.isBlocked),
                            "busy" to FixtureValue.Bool(state.isBusy),
                            "live" to FixtureValue.Bool(state.isLive),
                            "reported" to FixtureValue.Number(reported.toDouble()),
                        ),
                    )
            }
        } finally {
            engine.close()
            maps.release()
            scope.runCurrent()
        }
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "fixes" to FixtureValue.Array(maps.fixes.map { FixtureValue.Text(it.first) }),
                    "reads" to FixtureValue.Array(maps.reads.map { FixtureValue.Text(it.first) }),
                    "snapshots" to FixtureValue.Array(snapshots),
                ),
            ),
        )
    }

    /** The whole number [step] writes at [key]. */
    private fun whole(
        step: FixtureValue.Object,
        key: String,
    ): Int {
        val number = step.number(key)
        if (number != kotlin.math.floor(number)) throw AssertionError("`$key` is no whole number")
        return number.toInt()
    }

    /**
     * LocateMaps stands for the maps a locate control stands over, each named, every permission read and every fix
     * numbered from 0 in the order sent, across maps, and held until a step settles it.
     */
    private class LocateMaps {
        /** Refused is what a fix refused, or a read that fails, throws, naming the reason. */
        class Refused(
            reason: String,
        ) : Exception(reason)

        private val named = mutableMapOf<String, GeolocateControlTarget>()
        val reads = mutableListOf<Pair<String, CompletableDeferred<Boolean>>>()
        val fixes = mutableListOf<Pair<String, CompletableDeferred<List<Double>>>>()

        /** The map [name] names, made the first time it is named. */
        fun map(name: String): GeolocateControlTarget =
            named.getOrPut(name) {
                object : GeolocateControlTarget {
                    override suspend fun locate(): List<Double> {
                        val fix = CompletableDeferred<List<Double>>()
                        fixes += name to fix
                        return fix.await()
                    }

                    override suspend fun isLocationRefused(): Boolean {
                        val read = CompletableDeferred<Boolean>()
                        reads += name to read
                        return read.await()
                    }
                }
            }

        /** Answers the read [read] with the standing permission [answer]. */
        fun answer(
            read: Int,
            answer: String,
        ) {
            val held = reads.getOrNull(read)?.second
            if (held == null ||
                held.isCompleted
            ) {
                throw AssertionError("the permission read $read was never sent, or answered")
            }
            when (answer) {
                "denied" -> held.complete(true)
                "granted", "prompt", "unknown" -> held.complete(false)
                "fails" -> held.completeExceptionally(Refused("the seam could not say"))
                else -> throw AssertionError("`$answer` is no permission")
            }
        }

        /** Settles the fix [fix] with [answer], `located` or the reason it was refused. */
        fun settle(
            fix: Int,
            answer: String,
        ) {
            val held = fixes.getOrNull(fix)?.second
            if (held == null || held.isCompleted) throw AssertionError("the fix $fix was never sent, or answered")
            when (answer) {
                "located" -> held.complete(listOf(6.86, 45.83))
                "denied", "no_api", "unavailable" -> held.completeExceptionally(Refused(answer))
                else -> throw AssertionError("`$answer` is no fix's answer")
            }
        }

        /** Answers everything still held, so no coroutine waits for ever once a case ends. */
        fun release() {
            for ((_, read) in reads) read.complete(false)
            for ((_, fix) in fixes) fix.completeExceptionally(Refused("released"))
        }
    }
}
