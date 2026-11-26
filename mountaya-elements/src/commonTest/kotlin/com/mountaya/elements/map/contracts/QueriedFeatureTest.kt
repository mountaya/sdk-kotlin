package com.mountaya.elements

import com.mountaya.client.GeoJsonPoint
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class QueriedFeatureTest {
    private val point = GeoJsonPoint(listOf(6.9, 45.9))

    @Test
    fun aPropertyHoldingAListOrAnObjectIsLeftOutEveryOtherKeptNullIncluded() {
        val feature =
            QueriedFeature(
                properties =
                    mapOf(
                        "cuisine" to JsonArray(listOf(JsonPrimitive("savoyard"), JsonPrimitive("pizza"))),
                        "hours" to JsonObject(mapOf("open" to JsonPrimitive("08:00"))),
                        "name" to JsonPrimitive("Buvette"),
                        "open" to JsonPrimitive(true),
                        "seats" to JsonPrimitive(24),
                        "website" to JsonNull,
                    ),
                geometry = point,
                layer = "poi-food",
                sourceLayer = "poi_food",
                id = QueriedFeature.Id.Text("poi-12"),
            )

        assertEquals(listOf("name", "open", "seats", "website"), feature.properties.keys.sorted())
        assertEquals(JsonNull, feature.properties["website"])
        assertEquals<QueriedFeature.Id?>(QueriedFeature.Id.Text("poi-12"), feature.id)
        assertEquals("poi-food", feature.layer)
        assertEquals("poi_food", feature.sourceLayer)
    }

    @Test
    fun twoFeaturesAreEqualWhereEveryMemberIsTheSame() {
        val a = QueriedFeature(emptyMap(), point, "line", id = QueriedFeature.Id.Number(7.0))

        assertEquals(a, QueriedFeature(emptyMap(), point, "line", id = QueriedFeature.Id.Number(7.0)))
        assertEquals(
            a.hashCode(),
            QueriedFeature(emptyMap(), point, "line", id = QueriedFeature.Id.Number(7.0)).hashCode(),
        )
        assertNotEquals(a, QueriedFeature(emptyMap(), point, "line", id = QueriedFeature.Id.Text("7")))
        assertNotEquals(a, QueriedFeature(emptyMap(), point, "casing", id = QueriedFeature.Id.Number(7.0)))
        assertNotEquals(
            a,
            QueriedFeature(emptyMap(), point, "line", sourceLayer = "lines", id = QueriedFeature.Id.Number(7.0)),
        )
        assertEquals<QueriedFeature.Id>(QueriedFeature.Id.Number(Double.NaN), QueriedFeature.Id.Number(Double.NaN))
        assertNotEquals<QueriedFeature.Id>(QueriedFeature.Id.Text("café"), QueriedFeature.Id.Text("café"))
    }
}
