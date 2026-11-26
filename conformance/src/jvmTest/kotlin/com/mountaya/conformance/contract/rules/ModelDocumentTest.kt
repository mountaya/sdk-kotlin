package com.mountaya.conformance

import com.mountaya.client.DataOperation
import com.mountaya.client.FieldSelection
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The document printer against every document `sdk/model.json` carries.
 *
 * The model is where the contract writes the full selection's bytes once for every SDK, and the
 * persisted-query digest is taken over them, so a printer that differed by one space would send a
 * digest the server never stored. It runs on the jvm, the one target that reads a file of the
 * checkout; the printer is common code, so what it proves holds on every target.
 */
class ModelDocumentTest {
    @Test
    fun everyDataApiOperationPrintsTheFullDocumentTheModelCarries() {
        val model = System.getProperty("mountaya.model")?.let(::File)?.takeIf { it.isFile }
        if (model == null) {
            println("ModelDocumentTest: skipped, the contract checkout's sdk/model.json is absent.")
            return
        }
        val operations =
            Json
                .parseToJsonElement(model.readText())
                .jsonObject
                .getValue("data")
                .jsonObject
                .getValue("operations")
                .jsonArray
        assertTrue(operations.isNotEmpty(), "the model carries no Data API operation")

        for (element in operations) {
            val operation = element.jsonObject
            val name = operation.getValue("name").jsonPrimitive.content
            val printed =
                DataOperation(
                    name = name,
                    inputType = operation.getValue("inputType").jsonPrimitive.content,
                    selection = FieldSelection { read(operation.getValue("selection").jsonArray) },
                )

            assertEquals(operation.getValue("document").jsonPrimitive.content, printed.document, name)
            assertEquals(printed.document, printed.document(selecting = printed.selection), name)
        }
    }

    /** Reads the model's field tree: an array of fields, each carrying its own `fields` where it is an object. */
    private fun FieldSelection.Builder.read(fields: JsonArray) {
        for (element in fields) {
            val entry = element.jsonObject
            val name = entry.getValue("name").jsonPrimitive.content
            val beneath = entry["fields"]
            if (beneath == null) field(name) else field(name) { read(beneath.jsonArray) }
        }
    }
}
