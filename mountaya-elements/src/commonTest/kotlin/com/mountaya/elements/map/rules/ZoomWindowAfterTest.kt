package com.mountaya.elements

import com.mountaya.geo.ZoomBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ZoomWindowAfterTest {
    private val glJs = ZoomBounds(-2.0, 22.0)
    private val native = ZoomBounds(0.0, 25.5)
    private val held = ZoomBounds(8.0, 12.0)

    private fun zoom(value: Double): ZoomLimit = ZoomLimit.Zoom(value)

    /** The kind the call is refused as, or null where the window is taken. */
    private fun kind(
        min: ZoomLimit,
        max: ZoomLimit,
        renderer: ZoomBounds = glJs,
    ): MapException.Kind? =
        try {
            zoomWindowAfter(held, min, max, renderer)
            null
        } catch (refusal: MapException) {
            refusal.kind
        }

    @Test
    fun eachLimitStatedMovesEachLeftOutIsKeptAndAWindowAboveTheOneHeldIsTaken() {
        assertEquals(held, zoomWindowAfter(held, zoom(8.0), zoom(12.0), glJs))
        assertEquals(ZoomBounds(8.0, 14.0), zoomWindowAfter(held, ZoomLimit.Kept, zoom(14.0), glJs))
        assertEquals(held, zoomWindowAfter(held, ZoomLimit.Kept, ZoomLimit.Kept, glJs))
        assertEquals(ZoomBounds(10.0, 14.0), zoomWindowAfter(ZoomBounds(2.0, 6.0), zoom(10.0), zoom(14.0), glJs))
        assertEquals(
            ZoomBounds(12.0, 12.0),
            zoomWindowAfter(held, zoom(12.0), ZoomLimit.Kept, glJs),
            "a window of one zoom",
        )
    }

    @Test
    fun aReleasedLimitGoesBackToTheRenderersOwnAndIsReadAsThatZoom() {
        assertEquals(ZoomBounds(14.0, 22.0), zoomWindowAfter(held, zoom(14.0), ZoomLimit.Released, glJs))
        assertEquals(ZoomBounds(-2.0, 12.0), zoomWindowAfter(held, ZoomLimit.Released, ZoomLimit.Kept, glJs))
        assertEquals(native, zoomWindowAfter(held, ZoomLimit.Released, ZoomLimit.Released, native))
        assertEquals(MapException.Kind.ZoomBoundsRefused, kind(ZoomLimit.Released, zoom(1.0), ZoomBounds(3.0, 22.0)))
    }

    @Test
    fun aLimitNoFiniteNumberIsRefusedAsANumberBeforeAnyWindowIsRead() {
        assertEquals(MapException.Kind.NumberRefused, kind(zoom(Double.NaN), ZoomLimit.Kept))
        assertEquals(MapException.Kind.NumberRefused, kind(ZoomLimit.Kept, zoom(Double.POSITIVE_INFINITY)))
        assertEquals(MapException.Kind.NumberRefused, kind(zoom(Double.NEGATIVE_INFINITY), zoom(12.0)))
        assertEquals(
            MapException.Kind.NumberRefused,
            kind(zoom(Double.NaN), zoom(1.0)),
            "a number refused before the window",
        )
    }

    @Test
    fun aFloorUnderTheRenderersAboveTheCeilingStatedOrHeldOrACeilingUnderTheFloorHeldIsRefused() {
        assertEquals(MapException.Kind.ZoomBoundsRefused, kind(zoom(-3.0), ZoomLimit.Kept))
        assertNull(kind(zoom(-2.0), ZoomLimit.Kept))
        assertEquals(MapException.Kind.ZoomBoundsRefused, kind(zoom(-1.0), ZoomLimit.Kept, native))
        assertNull(kind(zoom(0.0), ZoomLimit.Kept, native))
        assertEquals(MapException.Kind.ZoomBoundsRefused, kind(zoom(13.0), ZoomLimit.Kept))
        assertEquals(MapException.Kind.ZoomBoundsRefused, kind(zoom(10.0), zoom(9.0)))
        assertEquals(MapException.Kind.ZoomBoundsRefused, kind(ZoomLimit.Kept, zoom(7.0)))
        assertNull(kind(ZoomLimit.Kept, zoom(8.0)))
    }
}
