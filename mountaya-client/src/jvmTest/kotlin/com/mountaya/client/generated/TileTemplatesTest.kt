package com.mountaya.client.generated

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The templates gate: every route `sdk/model.json` lists under `templates`, the ones a map renderer fetches on its
 * own, against the `UrlTemplate` printed for it, in the model's order, so a route the model adds, drops or moves
 * fails here before a renderer reads a stale address.
 */
class TileTemplatesTest {
    private val model: JsonObject? =
        System
            .getProperty("mountaya.model")
            ?.let(::File)
            ?.takeIf { it.isFile }
            ?.let { Json.parseToJsonElement(it.readText()).jsonObject }

    @Test
    fun everyTileTemplateIsPrintedAsTheModelListsIt() {
        val model = model ?: return skipped()
        val declared = model.getValue("tiles").jsonObject.getValue("templates").jsonArray.map { it.jsonObject }

        assertEquals(declared.map { it.text("operationId") }, TileTemplates.all.map { it.operationId }, "in order")
        for ((route, printed) in declared.zip(TileTemplates.all)) {
            val id = printed.operationId
            assertEquals(route.text("path"), printed.path, id)
            val named = Regex("\\{([^}]+)}").findAll(route.text("path")).map { it.groupValues[1] }.toList()
            assertEquals(named, printed.pathParameters, "$id: the path's parameters, in the path's order")
            assertEquals(route.names("pathParameters").toSet(), printed.pathParameters.toSet(), "$id: as declared")
            assertEquals(route.names("queryParameters"), printed.queryParameters, "$id: the query, in order")
            val success = route.getValue("success").jsonObject
            assertEquals(
                success.getValue("mediaTypes").jsonArray.map { it.jsonPrimitive.content },
                printed.mediaTypes,
                "$id: the media types",
            )
        }
    }

    @Test
    fun theRestApiListsNoTemplateSoNoneIsPrintedForIt() {
        val model = model ?: return skipped()

        assertEquals(emptyList(), model.getValue("rest").jsonObject["templates"]?.jsonArray.orEmpty())
    }

    private fun JsonObject.text(key: String): String = getValue(key).jsonPrimitive.content

    private fun JsonObject.names(key: String): List<String> =
        this[key]?.jsonArray.orEmpty().map { it.jsonObject.text("name") }

    private fun skipped() = println("TileTemplatesTest: skipped, the contract checkout's sdk/model.json is absent.")
}
