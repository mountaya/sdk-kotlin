package com.mountaya.conformance

import com.mountaya.admin.admin
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.client.Mountaya
import com.mountaya.client.MountayaException
import com.mountaya.client.OperationDeclaration
import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * `transport.response.envelope`: what a method returns from the case's body, sent by a scripted
 * exchange as a 200.
 *
 * A REST case calls an operation by its declaration, reading `data` and `extensions.metadata` as
 * open JSON, and writes the envelope back as it came: `extensions` beside `data` where the answer
 * carried metadata. A Data API case calls the printed `directions`, the operation the case names,
 * and writes its result back as JSON.
 *
 * A REST case naming an `operation` calls that operation's printed method, which decodes the answer
 * through its printed types, the caller's `request` decoded into the printed body type from the JSON
 * the case writes and held to be sent as written; the envelope is written back through the printed
 * types' own serializers, every member written, and each JSON Pointer of the case's `reads` is
 * answered from it. A call refused before it is sent answers `reads` null and the class it raised.
 */
object TransportResponseEnvelopeRunner : ConformanceRunner {
    /** A Json writing every member of a printed answer, a null and a default included. */
    private val written = Json { encodeDefaults = true }

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val input = case.fields
        val body = input["body"] ?: throw AssertionError("the input has no `body`")
        val exchange = Scripted.Exchange(scope, listOf(Scripted.Reply.Answer(200, body = Scripted.json(body))))
        val operation = input.textOrNull("operation")
        val surface = input.text("surface")
        return when {
            surface == "rest" && operation != null -> {
                val pointers = pointers(case)
                val client = Scripted.client(scope, exchange, Scripted.session, Scripted.noRetries)
                try {
                    val envelope = called(client, operation, input["request"])
                    val request = input["request"]
                    if (request != null) {
                        val sent =
                            exchange.sent
                                .singleOrNull()
                                ?.request
                                ?.body
                                ?.decodeToString()
                        if (sent == null || Json.parseToJsonElement(sent) != fixtureJson(request)) {
                            throw AssertionError("the call sent $sent, not ${fixtureJson(request)}")
                        }
                    }
                    ConformanceOutcome.Value(
                        Scripted.objectOf(
                            "reads" to FixtureValue.Object(pointers.map { it to fixtureValueOf(at(envelope, it)) }),
                            "refused" to FixtureValue.Null,
                        ),
                    )
                } catch (error: MountayaException) {
                    if (exchange.sent.isNotEmpty()) {
                        throw AssertionError(
                            "the call was refused and sent ${exchange.sent.size}",
                        )
                    }
                    ConformanceOutcome.Value(
                        Scripted.objectOf("reads" to FixtureValue.Null, "refused" to FixtureValue.Text(error.name)),
                    )
                }
            }

            surface == "rest" -> {
                val client = Scripted.client(scope, exchange)
                val response =
                    client.send(
                        OperationDeclaration("readEnvelope", "GET", idempotent = true),
                        "/v1/things",
                        JsonElement.serializer(),
                        JsonElement.serializer().nullable,
                    )
                val envelope = mutableListOf("data" to fixtureValueOf(response.data))
                response.metadata?.let { metadata ->
                    envelope += "extensions" to FixtureValue.Object(listOf("metadata" to fixtureValueOf(metadata)))
                }
                answer(null, FixtureValue.Object(envelope))
            }

            surface == "data" -> {
                if (input.text("operation") !=
                    "directions"
                ) {
                    throw AssertionError("the runner calls `directions` and nothing else")
                }
                val client = Scripted.client(scope, exchange, credential = Scripted.session)
                try {
                    val result = client.data.directions(directionsInput)
                    answer(
                        null,
                        fixtureValueOf(
                            Json.encodeToJsonElement(DataSchema.DirectionsResult.serializer(), result) as JsonObject,
                        ),
                    )
                } catch (error: MountayaException) {
                    answer(FixtureValue.Text(error.name), FixtureValue.Null)
                }
            }

            else -> {
                throw AssertionError("no surface is called `$surface`")
            }
        }
    }

    /**
     * Calls [operation]'s printed method, its body decoded from [request] where it takes one, and answers
     * the envelope it returned, written back through the printed types.
     */
    private suspend fun called(
        client: Mountaya,
        operation: String,
        request: FixtureValue?,
    ): JsonElement {
        val organizations = client.admin.organizations
        return when (operation) {
            "getOrganizationAiProviderConfig" -> {
                val response = organizations.getOrganizationAiProviderConfig("org")
                envelope(
                    written.encodeToJsonElement(AdminSchema.AiProviderConfig.serializer().nullable, response.data),
                    response.metadata,
                )
            }

            "patchOrganizationAiProviderConfig" -> {
                val body =
                    Json.decodeFromJsonElement(
                        AdminSchema.AiProviderConfigUpdateParams.serializer(),
                        fixtureJson(request ?: throw AssertionError("the case builds no `request`")),
                    )
                val response = organizations.patchOrganizationAiProviderConfig("org", body)
                envelope(
                    written.encodeToJsonElement(AdminSchema.AiProviderConfig.serializer(), response.data),
                    response.metadata,
                )
            }

            "getOrganizationWebhookEvent" -> {
                val response = client.admin.webhooks.getOrganizationWebhookEvent("org", "event")
                envelope(
                    written.encodeToJsonElement(AdminSchema.WebhookEventWithDeliveries.serializer(), response.data),
                    response.metadata,
                )
            }

            else -> {
                throw AssertionError("the runner calls no operation `$operation`")
            }
        }
    }

    /**
     * The JSON Pointers [case] asks about, the keys of its expected `reads`, none where it expects the
     * call refused: what the case asks, read off its expectation as the question it is, never as an answer.
     */
    private fun pointers(case: ConformanceCase): List<String> {
        val written =
            case.fixture.items("cases").firstOrNull { (it as? FixtureValue.Object)?.textOrNull("name") == case.name }
                as? FixtureValue.Object ?: throw AssertionError("the fixture holds no case `${case.name}`")
        return written.objectOrNull("expect")?.objectOrNull("reads")?.keys ?: emptyList()
    }

    /** An envelope of [data], with `extensions.metadata` beside it where the answer carried [metadata]. */
    private fun envelope(
        data: JsonElement,
        metadata: JsonObject?,
    ): JsonElement =
        JsonObject(
            buildMap {
                put("data", data)
                if (metadata != null) put("extensions", JsonObject(mapOf("metadata" to metadata)))
            },
        )

    /** The value at the JSON Pointer [pointer] in [json], which must hold one. */
    private fun at(
        json: JsonElement,
        pointer: String,
    ): JsonElement {
        if (pointer.isEmpty()) return json
        if (!pointer.startsWith("/")) throw AssertionError("`$pointer` is no JSON Pointer")
        var here = json
        for (escaped in pointer.substring(1).split('/')) {
            val token = escaped.replace("~1", "/").replace("~0", "~")
            here =
                when (here) {
                    is JsonObject -> here[token]
                    is JsonArray -> token.toIntOrNull()?.let { here.getOrNull(it) }
                    else -> null
                } ?: throw AssertionError("the envelope holds nothing at `$pointer`")
        }
        return here
    }

    private fun answer(
        refused: FixtureValue?,
        returned: FixtureValue,
    ): ConformanceOutcome =
        ConformanceOutcome.Value(
            Scripted.objectOf(
                "refused" to (refused ?: FixtureValue.Null),
                "returned" to returned,
            ),
        )
}

/** The input every runner's Data API call sends: a hike between two points. */
internal val directionsInput: DataSchema.DirectionsInput =
    DataSchema.DirectionsInput(DataSchema.ActivitySlug("HIKING"), listOf(listOf(6.86, 45.83), listOf(6.87, 45.84)))
