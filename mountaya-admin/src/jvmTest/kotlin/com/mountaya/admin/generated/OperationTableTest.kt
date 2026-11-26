package com.mountaya.admin.generated

import com.mountaya.client.CredentialDeclaration
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.OperationFacts
import com.mountaya.client.Pagination
import com.mountaya.client.SuccessKind
import com.mountaya.client.SuccessResponse
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/**
 * The facts gate of the admin unit: every operation it prints against `sdk/model.json`, fact by fact, as the core's
 * own facts test holds the core's.
 *
 * The model states the unit of every REST operation, and the admin unit prints those it states `admin`, the `admin`
 * group whole, `postSession` among them: idempotence, the six credential facts, the method, the path, the attempt
 * budget, whether the operation takes an `Idempotency-Key` and an `If-Match`, how it pages, its success statuses
 * with the headers each declares, its error statuses and the tasks a write runs. A printer that dropped one would
 * ship an admin unit that repeats a write or sends a credential an operation refuses, with nothing else failing.
 */
@OptIn(InternalMountayaApi::class)
class OperationTableTest {
    private val model: JsonObject? =
        System
            .getProperty("mountaya.model")
            ?.let(::File)
            ?.takeIf { it.isFile }
            ?.let { Json.parseToJsonElement(it.readText()).jsonObject }

    /** The REST API's operations the model states the admin unit's, in its order. */
    private fun JsonObject.adminOperations(): List<JsonObject> =
        getValue("rest").jsonObject.getValue("operations").jsonArray.map { it.jsonObject }.filter {
            it.text("unit") == "admin"
        }

    @Test
    fun everyOperationOfTheAdminUnitIsPrintedWithTheFactsTheModelDeclares() {
        val model = model ?: return skipped()
        val declared = model.adminOperations()
        val printed = OperationTable.restOperations
        assertEquals(declared.map { it.text("operationId") }, printed.map { it.operationId }, "the operations, in order")
        for ((operation, facts) in declared.zip(printed)) assertFacts(operation, facts)
    }

    @Test
    fun theTableHoldsEveryOperationOfTheAdminGroupOnceAndNothingElse() {
        val model = model ?: return skipped()
        val printed = OperationTable.restOperations
        assertEquals(model.adminOperations().size, printed.size)
        assertEquals(printed.size, printed.map { it.operationId }.toSet().size)
        assertEquals(listOf(Surface.Rest), printed.map { it.surface }.distinct())
        val groups =
            model.adminOperations().map { it.text("namespace").substringBefore('.') }.distinct()
        assertEquals(listOf("admin"), groups)
        // The one operation a secret key authenticates is the admin unit's.
        assertEquals(listOf("postSession"), printed.filter { it.credentials.requiresSecretKey }.map { it.operationId })
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

    private fun skipped() = println("OperationTableTest: skipped, the contract checkout's sdk/model.json is absent.")
}
