package com.mountaya.build.generator

import com.mountaya.build.generator.KotlinText.kdoc
import com.mountaya.build.generator.OperationPrinter.printBody
import com.mountaya.build.generator.OperationPrinter.printDataFacts
import com.mountaya.build.generator.OperationPrinter.printDataMethod
import com.mountaya.build.generator.OperationPrinter.printDataOperation
import com.mountaya.build.generator.OperationPrinter.printFacts
import com.mountaya.build.generator.OperationPrinter.printMethods
import com.mountaya.build.generator.OperationPrinter.printParameters
import com.mountaya.build.generator.OperationPrinter.printWalk
import com.mountaya.build.generator.SchemaPrinter.printAlias
import com.mountaya.build.generator.SchemaPrinter.printClass
import com.mountaya.build.generator.SchemaPrinter.printEnum
import com.mountaya.build.generator.SchemaPrinter.printInlineUnion
import com.mountaya.build.generator.SchemaPrinter.printUnion

/**
 * ClientPrinter prints the generated surface of one unit of the client from the model: every file of the core's
 * package, `com.mountaya.client.generated`, which `mountaya-client` holds, or of the admin unit's,
 * `com.mountaya.admin.generated`, which `mountaya-admin` holds, keyed by its name.
 *
 * The model states the unit of every REST operation, webhook event and component, and the printer splits the
 * client by that key and by no list of its own (`sdk/SPEC.md`, "The three layers, in every language"): the core
 * prints the `studio` and `agent` groups, the Tile and Data APIs and every component the admin unit does not hold
 * alone; the admin unit prints the `admin` group, the webhook events and the components only those reach, naming
 * the core's in full. Either print reads and checks the whole model, so a defect in one unit is refused by both.
 *
 * The bytes are final. No formatter runs between this and the file, so a formatter release
 * cannot move the output, and the check compares exactly what this returns.
 */
object ClientPrinter {
    /** The core's generated package, in `mountaya-client`. */
    const val PACKAGE = "com.mountaya.client.generated"

    /** The admin unit's generated package, in `mountaya-admin`. */
    const val ADMIN_PACKAGE = "com.mountaya.admin.generated"

    /** The object the admin unit's REST components are nested in, beside the core's `RestSchema`. */
    const val ADMIN_NAMESPACE = "AdminSchema"

    /** The hand-written class a route a map renderer fetches on its own is printed as. */
    private const val URL_TEMPLATE = "com.mountaya.client.UrlTemplate"

    /** The directory of the generated package, below a source set's `kotlin` directory. */
    const val DIRECTORY = "com/mountaya/client/generated"

    /** The group whose accessor the admin unit declares on the core's client, and the one group it prints. */
    private const val ADMIN_GROUP = "admin"

    /** The one type an error's code is printed as on every surface (`sdk/SPEC.md` section 3). */
    const val API_ERROR_CODE = "ApiErrorCode"

    /** The name the REST and Tile bundles give the inline vocabulary of an error entry's `extensions.code`. */
    private const val ENVELOPE_CODE = "ErrorExtensionsCode"

    /** The name the Data API's SDL gives the vocabulary of an error entry's `extensions.code`. */
    private const val DATA_CODE = "ErrorCode"

    /** Returns every file of the core's generated package, printed from files whose `modelDigest` is [digest]. */
    fun print(
        model: SdkModel,
        digest: String,
    ): Map<String, String> = print(model, ClientUnit.Core, digest)

    /** Returns the package [unit] is printed into. */
    fun packageOf(unit: ClientUnit): String =
        when (unit) {
            ClientUnit.Core -> PACKAGE
            ClientUnit.Admin -> ADMIN_PACKAGE
        }

    /** Returns the directory of [unit]'s generated package, below a source set's `kotlin` directory. */
    fun directoryOf(unit: ClientUnit): String = packageOf(unit).replace('.', '/')

