package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PaintZoomRangeTest {
    @Test
    fun theWindowRunsFromTheLowestLayerToTheHighestOfEverySource() {
        val sources =
            listOf(
                TileFixture.source(
                    listOf(
                        TileFixture.layer("a", zoomMin = 10, zoomMax = 14),
                        TileFixture.layer("b", zoomMin = 12, zoomMax = 22),
                    ),
                ),
                TileFixture.source(listOf(TileFixture.layer("c", zoomMin = 6, zoomMax = 16))),
            )
        assertEquals(ZoomBounds(6.0, 22.0), paintZoomRange(sources))
    }

    /** A source's own range says where its tiles are served and bounds nothing. */
    @Test
    fun aSourcesOwnRangeBoundsNothing() {
        val served =
            TileFixture.source(
                listOf(TileFixture.layer("a", zoomMin = 8, zoomMax = 22)),
                zoomMin = 10,
                zoomMax = 15,
            )
        assertEquals(ZoomBounds(8.0, 22.0), paintZoomRange(listOf(served, TileFixture.source(emptyList()))))
    }

    @Test
    fun noLayerPaintsAtNoZoom() {
        assertNull(paintZoomRange(emptyList()))
        assertNull(paintZoomRange(listOf(TileFixture.source(emptyList()))))
    }
}
