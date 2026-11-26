package com.mountaya.geo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GeolocateControlTargetTest {
    /** A map of an app's own, which locates at once and reads location allowed. */
    private class Here : GeolocateControlTarget {
        var located = 0
            private set

        override suspend fun locate(): List<Double> {
            located += 1
            return listOf(6.86, 45.83)
        }

        override suspend fun isLocationRefused(): Boolean = false
    }

    @Test
    fun aMapOfAnAppsOwnIsATargetAControlStandsOver() =
        runTest {
            val map = Here()
            val engine = GeolocateControlEngine(backgroundScope, map)

            engine.locate()
            runCurrent()

            assertEquals(1, map.located)
            assertEquals(GeolocateControlState(isBusy = false, isBlocked = false, isLive = true), engine.state.value)
            engine.close()
        }
}
