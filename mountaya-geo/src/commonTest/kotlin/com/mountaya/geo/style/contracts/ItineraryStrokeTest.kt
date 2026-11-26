package com.mountaya.geo

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ItineraryStrokeTest {
    private fun text(value: String) = JsonPrimitive(value)

    @Test
    fun twoStrokesAreEqualWhereTheyAreTheSameStrokeInTheSameColourAndHashAlike() {
        assertEquals<ItineraryStroke>(ItineraryStroke.Line(text("#123456")), ItineraryStroke.Line(text("#123456")))
        assertEquals(
            ItineraryStroke.Line(text("#123456")).hashCode(),
            ItineraryStroke.Line(text("#123456")).hashCode(),
        )
        assertNotEquals<ItineraryStroke>(ItineraryStroke.Line(text("#123456")), ItineraryStroke.Dashed(text("#123456")))
        assertNotEquals<ItineraryStroke>(ItineraryStroke.Line(text("#123456")), ItineraryStroke.Line(text("#654321")))
        assertEquals<ItineraryStroke>(ItineraryStroke.Casing, ItineraryStroke.Casing)
        assertNotEquals<ItineraryStroke>(ItineraryStroke.Dashed(text("#868e96")), ItineraryStroke.Casing)
    }

    @Test
    fun aColourReadFromTheFeatureComparesAsTheExpressionItIs() {
        val read = JsonArray(listOf(text("get"), text("mtyaColor")))
        assertEquals(
            ItineraryStroke.Line(JsonArray(listOf(text("get"), text("mtyaColor")))),
            ItineraryStroke.Line(read),
        )
        assertEquals(
            ItineraryStroke.Line(JsonArray(listOf(text("get"), text("mtyaColor")))).hashCode(),
            ItineraryStroke.Line(read).hashCode(),
        )
        assertNotEquals(ItineraryStroke.Line(JsonArray(listOf(text("get"), text("color")))), ItineraryStroke.Line(read))
    }

    /**
     * A colour compares as the serialization library compares JSON: a number by its text, so NaN is NaN
     * and -0 another than 0, and a text by its code units.
     */
    @Test
    fun aColourComparesItsNumbersAsTheSameNumbersAndItsTextsByCodePoint() {
        assertEquals(
            ItineraryStroke.Dashed(JsonPrimitive(Double.NaN)),
            ItineraryStroke.Dashed(JsonPrimitive(Double.NaN)),
        )
        assertNotEquals(ItineraryStroke.Dashed(JsonPrimitive(-0.0)), ItineraryStroke.Dashed(JsonPrimitive(0.0)))
        assertNotEquals(ItineraryStroke.Line(text("café")), ItineraryStroke.Line(text("café")))
    }
}
