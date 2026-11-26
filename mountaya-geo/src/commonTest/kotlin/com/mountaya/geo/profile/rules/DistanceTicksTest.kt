package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DistanceTicksTest {
    private val metric = RestSchema.UnitLength.Metric
    private val imperial = RestSchema.UnitLength.Imperial

    @Test
    fun theStepIsTheSmallestOneTwoOrFiveAtLeastTheRoughStep() {
        assertEquals(listOf(0.0, 10.0, 20.0, 30.0, 40.0), distanceTicks(0.0, 42_195.0, metric).map { it.value })
        assertEquals(listOf(0.0, 50.0, 100.0), distanceTicks(0.0, 100_000.0, metric, target = 3).map { it.value })
        assertEquals(listOf(0.0, 5.0, 10.0, 15.0, 20.0, 25.0), distanceTicks(0.0, 42_195.0, imperial).map { it.value })
    }

    @Test
    fun aMetricStopLandsOnWholeMetersAndCarriesTheDigitsItsStepNeeds() {
        assertEquals(
            listOf(
                DistanceTick(1.2, 1_200.0, 1),
                DistanceTick(1.4, 1_400.0, 1),
                DistanceTick(1.6, 1_600.0, 1),
                DistanceTick(1.8, 1_800.0, 1),
            ),
            distanceTicks(1_200.0, 1_900.0, metric),
        )
        assertEquals(8_046.72, distanceTicks(0.0, 42_195.0, imperial)[1].meters, 1e-9)
    }

    @Test
    fun aRangeGivenBackwardsIsReadInOrder() {
        assertEquals(distanceTicks(3_000.0, 9_000.0, metric), distanceTicks(9_000.0, 3_000.0, metric))
        assertEquals(7, distanceTicks(9_000.0, 3_000.0, metric).size)
    }

    @Test
    fun aRangeOfNoWidthOrNoNumberOrNoStopAskedHasNoStops() {
        assertEquals(emptyList(), distanceTicks(500.0, 500.0, metric))
        assertEquals(emptyList(), distanceTicks(0.0, 1_000.0, metric, target = 0))
        assertEquals(emptyList(), distanceTicks(0.0, Double.NaN, metric))
        assertEquals(emptyList(), distanceTicks(0.0, Double.POSITIVE_INFINITY, metric))
        assertEquals(6, DEFAULT_DISTANCE_TICK_COUNT)
    }

    /** A normalized step of exactly two or five keeps it; one above five is ten, a decade up. */
    @Test
    fun aStepOnTheLatticeIsKeptAndOneAboveFiveIsTheNextDecade() {
        assertEquals(2.0, distanceTicks(0.0, 12_000.0, metric).let { it[1].value - it[0].value })
        assertEquals(5.0, distanceTicks(0.0, 30_000.0, metric).let { it[1].value - it[0].value })
        assertEquals(listOf(0.0, 10.0), distanceTicks(0.0, 10_000.0, metric, target = 1).map { it.value })
    }

    /** Past 2^53 a double counts no further multiple of a step; the axis ends rather than loop. */
    @Test
    fun aRangeSoFarOutADoubleCountsNoFurtherMultipleEndsItsStops() {
        val far = 2.0.pow(63)
        assertTrue(distanceTicks(far, far + 4_096.0, metric).size <= 7)
    }
}