    /**
     * Returns every file of [unit]'s generated package, printed from files whose `modelDigest` is [digest], which every
     * file's header names beside the contract's tag and `SchemaVersion.kt` publishes beside `SCHEMA_VERSION`
     * (`sdk/SPEC.md` section 11).
     */
    fun print(
        model: SdkModel,
        unit: ClientUnit,
        digest: String,
    ): Map<String, String> {
        if (!ModelDigest.isWellFormed(digest)) {
            throw ModelException("modelDigest: `$digest` is not 64 lowercase hexadecimal characters")
        }
        val files = sortedMapOf<String, String>()

        /** Returns [body] under the header every file of this unit's print starts with. */
        fun printed(
            body: String,
            timestamps: Boolean = false,
        ): String = file(model, digest, unit, body, timestamps)
        for (operation in model.rest.operations) {
            val group =
                operation.namespace?.substringBefore('.')
                    ?: throw ModelException("RestSchema: ${operation.operationId} has no `x-sdk-namespace`")
            // The admin unit declares one accessor on the core's client, `admin`, and the core declares none of
            // that name, so an operation of the group is the admin unit's and every other one the core's.
            if ((group == ADMIN_GROUP) != (operation.unit == ClientUnit.Admin)) {
                throw ModelException(
                    "RestSchema: ${operation.operationId} is an operation of the `$group` group in the " +
                        "${operation.unit.wire} unit, and the admin unit holds the `$ADMIN_GROUP` group and nothing else",
                )
            }
        }
        for (operation in model.tiles.operations) {
            if (operation.namespace !=
                null
            ) {
                throw ModelException(
                    "TileSchema: ${operation.operationId} has a namespace, and the Tile API is un-namespaced",
                )
            }
        }
        // The Tile API is the core's whole: the admin unit declares nothing of it.
        val tileAdmin =
            (model.tiles.operations + model.tiles.templates).map { it.operationId to it.unit } +
                model.tiles.schemas.map { it.name to it.unit }
        for ((name, stated) in tileAdmin) {
            if (stated != ClientUnit.Core) {
                throw ModelException("TileSchema: $name is the ${stated.wire} unit's, and the Tile API is the core's")
            }
        }
        for (template in model.rest.templates) {
            if (template.unit != ClientUnit.Core) {
                throw ModelException(
                    "RestSchema: the route ${template.operationId} is the ${template.unit.wire} unit's, and a " +
                        "route a map renderer fetches on its own is the core's",
                )
            }
        }

        // The Tile API delivers no webhook, and this printer prints a webhook event of the REST API's alone.
        if (model.tiles.webhooks.isNotEmpty()) {
            throw ModelException("tiles.webhooks: webhooks on a surface this printer prints none for")
        }
        // Webhook receiving is the admin unit's (`sdk/SPEC.md`, "The three layers, in every language").
        for (webhook in model.rest.webhooks) {
            if (webhook.unit != ClientUnit.Admin) {
                throw ModelException(
                    "rest.webhooks.${webhook.type}: the ${webhook.unit.wire} unit's, and webhook receiving is the " +
                        "admin unit's",
                )
            }
        }
        val core = OpenApiSurfacePlan("RestSchema", "the REST API", "Rest", model.rest, ClientUnit.Core)
        val admin = OpenApiSurfacePlan(ADMIN_NAMESPACE, "the REST API", "Rest", model.rest, ClientUnit.Admin)
        val tiles = OpenApiSurfacePlan("TileSchema", "the Tile API", "Tiles", model.tiles, ClientUnit.Core)
        val data = DataTypes(model.data)
        val webhooks = WebhookPrinter(model.rest, admin.types).print()

        val groups =
            model.rest.operations
                .filter { it.unit == unit }
                .groupBy { operation ->
                    val namespace =
                        checkNotNull(operation.namespace) {
                            "${operation.operationId} is a REST operation with no namespace"
                        }
                    namespace.substringBefore('.')
                }
        if (unit == ClientUnit.Admin) {
            files["$ADMIN_NAMESPACE.kt"] = printed(admin.printNamespace(), timestamps = true)
            webhooks?.let { files["WebhookEvent.kt"] = printed(it, timestamps = true) }
            for ((group, operations) in groups.toSortedMap()) {
                files["${accessorName(group)}.kt"] = printed(printGroup(group, operations, admin))
            }
            files["OperationTable.kt"] = printed(printTable(model, unit))
            return files
        }

        files["RestSchema.kt"] = printed(core.printNamespace(), timestamps = true)
        files["TileSchema.kt"] = printed(tiles.printNamespace(), timestamps = true)
        files["DataSchema.kt"] = printed(printDataNamespace(model.data, data), timestamps = true)

        for ((group, operations) in groups.toSortedMap()) {
            val name = accessorName(group)
            files["$name.kt"] = printed(printGroup(group, operations, core))
        }
        files["Tiles.kt"] = printed(printFlatAccessor("Tiles", "the Tile API", model.tiles.operations, tiles))
        // The routes a map renderer fetches on its own, which no method binds; a surface whose model lists none
        // prints no object of none.
        for ((name, surface, templates) in listOf(
            Triple("RestTemplates", "the REST API", model.rest.templates),
            Triple("TileTemplates", "the Tile API", model.tiles.templates),
        )) {
            if (templates.isNotEmpty()) files["$name.kt"] = printed(printTemplates(name, surface, templates))
        }
        files["Data.kt"] = printed(printDataAccessor(model.data, data))
        files["OperationTable.kt"] = printed(printTable(model, unit))
        files["DataOperations.kt"] = printed(printDataOperations(model.data))
        files["SchemaVersion.kt"] = printed(printSchemaVersion(model, digest))
        files["$API_ERROR_CODE.kt"] = printed(printApiErrorCode(model))
        return files
    }

