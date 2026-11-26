package com.mountaya.elements

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class RelabelledBaseLabelsTest {
    @Test
    fun aBaseLabelReadingANameIsRelabelledOneReadingNoneIsLeftAPlacedOneSkipped() {
        val labels =
            listOf(
                SymbolLabel("place-label", JsonPrimitive("{name}")),
                SymbolLabel("road-shield", JsonPrimitive("{ref}")),
                SymbolLabel("waypoint-label", JsonPrimitive("{name}")),
                SymbolLabel("peak-label", JsonPrimitive("{name}\n{ele}")),
            )

        val relabelled = relabelledBaseLabels(labels, { it == "waypoint-label" }, RestSchema.Language.De)

        assertEquals(listOf("place-label", "road-shield", "peak-label"), relabelled.map { it.first })
        assertEquals(listOf(true, false, true), relabelled.map { it.second != null })
        assertEquals(
            JsonArray(
                listOf(
                    JsonPrimitive("coalesce"),
                    JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name:de"))),
                    JsonArray(listOf(JsonPrimitive("get"), JsonPrimitive("name"))),
                ),
            ),
            relabelled.first().second,
        )
    }
}
