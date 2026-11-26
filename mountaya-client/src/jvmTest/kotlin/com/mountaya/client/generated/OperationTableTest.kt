package com.mountaya.client.generated

import com.mountaya.client.OperationFacts
import com.mountaya.client.Pagination
import com.mountaya.client.SuccessKind
import com.mountaya.client.SuccessResponse
import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.Surface
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * The facts gate: every printed operation's facts against `sdk/model.json`, fact by fact.
 *
 * Idempotence, the six credential facts, the method, the path, the attempt budget, whether the
 * operation takes an `Idempotency-Key` and an `If-Match`, whether it answers a 304, how it pages,
 * its success statuses and its error statuses decide what the transport does with a call, and a
 * printer that dropped one would ship a client that repeats a write or sends a credential an
 * operation refuses, with nothing else failing. The headers each success status declares decide
 * nothing at request time, and are held here so the printed facts say all the model says. The
 * model is held to the bundles byte for byte by the contract's own gate, so reading the model here
 * reads them.
 *
 * The REST API's operations here are the core's, the ones the model states the `core` unit of: the admin
 * unit's are printed into `mountaya-admin`, whose own facts test holds them to the model the same way.
 */
class OperationTableTest {
    private val model: JsonObject? =
        System
            .getProperty("mountaya.model")
            ?.let(::File)
            ?.takeIf { it.isFile }
            ?.let { Json.parseToJsonElement(it.readText()).jsonObject }

    /** The operations of the surface [name] the model states the core's, a Tile API operation that states none among them. */
    private fun JsonObject.coreOperations(name: String): List<JsonObject> =
        getValue(name).jsonObject.getValue("operations").jsonArray.map { it.jsonObject }.filter {
            (it["unit"]?.jsonPrimitive?.content ?: "core") == "core"
        }

    @Test
    fun everyRestAndTileOperationIsPrintedWithTheFactsTheModelDeclares() {
        val model = model ?: return skipped()
        for ((printed, name) in listOf(OperationTable.restOperations to "rest", OperationTable.tileOperations to "tiles")) {
            val declared = model.coreOperations(name)
            assertEquals(declared.map { it.text("operationId") }, printed.map { it.operationId }, "$name: the operations, in order")
            for ((operation, facts) in declared.zip(printed)) assertFacts(operation, facts)
        }
    }

    @Test
    fun everyDataApiOperationIsTheModelsSentToTheEndpointIdempotentAndBehindASession() {
        val model = model ?: return skipped()
        val data = model.getValue("data").jsonObject
        val declared = data.getValue("operations").jsonArray.map { it.jsonObject }
        val printed = OperationTable.dataOperations
        assertEquals(declared.map { it.text("name") }, printed.map { it.operationId }, "data: the operations, in order")
        for ((operation, facts) in declared.zip(printed)) {
            val id = facts.operationId
            assertEquals("POST", facts.method, id)
            assertEquals(data.text("endpoint"), facts.path, id)
            assertEquals(true, facts.idempotent, id)
            assertEquals(SuccessKind.Graphql, facts.success, id)
            assertEquals(Surface.Data, facts.surface, id)
            // The model states the Data API's credential facts as it states a REST operation's, and every
            // Data API query is a read over its POST, which no fact of the model states.
            assertCredentials(id, operation.getValue("credentials").jsonObject, facts.credentials)
            assertEquals(true, facts.credentials.read, "$id: a Data API query is declared a read")
            assertEquals(operation.getValue("timeoutMs").jsonPrimitive.int, facts.timeoutMs, "$id: timeoutMs")
            assertEquals(operation.getValue("timeoutMs").jsonPrimitive.int.milliseconds, facts.timeout, "$id: timeout")
            assertFalse(facts.declaresIdempotencyKey, id)
            assertFalse(facts.isConditional, id)
            assertFalse(facts.answersNotModified, id)
            assertNull(facts.pagination, id)
            assertEquals(successes(operation.getValue("success").jsonObject), facts.successResponses, "$id: success statuses")
            assertEquals(emptyList(), facts.errorStatuses, id)
            assertEquals(emptyList(), facts.tasks, id)
        }
    }

