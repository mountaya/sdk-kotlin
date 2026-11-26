package com.mountaya.build.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelReaderTest {
    private val text =
        assertNotNull(javaClass.classLoader.getResource("generator/model.json"), "the test's model is a resource")
            .readText()

    @Test
    fun theModelIsReadInItsOwnOrder() {
        val model = ModelReader.read(text)

        assertEquals("v0.0.0-test", model.contract)
        assertEquals(
            listOf(
                "getHut",
                "listHuts",
                "postSignal",
                "getSignals",
                "patchHut",
                "deleteHut",
                "getExport",
                "listKeys",
                "postKey",
            ),
            model.rest.operations.map { it.operationId },
        )
        assertEquals(listOf("getTile", "getTileJson"), model.tiles.templates.map { it.operationId })
        assertEquals("/graphql", model.data.endpoint)
        val hut = assertIs<ObjectSchema>(model.rest.schemas.first())
        assertEquals("id", hut.properties.first().name)
        assertTrue(hut.properties.single { it.name == "in" }.deprecated)
    }

    /**
     * The model states the unit of every REST operation, webhook event and component, and a Tile API entry that
     * states none is the core's, whose the Tile API is.
     */
    @Test
    fun everyRestEntrysUnitIsReadAndATileEntryThatStatesNoneIsTheCores() {
        val model = ModelReader.read(text)

        assertEquals(
            mapOf("listKeys" to ClientUnit.Admin, "postKey" to ClientUnit.Admin, "getHut" to ClientUnit.Core),
            model.rest.operations
                .filter { it.operationId in setOf("listKeys", "postKey", "getHut") }
                .associate { it.operationId to it.unit },
        )
        assertEquals(
            ClientUnit.Admin,
            model.rest.schemas
                .single { it.name == "HutEventObject" }
                .unit,
        )
        assertEquals(
            ClientUnit.Core,
            model.rest.schemas
                .single { it.name == "Season" }
                .unit,
        )
        assertEquals(listOf(ClientUnit.Admin, ClientUnit.Admin), model.rest.webhooks.map { it.unit })
        assertEquals(listOf(ClientUnit.Core, ClientUnit.Core), model.tiles.schemas.map { it.unit })
        assertEquals(listOf(ClientUnit.Core, ClientUnit.Core), model.tiles.templates.map { it.unit })
    }

    @Test
    fun aPathParameterIsRequiredWhereTheModelLeavesItUnsaid() {
        val model = ModelReader.read(text)

        assertTrue(
            model.rest.operations
                .first()
                .pathParameters
                .all { it.required },
        )
    }

    @Test
    fun aDataApiScalarKeepsItsWireType() {
        val scalars =
            ModelReader
                .read(text)
                .data.scalars
                .associateBy { it.name }

        assertEquals("Geometry", scalars.getValue("GeoJSON").geometry)
        assertEquals("date-time", (scalars.getValue("DateTime").wire as ScalarType).format)
        assertTrue(scalars.getValue("ID").builtin)
    }

    @Test
    fun anOperationCarriesEveryFactTheFormatGivesIt() {
        val model = ModelReader.read(text)
        val delete = model.rest.operations.single { it.operationId == "deleteHut" }
        val list = model.rest.operations.single { it.operationId == "listHuts" }

        assertEquals("required", delete.credentials.actingUser)
        assertEquals(30000, delete.timeoutMs)
        assertTrue(delete.idempotencyKey && delete.conditional)
        assertIs<NullType>(delete.success.data?.type)
        assertEquals(listOf(404, 409), delete.errors.map { it.status })
        assertIs<InlineObject>(
            delete.errors
                .last()
                .metadata
                ?.type,
        )
        assertEquals(listOf("name", "-name"), list.pagination?.sort)
        assertEquals("name", list.pagination?.defaultSort)
        assertNull(
            model.rest.operations
                .single { it.operationId == "getSignals" }
                .timeoutMs,
        )
        assertEquals(
            listOf("DATA"),
            model.data.operations
                .single()
                .scopes,
        )
        assertEquals(
            30000,
            model.data.operations
                .single()
                .timeoutMs,
        )
    }

    @Test
    fun theTasksOfAWriteAreReadAndARead() {
        val model = ModelReader.read(text)
        val tasks = model.rest.operations.associate { it.operationId to it.tasks }

        assertEquals(listOf("signal.post", "signal.record"), tasks.getValue("postSignal"))
        assertEquals(listOf("hut.delete"), tasks.getValue("deleteHut"))
        assertEquals(emptyList(), tasks.getValue("getHut"))
    }

    @Test
    fun theWebhooksAreReadInTheirOwnOrderAndAbsentOnTheTileApi() {
        val model = ModelReader.read(text)
        val webhooks = model.rest.webhooks

        assertEquals(listOf("hut.created", "hut.updated"), webhooks.map { it.type })
        assertEquals(listOf("WebhookEvent"), webhooks.map { it.envelope }.distinct())
        assertEquals("HutEventObject", webhooks.first().objectName)
        assertEquals(emptyList(), webhooks.first().previousAttributes)
        assertEquals(listOf("count", "updated_at", "season", "old"), webhooks.last().previousAttributes)
        assertEquals("A hut was created.", webhooks.first().summary)
        assertEquals(emptyList(), model.tiles.webhooks)
    }

    @Test
    fun aMissingKeyIsNamedWhereItIsMissing() {
        val refused =
            assertFailsWith<ModelException> { ModelReader.read(text.replace("\"contract\": \"v0.0.0-test\",", "")) }

        assertEquals("sdk/model.json: `contract` is missing", refused.message)
    }
}
