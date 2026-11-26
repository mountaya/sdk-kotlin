package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class SurfaceColorTest {
    @Test
    fun eachSurfacePaintsInItsColourAnUnclassifiedOrUnknownOneInTheSharedGrey() {
        assertEquals(19, surfaceColors.size)
        assertEquals(HexColor("#334155"), surfaceColor(RestSchema.SurfaceType("asphalt")))
        assertEquals(HexColor("#ca8a04"), surfaceColor(RestSchema.SurfaceType("fine-gravel")))
        assertEquals(unclassifiedColor, surfaceColor(RestSchema.SurfaceType.Unknown))
        assertEquals(unclassifiedColor, surfaceColor(RestSchema.SurfaceType("moon-dust")))
        assertEquals(HexColor("#94a3b8"), unclassifiedColor)
    }
}
