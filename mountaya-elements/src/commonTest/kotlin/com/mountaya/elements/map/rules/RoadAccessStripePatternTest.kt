package com.mountaya.elements

import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.ShapePaint
import com.mountaya.geo.roadAccessColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoadAccessStripePatternTest {
    @Test
    fun aStretchIsStriped6PixelsWideOnWhiteInA16PixelTileInItsKindsColour() {
        assertEquals(
            StripePattern(
                16.0,
                6.0,
                "#9c36b5",
                "#ffffff",
                ShapePaint.LineCap.Round,
                listOf(
                    listOf(listOf(-8.0, 8.0), listOf(8.0, -8.0)),
                    listOf(listOf(0.0, 16.0), listOf(16.0, 0.0)),
                    listOf(listOf(8.0, 24.0), listOf(24.0, 8.0)),
                ),
            ),
            roadAccessStripePattern(RoadAccessKind.Permit),
        )
        for (kind in RoadAccessKind.entries) {
            assertEquals(roadAccessColor(kind).hex, roadAccessStripePattern(kind).color, kind.id)
        }
    }

    @Test
    fun eachStripeRisesAt45DegreesAndTheThreeTileSeamlessly() {
        val pattern = roadAccessStripePattern(RoadAccessKind.Forbidden)

        for ((from, to) in pattern.segments.map { it[0] to it[1] }) {
            // From its lower left end to its upper right one, at 45 degrees.
            assertEquals(from[1] - to[1], to[0] - from[0])
            assertTrue(to[0] > from[0])
        }
        // The halves past the tile's edges are the middle stripe moved by half a tile either way, so a stripe
        // leaving one tile goes on in the next.
        val middle = pattern.segments[1]
        assertEquals(middle.map { listOf(it[0] - 8, it[1] - 8) }, pattern.segments[0])
        assertEquals(middle.map { listOf(it[0] + 8, it[1] + 8) }, pattern.segments[2])
    }
}
