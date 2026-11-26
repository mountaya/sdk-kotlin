package com.mountaya.geo

import com.mountaya.client.GeoJsonPolygon
import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class IsochronesGeoJsonTest {
    private val square =
        GeoJsonPolygon(listOf(listOf(listOf(0.0, 0.0), listOf(1.0, 0.0), listOf(1.0, 1.0), listOf(0.0, 0.0))))

    @Test
    fun eachBandIsAPolygonColouredByItsPlace() {
        val bands =
            DataSchema.IsochronesResult {
                isochrones =
                    List(7) { index ->
                        DataSchema.Isochrone {
                            value = 600.0 * (index + 1)
                            geometry = square
                        }
                    }
            }
        val features = isochronesGeoJson(bands).features
        assertEquals(7, features.size)
        assertEquals(square, features[0].geometry)
        assertEquals(JsonPrimitive("#c5f6fa"), features[0].properties[FEATURE_COLOR_PROPERTY])
        // Past the last stop the ramp starts again from the first.
        assertEquals(JsonPrimitive("#c5f6fa"), features[6].properties[FEATURE_COLOR_PROPERTY])
        assertEquals(JsonPrimitive(6), features[6].properties[FEATURE_INDEX_PROPERTY])
        assertEquals(JsonNull, features[0].properties["area"])
        assertEquals(
            listOf("area", "center", "reachFactor", "value", "mtyaColor", "mtyaIndex", "mtyaKind"),
            features[0].properties.keys.toList(),
        )
    }

    @Test
    fun anAnswerWithNoBandDrawsNothing() {
        assertEquals(emptyList(), isochronesGeoJson(DataSchema.IsochronesResult()).features)
    }

    @Test
    fun aBandTakesTheRampsColourAtItsPlaceTheRampTakenAgainPastItsLastStop() {
        assertEquals(listOf("#c5f6fa", "#99e9f2", "#66d9e8", "#3bc9db", "#22b8cf", "#15aabf"), isochroneRamp)
        assertEquals(isochroneRamp, (0 until 6).map(::isochroneColor))
        assertEquals("#c5f6fa", isochroneColor(6))
        assertEquals("#66d9e8", isochroneColor(8))
        assertEquals("#99e9f2", isochroneColor(13))
        // 2^31 - 1 is 1 past a multiple of 6.
        assertEquals("#99e9f2", isochroneColor(Int.MAX_VALUE))
    }

    /**
     * An index below zero is no place in an answer, and takes the first stop rather than reading the
     * ramp back from its last.
     */
    @Test
    fun anIndexBelowZeroTakesTheFirstStop() {
        assertEquals("#c5f6fa", isochroneColor(-1))
        assertEquals("#c5f6fa", isochroneColor(-6))
        assertEquals("#c5f6fa", isochroneColor(-7))
        assertEquals("#c5f6fa", isochroneColor(Int.MIN_VALUE))
    }
}
