package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class SuggestionsGeoJsonTest {
    private val two =
        DataSchema.SuggestionsResult {
            routes =
                listOf(DataSchema.SuggestionRoute { seed = 1 }, DataSchema.SuggestionRoute { seed = 2 })
        }

    @Test
    fun theFirstIsChosenUnlessAnotherIsNamed() {
        val first = suggestionsGeoJson(two).features
        assertEquals(JsonPrimitive(true), first[0].properties["chosen"])
        assertEquals(JsonPrimitive(SUGGESTION_CHOSEN_COLOR), first[0].properties[FEATURE_COLOR_PROPERTY])
        assertEquals(JsonPrimitive(SUGGESTION_ALTERNATIVE_COLOR), first[1].properties[FEATURE_COLOR_PROPERTY])
        val second = suggestionsGeoJson(two, chosen = 1).features
        assertEquals(JsonPrimitive(false), second[0].properties["chosen"])
        assertEquals(JsonPrimitive(true), second[1].properties["chosen"])
        // A place no suggestion holds chooses none.
        assertEquals(
            listOf(
                JsonPrimitive(false),
                JsonPrimitive(false),
            ),
            suggestionsGeoJson(two, chosen = 5).features.map {
                it.properties["chosen"]
            },
        )
    }
}
