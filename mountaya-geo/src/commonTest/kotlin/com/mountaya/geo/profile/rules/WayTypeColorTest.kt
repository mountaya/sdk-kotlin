package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class WayTypeColorTest {
    @Test
    fun eachWayTypePaintsInItsColourAnUnclassifiedOrUnknownOneInTheSharedGrey() {
        assertEquals(11, wayTypeColors.size)
        assertEquals(HexColor("#3b82f6"), wayTypeColor(RestSchema.WayType("cycleway")))
        assertEquals(HexColor("#22c55e"), wayTypeColor(RestSchema.WayType("path")))
        assertEquals(unclassifiedColor, wayTypeColor(RestSchema.WayType.Unknown))
        assertEquals(unclassifiedColor, wayTypeColor(RestSchema.WayType("bridleway")))
    }
}
