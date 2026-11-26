package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class SlopeRampColorTest {
    @Test
    fun anAscentAndADescentReadTwoRampsAndLevelGroundTheFirstAscentStop() {
        assertEquals("#66bb6a", slopeRampColor(0.0))
        assertEquals("#66bb6a", slopeRampColor(-0.0))
        assertEquals("#ffb414", slopeRampColor(5.0))
        assertEquals("#2297eb", slopeRampColor(-5.0))
        assertEquals("#65b8b0", slopeRampColor(-1.0))
    }

    @Test
    fun atOrPastTheLastStopTheLastColourHoldsAndNoNumberIsGrey() {
        assertEquals("#b71c1c", slopeRampColor(22.0))
        assertEquals("#b71c1c", slopeRampColor(Double.POSITIVE_INFINITY))
        assertEquals("#0d47a1", slopeRampColor(-45.0))
        assertEquals("#94a3b8", slopeRampColor(Double.NaN))
    }

    /** Halfway from #66BB6A to #FFEB3B: red 0x66 + 0x99 / 2 is 178.5, which rounds up. */
    @Test
    fun aChannelHalfwayBetweenTwoWholeNumbersRoundsUp() {
        assertEquals("#b3d353", slopeRampColor(2.5))
    }

    @Test
    fun theRampsAreTheContracts() {
        assertEquals(listOf(0.0, 2.0, 3.0, 6.0, 11.0, 22.0), ascentSlopeRamp.map { it.degrees })
        assertEquals(listOf(0.0, 2.0, 3.0, 6.0, 11.0, 22.0), descentSlopeRamp.map { it.degrees })
        assertEquals(ascentSlopeRamp.first(), descentSlopeRamp.first())
    }
}
