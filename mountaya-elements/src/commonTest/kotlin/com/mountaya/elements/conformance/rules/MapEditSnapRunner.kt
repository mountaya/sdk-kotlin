package com.mountaya.elements

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.ConformanceOutcome
import com.mountaya.conformance.ConformanceRunner
import com.mountaya.conformance.FixtureValue
import com.mountaya.conformance.fixturePosition
import kotlinx.coroutines.test.TestScope

/**
 * `map.edit.snap`: where an edit's magnet places the point the case's `cursor` holds over its
 * `segments`, read through the projection the fixture states, the identity, one degree one pixel. The
 * fixture's `snapThresholdPx` is held to `SNAP_THRESHOLD_PX`, exactly, as the fixture states no
 * tolerance, and a constant it adds or drops fails the entry.
 */
object MapEditSnapRunner : ConformanceRunner {
    private val identity = ScreenProjection(point = { it }, position = { it })

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        holdConstants(case)
        val input = case.fields
        val segments =
            input.items("segments").map { line ->
                (line as? FixtureValue.Array)?.items?.map { fixturePosition(it, "segments") }
                    ?: throw AssertionError("`segments` holds a line that is not an array of positions")
            }
        val snap = snapToActiveSegments(fixturePosition(input["cursor"], "cursor"), segments, identity)
        return ConformanceOutcome.Value(
            FixtureValue.Object(
                listOf(
                    "lngLat" to FixtureValue.Array(snap.position.map { FixtureValue.Number(it) }),
                    "snapped" to FixtureValue.Bool(snap.isSnapped),
                ),
            ),
        )
    }

    /** Holds the fixture's constants to the rule's, the one it declares and no other. */
    private fun holdConstants(case: ConformanceCase) {
        val declared = case.root("constants")
        if (declared.keys != listOf("snapThresholdPx")) {
            throw AssertionError("the fixture declares ${declared.keys}, and the rule states snapThresholdPx alone")
        }
        val threshold = declared.number("snapThresholdPx")
        if (threshold != SNAP_THRESHOLD_PX) {
            throw AssertionError(
                "the fixture declares snapThresholdPx as $threshold, and the rule states $SNAP_THRESHOLD_PX",
            )
        }
    }
}