    /**
     * The header every printed file starts with, then its body. A file of the admin unit opts in to the core's seam,
     * which it reads where every one of its methods hands its call over (SPEC.md section 7).
     */
    private fun file(
        model: SdkModel,
        digest: String,
        unit: ClientUnit,
        body: String,
        timestamps: Boolean = false,
    ): String =
        buildString {
            append("// GENERATED FILE. DO NOT EDIT.\n")
            append("//\n")
            append("// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract\n")
            append("// ").append(model.contract)
            append(", model digest ").append(digest)
            append(".\n")
            append("// Every name below is the contract's, verbatim, but an enum member's, printed as its words\n")
            append("// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,\n")
            append("// and the next print overwrites any edit.\n")
            append('\n')
            if (unit != ClientUnit.Core) {
                append("@file:kotlin.OptIn(").append(OperationPrinter.INTERNAL_API).append("::class)\n")
                if (!timestamps) append('\n')
            }
            if (timestamps) {
                // A property the contract declares a timestamp is read through the anchored rule, wherever it sits.
                append(
                    "@file:kotlinx.serialization.UseSerializers(com.mountaya.client.TimestampSerializer::class)\n",
                )
                append('\n')
            }
            append("package ").append(packageOf(unit)).append('\n')
            append('\n')
            append(body)
        }

    /** The accessor class a namespace segment is printed as. */
    private fun accessorName(segment: String): String {
        val name = KotlinText.capitalized(segment)
        if (!KotlinText.isPlain(name) || !KotlinText.isPlain(segment)) {
            throw ModelException("`$segment` is a namespace segment no accessor can be named after")
        }
        return name
    }

    private fun printGroup(
        group: String,
        operations: List<Operation>,
        plan: OpenApiSurfacePlan,
    ): String =
        buildString {
            val name = accessorName(group)
            val resources =
                operations
                    .groupBy { operation ->
                        val namespace =
                            checkNotNull(operation.namespace) {
                                "${operation.operationId} is in `$group` with no namespace"
                            }
                        namespace.substringAfter('.')
                    }.toSortedMap()
            kdoc("", listOf("$name is the `$group` operations of the REST API, one property per resource."))
            append("public class ").append(name).append(" internal constructor(\n")
            append("    sender: ").append(OperationPrinter.SENDER_TYPE).append(",\n")
            append(") {\n")
            for (resource in resources.keys) {
                kdoc("    ", listOf("The `$group.$resource` operations."))
                append("    public val ")
                    .append(KotlinText.identifier(resource))
                    .append(": ")
                    .append(name)
                    .append('.')
                append(accessorName(resource)).append(" = ").append(name).append('.')
                append(accessorName(resource)).append("(sender)\n")
                append('\n')
            }
            var first = true
            for ((resource, members) in resources) {
                if (!first) append('\n')
                first = false
                kdoc("    ", listOf("${accessorName(resource)} is the `$group.$resource` operations of the REST API."))
                append("    public class ").append(accessorName(resource)).append(" internal constructor(\n")
                append("        private val sender: ").append(OperationPrinter.SENDER_TYPE).append(",\n")
                append("    ) {\n")
                checkWalkNames("$name.${accessorName(resource)}", members)
                members.forEachIndexed { index, operation ->
                    if (index > 0) append('\n')
                    printMethods(operation, "OperationTable.Rest", plan.types, "        ")
                    if (operation.pagination != null) {
                        append('\n')
                        printWalk(operation, plan.types, "        ")
                    }
                }
                append("    }\n")
            }
            append("}\n")
        }

    private fun printFlatAccessor(
        name: String,
        surface: String,
        operations: List<Operation>,
        plan: OpenApiSurfacePlan,
    ): String =
        buildString {
            kdoc("", listOf("$name is the operations of $surface, which is un-namespaced."))
            append("public class ").append(name).append(" internal constructor(\n")
            append("    private val sender: ").append(OperationPrinter.SENDER_TYPE).append(",\n")
            append(") {\n")
            checkWalkNames(name, operations)
            operations.forEachIndexed { index, operation ->
                if (index > 0) append('\n')
                printMethods(operation, "OperationTable.${plan.surface}", plan.types, "    ")
                if (operation.pagination != null) {
                    append('\n')
                    printWalk(operation, plan.types, "    ")
                }
            }
            append("}\n")
        }

    /**
     * Prints [templates], the routes of [surface] a map renderer fetches on its own, as the object [name]: one
     * `UrlTemplate` per route, named by its `operationId`, holding its path, the path's parameters in the order
     * the path names them, its query parameters and the media types it answers in, then `all`, every one in the
     * model's order. A route named `all`, or whose path and declared parameters disagree, is refused.
     */
    private fun printTemplates(
        name: String,
        surface: String,
        templates: List<Operation>,
    ): String =
        buildString {
            kdoc(
                "",
                listOf(
                    KotlinText.wrapped(
                        "$name is the routes of $surface a map renderer fetches on its own, the tiles and the base " +
                            "map's style, TileJSON, sprite and glyph documents: URL templates resolved against the " +
                            "surface's origin, which no method binds.",
                        100 - 3,
                    ),
                ),
            )
            append("public object ").append(name).append(" {\n")
            for (template in templates) {
                val id = template.operationId
                if (id == "all") throw ModelException("$name: a route named `all`, which the list of every route takes")
                val path = templatePathParameters(template)
                kdoc("    ", listOf("`$id`: ${template.summary.orEmpty()}".trimEnd(':', ' '), template.description))
                append("    public val ").append(KotlinText.identifier(id)).append(": $URL_TEMPLATE =\n")
                append("        $URL_TEMPLATE(\n")
                append("            operationId = ").append(KotlinText.literal(id)).append(",\n")
                append("            path = ").append(KotlinText.literal(template.path)).append(",\n")
                append("            pathParameters = ").append(literals(path)).append(",\n")
                append("            queryParameters = ").append(literals(template.queryParameters.map { it.name }))
                append(",\n")
                append("            mediaTypes = ").append(literals(template.success.mediaTypes)).append(",\n")
                append("        )\n\n")
            }
            kdoc("    ", listOf("Every route of $surface a map renderer fetches on its own, in the model's order."))
            append("    public val all: kotlin.collections.List<$URL_TEMPLATE> =\n")
            append("        kotlin.collections.listOf(\n")
            for (template in templates) {
                val member = KotlinText.identifier(template.operationId)
                append("            ").append(member).append(",\n")
            }
            append("        )\n")
            append("}\n")
        }

