package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EnabledTargetsTest {
    @Test
    fun aRequestEnablesItsBranchAloneEveryOtherFlagStatedFalse() {
        val known =
            listOf(
                DataSchema.SearchTarget.Places,
                DataSchema.SearchTarget.Pois,
                DataSchema.SearchTarget.Ways,
                DataSchema.SearchTarget.Itineraries,
                DataSchema.SearchTarget.Collections,
                DataSchema.SearchTarget.Zones,
            )
        for ((index, branch) in known.withIndex()) {
            val targets = assertNotNull(enabledTargets(branch), "the branch enables its targets")
            val flags = with(targets) { listOf(places, pois, ways, itineraries, collections, zones) }
            assertEquals((0 until 6).map { it == index }, flags)
        }
    }

    @Test
    fun aBranchThisBuildCannotEnableEnablesNothingItsNameReadByItsCodePoints() {
        assertNull(enabledTargets(DataSchema.SearchTarget("TRAILS")))
        assertNull(enabledTargets(DataSchema.SearchTarget("places")))
        assertNull(enabledTargets(DataSchema.SearchTarget("")))
    }
}
