package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class StepsTileTest {
    private fun steps(vararg ranges: IntRange): List<NavigationStep> =
        ranges.map { NavigationStep(it.first, it.last, RestSchema.ManeuverType.Continue) }

    @Test
    fun stepsTileWhereTheyCoverTheGeometryOnceInTravelOrder() {
        assertTrue(stepsTile(steps(0..1, 1..2), 3))
        assertTrue(stepsTile(steps(0..2, 2..2), 3))
        assertTrue(stepsTile(steps(0..0), 1))
    }

    /** A step may start and end on one vertex, wherever it stands. */
    @Test
    fun aStepOfOneVertexTilesItsVertex() {
        assertTrue(stepsTile(steps(0..1, 1..1, 1..2), 3))
        assertTrue(stepsTile(steps(0..0, 0..2), 3))
    }

    @Test
    fun aListThatBreaksTheTilingDoesNotTile() {
        assertFalse(stepsTile(steps(0..2, 2..1, 1..2), 3), "a range reversed")
        assertFalse(stepsTile(steps(0..1, 1..3), 3), "an index past the geometry")
        assertFalse(stepsTile(steps(0..2, 1..3), 4), "two steps overlapping")
        assertFalse(stepsTile(steps(0..1, 2..3, 1..2), 4), "steps out of order")
        assertFalse(stepsTile(steps(0..1, 2..3), 4), "a stretch no step covers")
        assertFalse(stepsTile(steps(0..1, 1..2), 4), "short of the last vertex")
        assertFalse(stepsTile(steps(1..2, 2..3), 4), "past the first vertex")
        assertFalse(stepsTile(steps(-1..1), 2), "an index before the geometry")
    }

    @Test
    fun anEmptyListTilesNothing() {
        assertFalse(stepsTile(emptyList(), 3))
        assertFalse(stepsTile(emptyList(), 0))
    }

    /** A refused list reads as no steps, never as a repair of them; an empty one is no refusal. */
    @Test
    fun aReadingIsTheListWholeOrRefused() {
        val tiling = steps(0..1, 1..2)

        assertSame(tiling, stepsReading(tiling, 3).steps)
        assertFalse(stepsReading(tiling, 3).refused)
        assertEquals(refusedSteps, stepsReading(steps(0..1), 3))
        assertTrue(stepsReading(steps(0..1), 3).steps.isEmpty())
        assertFalse(stepsReading(emptyList(), 3).refused)
    }
}
