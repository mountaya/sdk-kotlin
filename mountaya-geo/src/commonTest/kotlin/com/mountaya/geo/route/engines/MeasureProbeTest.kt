package com.mountaya.geo

import com.mountaya.client.InternalMountayaApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(InternalMountayaApi::class)
class MeasureProbeTest {
    private val line = listOf(listOf(0.0, 0.0), listOf(0.01, 0.0))

    @Test
    fun aLineMeasuredWhileATestHoldsTheProbeIsRecordedAndNothingIsOnceItLetsGo() {
        val measured = MeasureProbe.measuring { routeLengthMeters(line) }
        routeLengthMeters(line)

        assertEquals(1, measured.size)
        assertSame(line, measured.single(), "the line measured, by identity")
        assertTrue(MeasureProbe.measuring {}.isEmpty(), "a measurement before the probe was held is not recorded")
    }

    @Test
    fun theProbeLetsGoHoweverTheWorkEnds() {
        assertFailsWith<IllegalStateException> {
            MeasureProbe.measuring {
                cumulativeMeters(line)
                error("the work failed")
            }
        }
        val after = MeasureProbe.measuring {}
        cumulativeMeters(line)

        assertTrue(after.isEmpty())
    }
}
