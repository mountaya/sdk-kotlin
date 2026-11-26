package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class AmenityNameTest {
    private val water = RestSchema.WaypointAmenity("water")

    @Test
    fun aServiceIsNamedByTheReadersWordElseTheEnglishOneElseItsOwnValue() {
        assertEquals("Water", amenityName(water, ElementsScope.standard))
        assertEquals("Hot meal", amenityName(RestSchema.WaypointAmenity("hot-meal"), ElementsScope.standard))
        assertEquals("sauna", amenityName(RestSchema.WaypointAmenity("sauna"), ElementsScope.standard))
        val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Fr))
        assertEquals("Eau", amenityName(water, french))
        val words = JsonObject(mapOf("amenities" to JsonObject(mapOf("water" to JsonPrimitive("Drinking water")))))
        val overridden = scopeOf(ElementsScope.standard, ElementsScopeStatement(words = WordOverrides.BySection(words)))

        assertEquals("Drinking water", amenityName(water, overridden))
        assertEquals("Food", amenityName(RestSchema.WaypointAmenity("food"), overridden))
    }
}