    /**
     * The parameters [template]'s path names, in the order it names them, refused where the route declares
     * parameters its path does not name, or names one it does not declare.
     */
    private fun templatePathParameters(template: Operation): List<String> {
        val named = Regex("\\{([^}]+)}").findAll(template.path).map { it.groupValues[1] }.toList()
        val declared = template.pathParameters.map { it.name }
        if (declared.isNotEmpty() && (declared.toSet() != named.toSet() || declared.size != named.size)) {
            throw ModelException(
                "${template.operationId}: its path names ${named.joinToString()}, and it declares ${declared.joinToString()}",
            )
        }
        return named
    }

    /** [values] as a printed list of string literals, or an empty list. */
    private fun literals(values: List<String>): String =
        if (values.isEmpty()) {
            "kotlin.collections.emptyList()"
        } else {
            "kotlin.collections.listOf(" + values.joinToString(", ") { KotlinText.literal(it) } + ")"
        }

    /** Refuses a paged operation of [accessor] whose walk's name another of its operations holds. */
    private fun checkWalkNames(
        accessor: String,
        operations: List<Operation>,
    ) {
        val names = operations.map { it.operationId }.toSet()
        val taken = operations.filter { it.pagination != null }.map(OperationPrinter::walkName).filter { it in names }
        if (taken.isNotEmpty()) {
            throw ModelException(
                "$accessor: ${taken.joinToString()} would be both an operation and a walk, and the printer renames nothing",
            )
        }
    }

    private fun printDataAccessor(
        data: DataSurface,
        types: DataTypes,
    ): String =
        buildString {
            kdoc(
                "",
                listOf(
                    "Data is the operations of the Data API, which is un-namespaced. Each returns " +
                        "its result type whole, and a " +
                        "field the caller did not select is null in it.",
                ),
            )
            append("public class Data internal constructor(\n")
            append("    private val sender: ").append(OperationPrinter.SENDER_TYPE).append(",\n")
            append(") {\n")
            data.operations.forEachIndexed { index, operation ->
                if (index > 0) append('\n')
                printDataMethod(operation, types, "    ")
            }
            append("}\n")
        }

    /**
     * Prints the facts of every operation [unit] prints a method for: the core's REST operations, the Tile API's and
     * the Data API's, or the admin unit's REST operations alone.
     */
    private fun printTable(
        model: SdkModel,
        unit: ClientUnit,
    ): String =
        buildString {
            val rest = model.rest.operations.filter { it.unit == unit }
            kdoc(
                "",
                listOf(
                    "OperationTable is what the contract declares about each printed operation, one " +
                        "property per operation and " +
                        "nothing inferred from a method. A test holds it to `sdk/model.json` fact by fact.",
                ),
            )
            append("internal object OperationTable {\n")
            val surfaces =
                if (unit == ClientUnit.Core) {
                    listOf("Rest" to rest, "Tiles" to model.tiles.operations)
                } else {
                    listOf("Rest" to rest)
                }
            for ((surface, operations) in surfaces) {
                kdoc(
                    "    ",
                    listOf(
                        "The operations of the ${if (surface == "Rest") "REST" else "Tile"} API, in the model's order.",
                    ),
                )
                append("    internal object ").append(surface).append(" {\n")
                operations.forEachIndexed { index, operation ->
                    if (index > 0) append('\n')
                    printFacts(operation, surface, "        ")
                }
                append("    }\n\n")
            }
            if (unit != ClientUnit.Core) {
                kdoc(
                    "    ",
                    listOf("Every operation of the REST API the ${unit.wire} unit prints, in the model's order."),
                )
                append("    val restOperations: kotlin.collections.List<com.mountaya.client.OperationFacts> =\n")
                append("        kotlin.collections.listOf(\n")
                for (operation in rest) append("            Rest.${operation.operationId},\n")
                append("        )\n")
                append("}\n")
                return@buildString
            }
            kdoc("    ", listOf("The operations of the Data API, in the model's order."))
            append("    internal object Data {\n")
            model.data.operations.forEachIndexed { index, operation ->
                if (index > 0) append('\n')
                printDataFacts(operation, model.data.endpoint, "        ")
            }
            append("    }\n\n")
            kdoc(
                "    ",
                listOf(
                    "The path every Data API operation is sent to under the Data API's origin, a call by " +
                        "declaration's included.",
                ),
            )
            val endpoint = KotlinText.literal(model.data.endpoint)
            append("    val dataEndpoint: kotlin.String = ").append(endpoint).append("\n\n")
            val lists =
                listOf(
                    Triple("restOperations", "Rest", rest.map { it.operationId }),
                    Triple("tileOperations", "Tiles", model.tiles.operations.map { it.operationId }),
                    Triple("dataOperations", "Data", model.data.operations.map { KotlinText.identifier(it.name) }),
                )
            val titles =
                mapOf("Rest" to "the REST API the core prints", "Tiles" to "the Tile API", "Data" to "the Data API")
            lists.forEachIndexed { index, (list, surface, names) ->
                if (index > 0) append('\n')
                kdoc("    ", listOf("Every operation of ${titles.getValue(surface)}, in the model's order."))
                append("    val ").append(list)
                append(": kotlin.collections.List<com.mountaya.client.OperationFacts> =\n")
                append("        kotlin.collections.listOf(\n")
                for (name in names) append("            $surface.$name,\n")
                append("        )\n")
            }
            append("}\n")
        }

