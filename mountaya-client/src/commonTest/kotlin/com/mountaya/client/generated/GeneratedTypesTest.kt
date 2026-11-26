package com.mountaya.client.generated

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.wireJson
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.time.Instant

/** How the printed types read and write the wire: open enums, open unions, timestamps, wide integers and GeoJSON. */
class GeneratedTypesTest {
    @Test
    fun anEnumValueThisBuildNeverSawReadsAsItselfAndWritesBack() {
        val unseen = wireJson.decodeFromString(RestSchema.ExportFormat.serializer(), "\"gpx-2\"")

        assertEquals("gpx-2", unseen.value)
        assertNotEquals(RestSchema.ExportFormat.Gpx, unseen)
        assertEquals("\"gpx-2\"", wireJson.encodeToString(RestSchema.ExportFormat.serializer(), unseen))
        assertEquals("gpx", RestSchema.ExportFormat.Gpx.toString())
    }

    @Test
    fun aUnionMemberThisBuildNeverSawReadsAsUnknown() {
        val unseen = wireJson.decodeFromString(RestSchema.AgentRunEvent.serializer(), """{"type":"teleported","to":"Zermatt"}""")

        val unknown = assertIs<RestSchema.AgentRunEvent.Unknown>(unseen)
        assertEquals(JsonPrimitive("Zermatt"), unknown.json["to"])
        assertEquals("""{"type":"teleported","to":"Zermatt"}""", wireJson.encodeToString(RestSchema.AgentRunEvent.serializer(), unseen))
    }

    @Test
    fun aKnownUnionMemberReadsAsItsClassAndWritesItsDiscriminator() {
        val done = wireJson.decodeFromString(RestSchema.AgentRunEvent.serializer(), """{"type":"done","reason":"looping"}""")

        assertEquals(RestSchema.AgentRunReason.Looping, assertIs<RestSchema.AgentDoneEvent>(done).reason)
        assertEquals("""{"type":"done","reason":"looping"}""", wireJson.encodeToString(RestSchema.AgentRunEvent.serializer(), done))
    }

    @Test
    fun aRequestUnionMemberIsBuiltWithItsDiscriminatorFixed() {
        val input: RestSchema.AgentRunInputParams = RestSchema.AgentUserMessageInput(content = "Plan a hut-to-hut trek")

        assertEquals(
            """{"type":"user-message","content":"Plan a hut-to-hut trek"}""",
            wireJson.encodeToString(RestSchema.AgentRunInputParams.serializer(), input),
        )
    }

    @Test
    fun aTimestampIsReadThroughTheAnchoredRuleAndRefusedWhenItFailsIt() {
        val session = wireJson.decodeFromString(RestSchema.Session.serializer(), """{"expires_at":"2026-03-14T09:26:53Z","user_id":null,"token":"sess_x"}""")

        assertEquals(Instant.fromEpochSeconds(1_773_480_413), session.expires_at)
        assertFailsWith<SerializationException> {
            wireJson.decodeFromString(RestSchema.Session.serializer(), """{"expires_at":"2026-03-14","user_id":null,"token":"sess_x"}""")
        }
    }

    @Test
    fun aKeyThisBuildDoesNotKnowIsIgnored() {
        val session =
            wireJson.decodeFromString(
                RestSchema.Session.serializer(),
                """{"expires_at":"2026-03-14T09:26:53Z","user_id":null,"token":"sess_x","added_later":{"a":1}}""",
            )

        assertEquals("sess_x", session.token)
    }

    @Test
    fun aRequiredPropertyMissingFailsTheDecode() {
        assertFailsWith<SerializationException> {
            wireJson.decodeFromString(RestSchema.Session.serializer(), """{"token":"sess_x"}""")
        }
    }

    @Test
    fun aGeoJsonComponentIsTheOneHandWrittenGeometry() {
        val geometry: RestSchema.GeoJSONGeometry =
            wireJson.decodeFromString(RestSchema.GeoJSONGeometry.serializer(), """{"type":"LineString","coordinates":[[6.86,45.83],[6.87,45.84]]}""")

        assertEquals(GeoJsonLineString(listOf(listOf(6.86, 45.83), listOf(6.87, 45.84))), geometry)
        assertIs<GeoJsonGeometry>(geometry)
    }

    @Test
    fun aDataApiResultReadsWhateverWasSelectedAndNullForTheRest() {
        val result = wireJson.decodeFromString(DataSchema.DirectionsResult.serializer(), """{"routes":[{"summary":"Via the Col"}]}""")

        val route = result.routes?.single()
        assertEquals("Via the Col", route?.summary)
        assertEquals(null, route?.distance)
        assertEquals(null, route?.geometry)
    }

    @Test
    fun twoValuesBuiltAlikeAreEqualAndPrintTheirProperties() {
        val first = RestSchema.CollectionUpdateParams { name = "Alps" }
        val second = RestSchema.CollectionUpdateParams { name = "Alps" }

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(RestSchema.CollectionUpdateParams { description = null }, RestSchema.CollectionUpdateParams())
        assertEquals("CollectionUpdateParams(description=null, is_private=null, name=Alps)", first.toString())
    }

    /**
     * Three states reach every object a request sends, not only a merge body: a value object such
     * a body embeds leaves out what was not set, and writes a null it was given.
     */
    @Test
    fun aValueObjectAMergeBodyEmbedsHasThreeStatesTooAndWritesEachApart() {
        assertEquals(
            listOf("{}", """{"email":null}""", """{"email":"hut@example.com"}"""),
            listOf(
                RestSchema.WaypointContact(),
                RestSchema.WaypointContact { email = null },
                RestSchema.WaypointContact { email = "hut@example.com" },
            ).map { wireJson.encodeToString(RestSchema.WaypointContact.serializer(), it) },
        )
    }

    @Test
    fun aDataApiInputIsWrittenWithOnlyWhatWasSet() {
        val input = DataSchema.DirectionsInput(DataSchema.ActivitySlug.Running, listOf(listOf(6.86, 45.83))) { speed = 4.5 }

        assertEquals(
            Json.parseToJsonElement("""{"activity":"RUNNING","waypoints":[[6.86,45.83]],"speed":4.5}"""),
            wireJson.encodeToJsonElement(DataSchema.DirectionsInput.serializer(), input),
        )
        assertIs<JsonObject>(wireJson.encodeToJsonElement(DataSchema.DirectionsInput.serializer(), input))
    }

    @Test
    fun aRequiredPropertyThatMayBeNullReadsNullWhereTheAnswerLeavesItOutAndIsAlwaysWritten() {
        val session = wireJson.decodeFromString(RestSchema.Session.serializer(), """{"expires_at":"2026-03-14T09:26:53Z","token":"sess_x"}""")

        assertNull(session.user_id)
        assertEquals(
            """{"expires_at":"2026-03-14T09:26:53.000Z","user_id":null,"token":"sess_x"}""",
            wireJson.encodeToString(RestSchema.Session.serializer(), session),
        )
    }

    @Test
    fun aMergeValueReadFromAnAnswerKeepsItsThreeStatesWhenItIsWrittenAgain() {
        val forms = listOf("{}", """{"email":null}""", """{"email":"hut@example.com"}""")
        val written =
            forms.map {
                wireJson.encodeToString(
                    RestSchema.WaypointContact.serializer(),
                    wireJson.decodeFromString(RestSchema.WaypointContact.serializer(), it),
                )
            }

        assertEquals(forms, written)
    }
}
