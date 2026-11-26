package com.mountaya.elements

import com.mountaya.client.generated.DataSchema
import com.mountaya.geo.PixelPadding
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HeldAnalysisTest {
    @Test
    fun aHeldResultKeepsItsInputItsAnswerAndItsOptionsReadBackAsTheirTypes() {
        val input =
            DataSchema.IsochronesInput(
                DataSchema.ActivitySlug.HikingAndTrail,
                listOf(listOf(6.86, 45.83)),
                listOf(600.0),
                DataSchema.RangeType.Time,
            )
        val held =
            HeldAnalysis(
                AnalysisKind.Isochrones,
                input,
                DataSchema.IsochronesResult { isochrones = emptyList() },
                chosen = 0,
                fit = CameraFit(padding = PixelPadding(12.0)),
            )
        assertEquals(AnalysisKind.Isochrones, held.kind)
        assertNull(held.result as? DataSchema.SuggestionsResult, "a result is of its own kind")
        assertEquals(listOf(600.0), assertIs<DataSchema.IsochronesInput>(held.input).range)
        assertTrue(assertIs<DataSchema.IsochronesResult>(held.result).isochrones?.isEmpty() == true)
        assertEquals(CameraFit(padding = PixelPadding(12.0)), held.fit)
    }

    @Test
    fun aResultHandedOverHoldsNoInput() {
        val held = HeldAnalysis(AnalysisKind.Places, null, DataSchema.GeocodeResult { places = emptyList() }, 0, null)
        assertNull(held.input)
        assertNull(held.fit)
    }
}