    private fun printDataOperations(data: DataSurface): String =
        buildString {
            kdoc(
                "",
                listOf(
                    "DataOperations is each Data API operation with its full field tree, which a caller's selection is held to.",
                ),
            )
            append("internal object DataOperations {\n")
            data.operations.forEachIndexed { index, operation ->
                if (index > 0) append('\n')
                printDataOperation(operation, "    ")
            }
            append('\n')
            kdoc("    ", listOf("Every Data API operation, in the model's order."))
            append("    val all: kotlin.collections.List<com.mountaya.client.DataOperation> =\n")
            append("        kotlin.collections.listOf(\n")
            for (operation in data.operations) {
                append("            ").append(KotlinText.identifier(operation.name)).append(",\n")
            }
            append("        )\n")
            append("}\n")
        }

    /**
     * Prints [API_ERROR_CODE], the one open vocabulary of an error's code (`sdk/SPEC.md` section 3): the union of the
     * codes the REST API's and the Tile API's [ENVELOPE_CODE] and the Data API's [DATA_CODE] declare, each once, in the
     * order the REST API, then the Tile API, then the Data API first names it, every constant saying which surfaces
     * declare it. Two surfaces declaring one code under two spellings print two constants, which the member check
     * refuses where their names meet.
     */
    private fun printApiErrorCode(model: SdkModel): String {
        val declared = linkedMapOf<String, MutableList<String>>()
        for ((surface, codes) in listOf(
            "the REST API" to envelopeCodes("RestSchema", model.rest),
            "the Tile API" to envelopeCodes("TileSchema", model.tiles),
            "the Data API" to
                model.data.enums
                    .filter { it.name == DATA_CODE }
                    .flatMap { enum -> enum.members.map { it.name } },
        )) {
            for (code in codes) declared.getOrPut(code) { mutableListOf() } += surface
        }
        val members =
            declared.map { (code, surfaces) ->
                SchemaPrinter.EnumConstant(
                    EnumMembers.data(API_ERROR_CODE, code),
                    code,
                    KotlinText.literal(code),
                    "The `$code` code, which ${spoken(surfaces)} ${if (surfaces.size == 1) "declares" else "declare"}.",
                    false,
                )
            }
        return buildString {
            printEnum(
                PACKAGE,
                API_ERROR_CODE,
                "$API_ERROR_CODE is the code an error's entry carries under `extensions.code`, on every surface the\n" +
                    "client calls: one vocabulary, the union of the codes the REST API's and the Tile API's\n" +
                    "`$ENVELOPE_CODE` and the Data API's `$DATA_CODE` declare, which an error's `code`, each of its\n" +
                    "entries' `code` and its `hasCode` take. A code a later release of the API sends is kept " +
                    "exactly as\n" +
                    "sent and never refused.",
                "kotlin.String",
                members,
                "",
            )
        }
    }

    /**
     * The codes [namespace]'s envelope declares for an error entry's `extensions.code`, the inline [ENVELOPE_CODE] of
     * its envelope's components, in the bundle's order, or none where the surface declares none. A vocabulary of
     * anything but text, or two that differ, is refused.
     */
    private fun envelopeCodes(
        namespace: String,
        surface: OpenApiSurface,
    ): List<String> {
        val found = mutableListOf<List<Any>>()

        fun visit(type: TypeNode) {
            when (type) {
                is ScalarType -> if (type.enumName == ENVELOPE_CODE) found += type.enumValues.orEmpty()
                is ArrayType -> visit(type.items)
                is MapType -> visit(type.values)
                is InlineObject -> type.properties.forEach { visit(it.type) }
                is InlineUnion -> type.members.forEach(::visit)
                is RefType, is AnyType, is NullType -> Unit
            }
        }
        for (schema in surface.schemas.filter { it.role == "envelope" }) {
            when (schema) {
                is ObjectSchema -> schema.properties.forEach { visit(it.type) }
                is AliasSchema -> visit(schema.type)
                else -> Unit
            }
        }
        val codes = found.distinct()
        if (codes.size > 1) {
            throw ModelException(
                "$namespace.$ENVELOPE_CODE: declared twice, as two vocabularies, so no union is one",
            )
        }
        return codes.firstOrNull().orEmpty().map { code ->
            code as? String
                ?: throw ModelException(
                    "$namespace.$ENVELOPE_CODE: the code `$code` is not text, as every error code is",
                )
        }
    }

