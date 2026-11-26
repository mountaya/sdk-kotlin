package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** What a probe counts of the parts that report to it, read with no composition. */
class RenderProbeTest {
    @Test
    fun eachReportIsCountedByItsPartsNameAndAPartNeverReportedCountsNone() {
        val probe = RenderProbe()

        probe.report("PanelRow parking")
        probe.report("PanelRow parking")
        probe.report("PoiPanel")

        assertEquals(2, probe["PanelRow parking"])
        assertEquals(1, probe["PoiPanel"])
        assertEquals(0, probe["PanelRow poi_food"])
        assertEquals(mapOf("PanelRow parking" to 2, "PoiPanel" to 1), probe.counts)
    }

    @Test
    fun theCountsReadAreTheOnesThatStoodThenAndAChartsPathsAreNoneBeforeOneIsComposed() {
        val probe = RenderProbe()
        probe.report("ProfileCurve")
        val before = probe.counts

        probe.report("ProfileCurve")

        assertEquals(1, before["ProfileCurve"])
        assertEquals(2, probe["ProfileCurve"])
        assertNull(probe.plotPaths)
    }
}
