package com.mountaya.elements

import com.mountaya.client.GeoJsonPoint
import com.mountaya.geo.ReadoutPoint
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutPoiOfTest {
    private fun feature(vararg properties: Pair<String, JsonPrimitive>) =
        QueriedFeature(properties.toMap(), GeoJsonPoint(listOf(6.86, 45.83)), "layer")

    /** A point of interest's category is its `subclass`, else its `class`, and its name its `name`. */
    @Test
    fun aPointOfInterestIsNamedByItsNameAndCategorizedByItsSubclassElseItsClass() {
        val hut =
            feature(
                "name" to JsonPrimitive("Refuge du Goûter"),
                "subclass" to JsonPrimitive("alpine_hut"),
                "class" to JsonPrimitive("lodging"),
            )
        val lodging = feature("name" to JsonPrimitive("Gîte"), "class" to JsonPrimitive("lodging"))
        val emptySubclass = feature("subclass" to JsonPrimitive(""), "class" to JsonPrimitive("lodging"))

        assertEquals(ReadoutPoint.Poi("Refuge du Goûter", "alpine_hut"), readoutPoiOf(hut))
        assertEquals(ReadoutPoint.Poi("Gîte", "lodging"), readoutPoiOf(lodging))
        assertEquals(ReadoutPoint.Poi("", "lodging"), readoutPoiOf(emptySubclass))
        assertEquals(ReadoutPoint.Poi("", ""), readoutPoiOf(feature("name" to JsonPrimitive(3))))
    }
}