    /** Surfaces as prose: `a`, `a` and `b`, or `a`, `b` and `c`. */
    private fun spoken(surfaces: List<String>): String =
        if (surfaces.size ==
            1
        ) {
            surfaces.single()
        } else {
            surfaces.dropLast(1).joinToString(", ") + " and " + surfaces.last()
        }

    private fun printSchemaVersion(
        model: SdkModel,
        digest: String,
    ): String =
        buildString {
            kdoc(
                "",
                listOf(
                    "SCHEMA_VERSION is the contract tag this surface was printed from, which `VERSION` in\n" +
                        "mountaya/schemas names.",
                ),
            )
            append("public const val SCHEMA_VERSION: kotlin.String = ").append(KotlinText.literal(model.contract))
            append('\n')
            append('\n')
            kdoc(
                "",
                listOf(
                    "MODEL_DIGEST is the digest of the files this surface and the elements' data were printed " +
                        "from,\n`modelDigest` in mountaya/schemas: the model and the elements' tables, named " +
                        "exactly where the tag\nnames a release and the files move between two. A conformance " +
                        "report carries the same digest.",
                ),
            )
            append("public const val MODEL_DIGEST: kotlin.String = ").append(KotlinText.literal(digest))
            append('\n')
        }

    private fun printDataNamespace(
        data: DataSurface,
        types: DataTypes,
    ): String =
        buildString {
            val names = (data.objects + data.inputs).map { it.name } + data.enums.map { it.name }
            val duplicates = names.groupBy { it }.filterValues { it.size > 1 }.keys
            if (duplicates.isNotEmpty()) throw ModelException("DataSchema: ${duplicates.joinToString()} declared twice")
            kdoc("", listOf("DataSchema is the Data API's types, as the SDL names them."))
            append("public object DataSchema {\n")
            val declarations = mutableListOf<StringBuilder.() -> Unit>()
            for (type in data.objects) declarations += { printClass(ClassSpecs.dataObject(type, types), "    ") }
            for (type in data.inputs) declarations += { printClass(ClassSpecs.dataInput(type, types), "    ") }
            for (enum in data.enums) {
                declarations += {
                    printEnum(
                        "DataSchema",
                        enum.name,
                        enum.description,
                        "kotlin.String",
                        enum.members.map {
                            SchemaPrinter.EnumConstant(
                                EnumMembers.data(enum.name, it.name),
                                it.name,
                                KotlinText.literal(it.name),
                                it.description,
                                it.deprecated,
                            )
                        },
                        "    ",
                    )
                }
            }
            declarations.forEachIndexed { index, declaration ->
                if (index > 0) append('\n')
                declaration()
            }
            append("}\n")
        }
}

/**
 * What one OpenAPI surface prints into one unit's namespace: the unit's components but the envelope, the
 * inline schemas its components and operations name from where they sit, and the optional-parameter builder of
 * each of its operations. A unit other than the core names the core's components in full, through the core's own
 * namespace.
 */