    @Test
    fun theTableHoldsEveryOperationOnce() {
        val model = model ?: return skipped()
        val count = listOf("rest", "tiles", "data").sumOf { model.coreOperations(it).size }
        val all = OperationTable.restOperations + OperationTable.tileOperations + OperationTable.dataOperations
        assertEquals(count, all.size)
        assertEquals(count, all.map { it.surface to it.operationId }.toSet().size)
        assertEquals(listOf(Surface.Rest), OperationTable.restOperations.map { it.surface }.distinct())
        assertEquals(listOf(Surface.Tiles), OperationTable.tileOperations.map { it.surface }.distinct())
    }

    @Test
    fun noTemplateIsPrintedAsAnOperation() {
        val model = model ?: return skipped()
        val templates =
            listOf("rest", "tiles").flatMap { name ->
                model.getValue(name).jsonObject.getValue("templates").jsonArray.map { it.jsonObject.text("operationId") }
            }
        val printed = (OperationTable.restOperations + OperationTable.tileOperations).map { it.operationId }.toSet()
        for (template in templates) assertEquals(false, template in printed, "$template is marked x-sdk-ignore")
    }

    @Test
    fun everyDataOperationPrintsTheFullDocumentTheModelCarries() {
        val model = model ?: return skipped()
        val operations = model.getValue("data").jsonObject.getValue("operations").jsonArray.map { it.jsonObject }
        assertEquals(operations.map { it.text("name") }, DataOperations.all.map { it.name })
        assertEquals(operations.map { it.text("inputType") }, DataOperations.all.map { it.inputType })
        assertEquals(operations.map { it.text("document") }, DataOperations.all.map { it.document })
    }

    @Test
    fun theSchemaVersionIsTheContractTheModelWasBuiltFromAndTheTagItsVersionNames() {
        val model = model ?: return skipped()
        assertEquals(model.text("contract"), SCHEMA_VERSION)
        val version = System.getProperty("mountaya.version")?.let(::File)?.takeIf { it.isFile } ?: return skipped()
        assertEquals(version.readText().trim(), SCHEMA_VERSION)
    }

    /**
     * `modelDigest` (`sdk/SPEC.md` section 11), taken here with the JDK's own SHA-256 rather than the printer's:
     * each of the five inputs as its path, a line feed, its length in bytes, a line feed and its bytes.
     */
    @Test
    fun theModelDigestIsTheDigestOfTheFilesTheSurfaceWasPrintedFrom() {
        assertEquals(64, MODEL_DIGEST.length)
        assertTrue(MODEL_DIGEST.all { it in '0'..'9' || it in 'a'..'f' }, MODEL_DIGEST)
        val checkout =
            System
                .getProperty("mountaya.model")
                ?.let(::File)
                ?.takeIf { it.isFile }
                ?.parentFile
                ?.parentFile ?: return skipped()
        val sha256 = MessageDigest.getInstance("SHA-256")
        for (path in DIGEST_INPUTS) {
            val bytes = File(checkout, path).readBytes()
            sha256.update("$path\n${bytes.size}\n".encodeToByteArray())
            sha256.update(bytes)
        }
        assertEquals(sha256.digest().joinToString("") { "%02x".format(it) }, MODEL_DIGEST)
    }

