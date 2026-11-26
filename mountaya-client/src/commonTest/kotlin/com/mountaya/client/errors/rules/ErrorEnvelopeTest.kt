package com.mountaya.client

import com.mountaya.client.ErrorEntry.PathComponent
import com.mountaya.client.generated.ApiErrorCode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ErrorEnvelopeTest {
    private fun read(body: String): ErrorEnvelope = ErrorEnvelope.read(body.encodeToByteArray())

    @Test
    fun anEntryIsAnObjectWithAStringMessageItsCodeAndPathReadWhereTheyAre() {
        val read =
            read(
                """
                {"errors": [42, {"message": "no code"}, {"extensions": {"code": "VALIDATION_FAILED"},
                 "message": "Too long.", "path": ["request", "body", "waypoints", 0, "name"]},
                 {"message": 7}, {"message": "odd path", "path": [true]}]}
                """.trimIndent(),
            )

        assertEquals(3, read.errors.size)
        assertEquals(ErrorEntry("no code"), read.errors[0])
        assertEquals(ApiErrorCode("VALIDATION_FAILED"), read.errors[1].code)
        assertEquals(
            listOf(
                PathComponent.Key("request"),
                PathComponent.Key("body"),
                PathComponent.Key("waypoints"),
                PathComponent.Index(0),
                PathComponent.Key("name"),
            ),
            read.errors[1].path,
        )
        assertNull(read.errors[2].path)
        assertTrue(read.hasErrors)
    }

    @Test
    fun errorsThatAreNoListCarryNoEntryAndStillSayTheBodyCarriedErrors() {
        val read = read("""{"errors": "conflict"}""")

        assertTrue(read.errors.isEmpty())
        assertTrue(read.hasErrors)
        assertFalse(read("""{"errors": null}""").hasErrors)
    }

    @Test
    fun theMetadataIsKeptExactlyAsSentAndABodyThatIsNoJsonObjectCarriesNothing() {
        val read = read("""{"errors": [], "extensions": {"metadata": {"holder": {"id": "m1"}}}}""")

        assertEquals(JsonObject(mapOf("holder" to JsonObject(mapOf("id" to JsonPrimitive("m1"))))), read.metadata)
        assertNull(read("""{"extensions": {"metadata": null}}""").metadata)
        assertTrue(read("not json").errors.isEmpty())
        assertFalse(read("[]").hasErrors)
    }
}
