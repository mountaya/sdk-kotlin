package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class LongitudeDeltaTest {
    @Test
    fun theWayAcrossTheAntimeridianIsTheShortOne() {
        assertEquals(0.19999999999998863, longitudeDelta(179.9, -179.9))
        assertEquals(-0.20000000000004547, longitudeDelta(-179.9, 179.9))
    }

    @Test
    fun aDifferenceAlreadyTheShortWayIsThePlainDifference() {
        assertEquals(7.2 - 6.8652, longitudeDelta(6.8652, 7.2))
    }

    /** Exactly half a turn keeps its sign as the plain difference has it, either way. */
    @Test
    fun halfATurnIsKeptAsThePlainDifference() {
        assertEquals(180.0, longitudeDelta(0.0, 180.0))
        assertEquals(-180.0, longitudeDelta(0.0, -180.0))
        assertEquals(180.0, longitudeDelta(-90.0, 90.0))
    }

    @Test
    fun theLongWayRoundIsTurned() {
        assertEquals(-90.0, longitudeDelta(10.0, 280.0))
        assertEquals(0.0, longitudeDelta(190.0, -170.0))
    }

    @Test
    fun aLongitudeThatIsNotFiniteAnswersNaN() {
        assertEquals(Double.NaN, longitudeDelta(Double.NaN, 10.0))
        assertEquals(Double.NaN, longitudeDelta(0.0, Double.POSITIVE_INFINITY))
    }
}