    private fun assertFacts(
        operation: JsonObject,
        facts: OperationFacts,
    ) {
        val id = operation.text("operationId")
        assertEquals(operation.text("method"), facts.method, "$id: method")
        assertEquals(operation.text("path"), facts.path, "$id: path")
        assertEquals(operation.flag("idempotent"), facts.idempotent, "$id: x-idempotent")
        assertCredentials(id, operation.getValue("credentials").jsonObject, facts.credentials)
        assertEquals(false, facts.credentials.read, "$id: a REST or Tile operation is a read by its method alone")
        val timeoutMs = operation["timeoutMs"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.int
        assertEquals(timeoutMs, facts.timeoutMs, "$id: timeoutMs")
        assertEquals(timeoutMs?.milliseconds, facts.timeout, "$id: timeout")
        assertEquals(operation.flag("idempotencyKey"), facts.declaresIdempotencyKey, "$id: idempotencyKey")
        assertEquals(operation.flag("conditional"), facts.isConditional, "$id: conditional")
        assertEquals(operation["notModified"]?.jsonPrimitive?.boolean ?: false, facts.answersNotModified, "$id: notModified")
        val pagination =
            (operation["pagination"] as? JsonObject)?.let { paged ->
                Pagination(
                    paged.getValue("sort").jsonArray.map { it.jsonPrimitive.content },
                    paged["defaultSort"]?.takeIf { it !is JsonNull }?.jsonPrimitive?.content,
                )
            }
        assertEquals(pagination, facts.pagination, "$id: pagination")
        val success = operation.getValue("success").jsonObject
        val kind =
            when (success.text("kind")) {
                "envelope" -> SuccessKind.Envelope
                "event-stream" -> SuccessKind.EventStream
                else -> SuccessKind.Binary
            }
        assertEquals(kind, facts.success, "$id: success")
        assertEquals(successes(success), facts.successResponses, "$id: success statuses and their headers, in the model's order")
        val errors = (operation["errors"] as? JsonArray).orEmpty().map { it.jsonObject.getValue("status").jsonPrimitive.int }
        assertEquals(errors.sorted(), facts.errorStatuses, "$id: error statuses, ascending")
        val tasks = (operation["tasks"] as? JsonArray).orEmpty().map { it.jsonPrimitive.content }
        assertEquals(tasks, facts.tasks, "$id: the tasks a write runs, in the model's order")
    }

    /** Holds the [printed] credential facts of the operation [id] to the model's [credentials]. */
    private fun assertCredentials(
        id: String,
        credentials: JsonObject,
        printed: CredentialDeclaration,
    ) {
        assertEquals(credentials.text("actingUser"), printed.actingUser.value, "$id: actingUser")
        assertEquals(credentials.flag("acceptsShareToken"), printed.acceptsShareToken, "$id: acceptsShareToken")
        assertEquals(credentials.flag("requiresSession"), printed.requiresSession, "$id: requiresSession")
        assertEquals(credentials.flag("requiresSecretKey"), printed.requiresSecretKey, "$id: requiresSecretKey")
        assertEquals(credentials.flag("takesNoCredential"), printed.takesNoCredential, "$id: takesNoCredential")
        assertEquals(credentials.flag("pendingInvitee"), printed.pendingInvitee, "$id: pendingInvitee")
    }

    /** Every status a model's [success] arrives with and the names of the headers each declares, in its order. */
    private fun successes(success: JsonObject): List<SuccessResponse> =
        success.getValue("responses").jsonArray.map { response ->
            val declared = (response.jsonObject["headers"] as? JsonArray).orEmpty()
            SuccessResponse(
                response.jsonObject.getValue("status").jsonPrimitive.int,
                declared.map { it.jsonObject.text("name") },
            )
        }

    private fun JsonObject.text(key: String): String = getValue(key).jsonPrimitive.content

    private fun JsonObject.flag(key: String): Boolean = getValue(key).jsonPrimitive.boolean

    private companion object {
        /** The inputs of `modelDigest`, each a path from the contract checkout's root, in the order it reads them. */
        val DIGEST_INPUTS =
            listOf(
                "sdk/model.json",
                "sdk/elements/words.json",
                "sdk/elements/tokens.json",
                "sdk/elements/glyphs.json",
                "sdk/elements/NOTICE",
            )
    }

    private fun skipped() = println("OperationTableTest: skipped, the contract checkout's sdk/model.json is absent.")
}
