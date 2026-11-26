package com.mountaya.geo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class LayersSelectionTargetTest {
    @Test
    fun aMapOfAnAppsOwnIsATargetASelectionDrivesComparedByIdentity() =
        runTest {
            val map = HeldLayersMap(TileFixture.layers(emptyList()), null, answersAtOnce = true)
            val target: LayersSelectionTarget = map
            val engine = LayersSelectionEngine(backgroundScope, target)

            engine.setOverlay("slopes")
            engine.setOverlay(null)
            engine.setInstant(Instant.parse("2026-03-15T12:00:00Z"))
            runCurrent()

            assertEquals(listOf("overlay:slopes", "overlay:-", "instant:2026-03-15T12:00:00Z"), map.calls)
            assertSame<Any>(map, target)
            map.release()
            engine.close()
        }
}
