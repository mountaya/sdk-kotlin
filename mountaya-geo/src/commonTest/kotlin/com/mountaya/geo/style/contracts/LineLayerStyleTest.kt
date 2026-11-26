package com.mountaya.geo

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LineLayerStyleTest {
    private val width =
        JsonArray(
            listOf(
                JsonPrimitive("interpolate"),
                JsonArray(listOf(JsonPrimitive("linear"))),
                JsonArray(listOf(JsonPrimitive("zoom"))),
                JsonPrimitive(7.0),
                JsonPrimitive(2.0),
            ),
        )
    private val style =
        LineLayerStyle(
            mapOf("line-cap" to JsonPrimitive("round"), "line-join" to JsonPrimitive("round")),
            mapOf("line-color" to JsonPrimitive("#123456"), "line-width" to width),
        )

    @Test
    fun twoStylesAreEqualWhereTheirLayoutAndPaintHoldTheSameProperties() {
        val copy =
            LineLayerStyle(
                mapOf("line-join" to JsonPrimitive("round"), "line-cap" to JsonPrimitive("round")),
                mapOf("line-width" to width, "line-color" to JsonPrimitive("#123456")),
            )
        assertEquals(style, copy)
        assertEquals(style.hashCode(), copy.hashCode())
        assertNotEquals(LineLayerStyle(emptyMap(), style.paint), style)
        assertNotEquals(LineLayerStyle(style.layout, mapOf("line-color" to JsonPrimitive("#123456"))), style)
    }

    @Test
    fun twoStylesCompareTheirNumbersAsTheSameNumbersAndTheirTextsByCodePoint() {
        val unknown = LineLayerStyle(emptyMap(), mapOf("line-opacity" to JsonPrimitive(Double.NaN)))
        assertEquals(LineLayerStyle(emptyMap(), mapOf("line-opacity" to JsonPrimitive(Double.NaN))), unknown)
        assertEquals(
            LineLayerStyle(emptyMap(), mapOf("line-opacity" to JsonPrimitive(Double.NaN))).hashCode(),
            unknown.hashCode(),
        )
        assertNotEquals(
            LineLayerStyle(emptyMap(), mapOf("line-opacity" to JsonPrimitive(-0.0))),
            LineLayerStyle(emptyMap(), mapOf("line-opacity" to JsonPrimitive(0.0))),
        )
        assertNotEquals(
            LineLayerStyle(emptyMap(), mapOf("line-color" to JsonPrimitive("café"))),
            LineLayerStyle(emptyMap(), mapOf("line-color" to JsonPrimitive("café"))),
        )
    }
}