internal class OpenApiSurfacePlan(
    val namespace: String,
    private val title: String,
    /** The name of the surface in the operation table: `Rest` or `Tiles`. */
    val surface: String,
    surfaceModel: OpenApiSurface,
    /** The unit whose files the plan prints. */
    private val unit: ClientUnit,
) {
    private val schemas = surfaceModel.schemas.associateBy { it.name }

    val types =
        OpenApiTypes(
            namespace,
            schemas,
            unit,
            surface,
            ClientPrinter.packageOf(unit),
            core =
                if (unit == ClientUnit.Core) {
                    null
                } else {
                    OpenApiTypes("${ClientPrinter.PACKAGE}.RestSchema", schemas, ClientUnit.Core, surface)
                },
        )

    /**
     * The surface as this unit prints it: its own operations and every component, and the routes a renderer fetches
     * on its own where it is the core, whose they are.
     */
    private val model =
        OpenApiSurface(
            surfaceModel.origin,
            surfaceModel.operations.filter { it.unit == unit },
            if (unit == ClientUnit.Core) surfaceModel.templates else emptyList(),
            surfaceModel.schemas,
            surfaceModel.webhooks,
        )

    private val printed = model.schemas.filter { it.role != "envelope" && it.unit == unit }

    /** The union each member component extends. */
    private val unions: Map<String, String> =
        buildMap {
            for (union in printed.filterIsInstance<UnionSchema>()) {
                for (member in union.members) {
                    val schema = types.schema(RefType(member.type, false))
                    if (schema !is ObjectSchema) {
                        throw ModelException(
                            "$namespace.${union.name}: its member ${member.type} is not an object",
                        )
                    }
                    // A member extends its union's class, and a class extends none another module declares.
                    if (schema.unit != unit) {
                        throw ModelException(
                            "$namespace.${union.name}: its member ${member.type} is the ${schema.unit.wire} unit's, " +
                                "and a member extends its union in the module the union is printed in",
                        )
                    }
                    val earlier = put(member.type, union.name)
                    if (earlier != null) {
                        throw ModelException(
                            "$namespace.${member.type}: a member of both $earlier and ${union.name}, which no Kotlin class can extend",
                        )
                    }
                }
            }
        }

    /** The inline schemas the surface names from where they sit, in the order they are first met. */
    private val inline = linkedMapOf<String, TypeNode>()

    /**
     * The inline objects a request sends as JSON, where they were first met, whose optional
     * nullable properties have three states. A form never does: a part is sent or it is not.
     */
    private val sentByRequest = mutableSetOf<String>()

    init {
        for (schema in printed) {
            val request = "request" in schema.usage
            if (schema is ObjectSchema) schema.properties.forEach { visit(it.type, request) }
            if (schema is AliasSchema) visit(schema.type, request)
        }
        for (operation in model.operations) {
            for (parameter in operation.pathParameters + operation.queryParameters + operation.headerParameters) {
                visit(parameter.type, request = true)
            }
            operation.requestBody?.content?.forEach {
                visit(it.type, request = true, form = it.mediaType == "multipart/form-data")
            }
            operation.success.data?.let { visit(it.type, request = false) }
            operation.success.metadata?.let { visit(it.type, request = false) }
            operation.success.event?.let { visit(it, request = false) }
        }
        // Read, held to the surface's components, and printed nowhere: the headers a success
        // declares, what an error status adds, and the documents a template's route answers. An
        // inline type among them is checked, never declared.
        for (operation in model.operations + model.templates) {
            val site = "$namespace.${operation.operationId}"
            operation.success.responses
                .flatMap { it.headers }
                .forEach { resolves(it.type, site) }
            operation.errors.mapNotNull { it.metadata }.forEach { resolves(it.type, site) }
            operation.success.documents.forEach { resolves(it.type, site) }
        }
        val names =
            printed.map { it.name } + inline.keys + model.operations.map(OperationPrinter::parametersClass) +
                model.operations.mapNotNull(OperationPrinter::bodyClass)
        val duplicates = names.groupBy { it }.filterValues { it.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw ModelException(
                "$namespace: ${duplicates.joinToString()} would be declared twice, and the printer renames nothing",
            )
        }
    }

    private fun visit(
        type: TypeNode,
        request: Boolean,
        form: Boolean = false,
    ) {
        when (type) {
            is InlineObject -> {
                if (inline.putIfAbsent(type.name, type) == null && request && !form) sentByRequest += type.name
                type.properties.forEach { visit(it.type, request) }
                type.additional?.let { visit(it, request) }
            }

            is InlineUnion -> {
                inline.putIfAbsent(type.name, type)
                type.members.forEach { visit(it, request) }
            }

            is ScalarType -> {
                if (type.enumName != null) inline.putIfAbsent(type.enumName, type)
            }

            is ArrayType -> {
                visit(type.items, request)
            }

            is MapType -> {
                type.keys?.let { visit(it, request) }
                visit(type.values, request)
            }

            is RefType -> {
                types.schema(type)
            }

            is AnyType, is NullType -> {}
        }
    }

    /** Refuses [type] where it names a component the surface does not declare, anywhere inside it. */
    private fun resolves(
        type: TypeNode,
        site: String,
    ) {
        when (type) {
            is RefType -> {
                if (!types.declares(type.name)) throw ModelException("$site: `${type.name}` names no component")
            }

            is ArrayType -> {
                resolves(type.items, site)
            }

            is MapType -> {
                (listOfNotNull(type.keys) + type.values).forEach { resolves(it, site) }
            }

            is InlineObject -> {
                (type.properties.map { it.type } + listOfNotNull(type.additional)).forEach { resolves(it, site) }
            }

            is InlineUnion -> {
                type.members.forEach { resolves(it, site) }
            }

            is ScalarType, is AnyType, is NullType -> {}
        }
    }

    /** Prints the namespace object. */
    fun printNamespace(): String =
        buildString {
            kdoc("", listOf(about()))
            append("public object ").append(namespace).append(" {\n")
            val declarations = mutableListOf<StringBuilder.() -> Unit>()
            for (schema in printed) declarations += { printSchema(schema) }
            for ((name, type) in inline) declarations += { printInline(name, type) }
            for (operation in model.operations) {
                if (OperationPrinter.bodyClass(operation) != null) {
                    declarations += { printBody(operation, types, "    ") }
                }
                declarations += { printParameters(operation, types, "    ") }
            }
            declarations.forEachIndexed { index, declaration ->
                if (index > 0) append('\n')
                declaration()
            }
            append("}\n")
        }

    /** What the namespace object's documentation says it holds. */
    private fun about(): String =
        when {
            surface != "Rest" -> {
                "$namespace is the types of $title, as the contract names them."
            }

            unit == ClientUnit.Core -> {
                KotlinText.wrapped(
                    "$namespace is the types of $title, as the contract names them, but those only its admin " +
                        "unit reaches, which `mountaya-admin` prints into `${ClientPrinter.ADMIN_NAMESPACE}`.",
                    100 - 3,
                )
            }

            else -> {
                KotlinText.wrapped(
                    "$namespace is the types of $title only its admin unit reaches, through its `admin` " +
                        "operations and its webhook events, as the contract names them. A type the core holds too is " +
                        "`RestSchema`'s, in `mountaya-client`.",
                    100 - 3,
                )
            }
        }

    private fun StringBuilder.printSchema(schema: Schema) {
        val indent = "    "
        when (schema) {
            is ObjectSchema -> {
                printClass(
                    ClassSpecs.openApi(
                        schema.name,
                        schema.description,
                        schema.properties,
                        "request" in schema.usage,
                        types,
                        unions[schema.name],
                    ),
                    indent,
                )
            }

            is EnumSchema -> {
                printEnum(
                    namespace,
                    schema.name,
                    schema.description,
                    enumWireType(schema),
                    schema.members.map {
                        SchemaPrinter.EnumConstant(
                            EnumMembers.openApi(schema.name, it.name),
                            it.name,
                            KotlinText.value(it.value),
                            memberDoc(it),
                            it.deprecated,
                        )
                    },
                    indent,
                )
            }

            is UnionSchema -> {
                printUnion(schema, types, indent)
            }

            is AliasSchema -> {
                printAlias(schema.name, schema.description, types.kotlin(schema.type), indent)
            }

            is GeoJsonSchema -> {
                printAlias(schema.name, schema.description, SurfaceTypes.geoJson(schema.geometry), indent)
            }
        }
    }

    /** A member's description, then what it includes and what a key carrying it needs beside it. */
    private fun memberDoc(member: EnumMember): String? {
        val includes =
            member.includes.takeIf { it.isNotEmpty() }?.let {
                val reach = if (it.size == 1) "that scope reaches" else "those scopes reach"
                "It includes ${listed(it)}: a key carrying it reaches what $reach."
            }
        val needs =
            member.needs.takeIf { it.isNotEmpty() }?.let {
                "A key carrying it carries ${listed(
                    it,
                )} beside it."
            }
        return listOfNotNull(member.description, includes, needs).joinToString("\n\n").ifEmpty { null }
    }

    /** Wire values as prose: `a`, `a` and `b`, or `a`, `b` and `c`. */
    private fun listed(values: List<String>): String {
        val quoted = values.map { "`$it`" }
        return if (quoted.size ==
            1
        ) {
            quoted.single()
        } else {
            quoted.dropLast(1).joinToString(", ") + " and " + quoted.last()
        }
    }

    private fun enumWireType(schema: EnumSchema): String =
        when (schema.valueType) {
            "string" -> "kotlin.String"

            "integer" -> if (schema.format == "int64") "kotlin.Long" else "kotlin.Int"

            else -> throw ModelException(
                "$namespace.${schema.name}: an enum of `${schema.valueType}`, which this printer does not know",
            )
        }

    private fun StringBuilder.printInline(
        name: String,
        type: TypeNode,
    ) {
        val indent = "    "
        when (type) {
            is InlineObject -> {
                printClass(ClassSpecs.openApi(name, null, type.properties, name in sentByRequest, types, null), indent)
            }

            is InlineUnion -> {
                printInlineUnion(type, types, indent)
            }

            is ScalarType -> {
                // A set the contract writes inline is printed under the words its `x-enum-varnames` gives each
                // member, as a set it names is (`sdk/SPEC.md` section 9); one that gives none prints each member
                // under its wire value, between backticks where it must be.
                val wire = if (type.kind == "string") "kotlin.String" else "kotlin.Int"
                val values = type.enumValues.orEmpty()
                val names = type.enumNames
                if (names != null && names.size != values.size) {
                    throw ModelException(
                        "$namespace.$name: ${names.size} member names for ${values.size} values, so no value can be " +
                            "matched to its name",
                    )
                }
                printEnum(
                    namespace,
                    name,
                    null,
                    wire,
                    values.mapIndexed { index, value ->
                        val constant = value.toString()
                        val named = names?.get(index)
                        if (named == null) {
                            KotlinText.identifier(constant)
                            SchemaPrinter.EnumConstant(constant, constant, KotlinText.value(value), null, false)
                        } else {
                            SchemaPrinter.EnumConstant(
                                EnumMembers.openApi(name, named),
                                named,
                                KotlinText.value(value),
                                "Sent as `$constant`.",
                                false,
                            )
                        }
                    },
                    indent,
                )
            }

            else -> {
                // Nothing to declare.
            }
        }
    }
}
