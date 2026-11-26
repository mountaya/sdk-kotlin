package com.mountaya.build.generator

import groovy.json.JsonSlurper

/** ModelException is a model this printer cannot print, and why. Nothing is ever skipped in its place. */
class ModelException(
    message: String,
) : RuntimeException(message)

/**
 * ModelReader reads `sdk/model.json` into [SdkModel].
 *
 * It refuses a `modelVersion` other than [SdkModel.VERSION] and a `kind` it does not know, and
 * names where it found one; it ignores a key it does not know, which is what the model's own
 * reading rules ask of a reader. A key it knows is held to what the format says of it, so a
 * value the printer would read past is refused where it is read, printed or not.
 */
object ModelReader {
    /** The headers the client sends itself, which a caller's own headers never name. */
    private val CLIENT_HEADERS = setOf("accept-language", "idempotency-key", "if-match", "last-event-id")

    /** What `x-acting-user` may say. */
    private val ACTING_USERS = setOf("required", "optional", "none")

    /** Reads [text], the model's JSON, refusing a version, a `kind` or a shape this reader was not written for. */
    fun read(text: String): SdkModel {
        val root =
            JsonSlurper().parseText(text) as? Map<*, *> ?: throw ModelException("sdk/model.json: not a JSON object")
        val node = Node(root, "sdk/model.json")
        val version = node.int("modelVersion")
        if (version != SdkModel.VERSION) {
            throw ModelException("sdk/model.json: modelVersion $version, and this printer reads ${SdkModel.VERSION}.")
        }
        return SdkModel(
            contract = node.string("contract"),
            rest = surface(node.node("rest"), statesUnits = true),
            tiles = surface(node.node("tiles"), statesUnits = false),
            data = data(node.node("data")),
        )
    }

    /**
     * The unit [node] states, which every operation, webhook event and component of the REST API states, the model
     * splitting the client by that key alone; a surface that may leave it unsaid, the Tile API, is the core's where it
     * does. A unit this printer does not know is refused where it is read.
     */
    private fun unit(
        node: Node,
        statesUnits: Boolean,
    ): ClientUnit {
        val stated = if (statesUnits) node.string("unit") else node.optionalString("unit") ?: return ClientUnit.Core
        return ClientUnit.of(stated)
            ?: throw ModelException("${node.where}.unit: `$stated`, which this printer does not know")
    }

    private fun surface(
        node: Node,
        statesUnits: Boolean,
    ): OpenApiSurface {
        val templates = node.nodes("templates").map { operation(it, statesUnits = false) }
        // A template is what a map renderer fetches on its own, so it is always a file.
        for (template in templates) {
            if (template.success.kind != "binary") {
                throw ModelException("${node.where}.templates (${template.operationId}): a template that is not binary")
            }
        }
        return OpenApiSurface(
            origin = node.string("origin"),
            operations = node.nodes("operations").map { operation(it, statesUnits) },
            templates = templates,
            schemas = node.nodes("schemas").map { schema(it, statesUnits) },
            webhooks = node.optionalNodes("webhooks").map { webhook(it, statesUnits) },
        )
    }

    private fun webhook(
        node: Node,
        statesUnits: Boolean,
    ): Webhook =
        Webhook(
            type = node.string("type"),
            unit = unit(node, statesUnits),
            envelope = node.string("envelope"),
            objectName = node.string("object"),
            previousAttributes =
                if (node.has(
                        "previousAttributes",
                    )
                ) {
                    node.strings("previousAttributes")
                } else {
                    emptyList()
                },
            summary = node.text("summary"),
            description = node.text("description"),
        )

    private fun schema(
        node: Node,
        statesUnits: Boolean,
    ): Schema {
        val name = node.string("name")
        val usage = node.strings("usage").toSet()
        val description = node.text("description")
        val role = node.optionalString("role")
        val unit = unit(node, statesUnits)
        return when (val kind = node.string("kind")) {
            "object" -> {
                objectSchema(node, name, usage, description, role, unit)
            }

            "enum" -> {
                EnumSchema(
                    name,
                    usage,
                    description,
                    role,
                    unit,
                    valueType = node.string("valueType"),
                    format = node.optionalString("format"),
                    members =
                        node.nodes("members").map {
                            EnumMember(
                                it.value("value"),
                                it.string("name"),
                                it.text("description"),
                                it.flag("deprecated"),
                                includes = if (it.has("includes")) it.strings("includes") else emptyList(),
                                needs = if (it.has("needs")) it.strings("needs") else emptyList(),
                            )
                        },
                )
            }

            "union" -> {
                UnionSchema(
                    name,
                    usage,
                    description,
                    role,
                    unit,
                    discriminator = node.string("discriminator"),
                    members = node.nodes("members").map { UnionMember(it.string("value"), it.string("type")) },
                )
            }

            "alias" -> {
                AliasSchema(name, usage, description, role, unit, type(node.node("type")))
            }

            "geojson" -> {
                GeoJsonSchema(name, usage, description, role, unit, node.string("geometry"))
            }

            else -> {
                throw ModelException("${node.where}: a schema of kind `$kind`, which this printer does not know")
            }
        }
    }

    /**
     * An object component. A property inherited from a parent names one the component extends, a
     * component that refuses unknown keys allows none, and a property the server alone writes is
     * on no component a request sends, as one a request alone sends is on none a response carries.
     */
    private fun objectSchema(
        node: Node,
        name: String,
        usage: Set<String>,
        description: String?,
        role: String?,
        unit: ClientUnit,
    ): ObjectSchema {
        val properties = node.nodes("properties").map(::property)
        val extends = if (node.has("extends")) node.strings("extends") else emptyList()
        val closed = node.flag("closed")
        if (closed && node.has("additional")) {
            throw ModelException("${node.where}: a component both closed and open to other keys")
        }
        for (property in properties) {
            val parent = property.from
            if (parent != null && parent !in extends) {
                throw ModelException(
                    "${node.where}.${property.name}: inherited from `$parent`, which it does not extend",
                )
            }
            if (property.readOnly && "request" in usage) {
                throw ModelException(
                    "${node.where}.${property.name}: written by the server alone, on a component a request sends",
                )
            }
            if (property.writeOnly && "response" in usage) {
                throw ModelException(
                    "${node.where}.${property.name}: sent by a request alone, on a component a response carries",
                )
            }
        }
        return ObjectSchema(name, usage, description, role, unit, properties, closed, extends)
    }

    private fun property(node: Node): Property {
        val readOnly = node.flag("readOnly")
        val writeOnly = node.flag("writeOnly")
        if (readOnly && writeOnly) throw ModelException("${node.where}: a property both read-only and write-only")
        return Property(
            name = node.string("name"),
            required = node.boolean("required"),
            type = type(node.node("type")),
            description = node.text("description"),
            default = node.optionalValue("default"),
            hasDefault = node.has("default"),
            discriminatorValue = node.optionalString("discriminatorValue"),
            deprecated = node.flag("deprecated"),
            from = node.optionalString("from"),
            readOnly = readOnly,
            writeOnly = writeOnly,
            secret = node.flag("secret"),
        )
    }

    private fun type(node: Node): TypeNode {
        val nullable = node.flag("nullable")
        return when (val kind = node.string("kind")) {
            "string", "integer", "number", "boolean" -> {
                ScalarType(
                    kind = kind,
                    format = node.optionalString("format"),
                    constant = node.optionalValue("const"),
                    enumName = if (node.has("enum")) node.string("name") else null,
                    enumValues = if (node.has("enum")) node.values("enum") else null,
                    nullable = nullable,
                    enumNames = if (node.has("enumNames")) node.strings("enumNames") else null,
                )
            }

            "array" -> {
                ArrayType(type(node.node("items")), nullable)
            }

            "map" -> {
                MapType(type(node.node("values")), node.optionalNode("keys")?.let(::type), nullable)
            }

            "object" -> {
                val closed = node.flag("closed")
                if (closed && node.has("additional")) {
                    throw ModelException("${node.where}: an object both closed and open to other keys")
                }
                InlineObject(
                    name = node.string("name"),
                    properties = node.nodes("properties").map(::property),
                    additional = node.optionalNode("additional")?.let(::type),
                    closed = closed,
                    nullable = nullable,
                )
            }

            "union" -> {
                InlineUnion(node.string("name"), node.nodes("members").map(::type), nullable)
            }

            "ref" -> {
                RefType(node.string("name"), nullable)
            }

            "any" -> {
                AnyType(nullable)
            }

            "null" -> {
                NullType(nullable)
            }

            else -> {
                throw ModelException("${node.where}: a type of kind `$kind`, which this printer does not know")
            }
        }
    }

    /** What an operation declares of the credentials it takes, a REST, Tile or Data API one alike. */
    private fun credentials(credentials: Node): Credentials {
        val actingUser = credentials.string("actingUser")
        if (actingUser !in ACTING_USERS) {
            throw ModelException("${credentials.where}.actingUser: `$actingUser`, which this printer does not know")
        }
        return Credentials(
            actingUser = actingUser,
            acceptsShareToken = credentials.boolean("acceptsShareToken"),
            requiresSession = credentials.boolean("requiresSession"),
            requiresSecretKey = credentials.boolean("requiresSecretKey"),
            takesNoCredential = credentials.boolean("takesNoCredential"),
            pendingInvitee = credentials.boolean("pendingInvitee"),
        )
    }

    /** Every status a success arrives with and the headers each declares, refusing a success that names none. */
    private fun responses(node: Node): List<SuccessResponse> {
        val responses =
            node.nodes("responses").map {
                SuccessResponse(it.int("status"), it.optionalNodes("headers").map(::parameter))
            }
        if (responses.isEmpty()) throw ModelException("${node.where}: a success that names no status")
        return responses
    }

    private fun operation(
        node: Node,
        statesUnits: Boolean,
    ): Operation {
        val credentials = credentials(node.node("credentials"))
        val queryParameters = node.optionalNodes("queryParameters").map(::parameter)
        val headerParameters = node.optionalNodes("headerParameters").map(::parameter)
        for (header in headerParameters) {
            if (header.name.lowercase() in CLIENT_HEADERS) {
                throw ModelException(
                    "${node.where}: `${header.name}` among the caller's headers, where the client sends it",
                )
            }
        }
        val pagination =
            node.optionalNode("pagination")?.let { paged ->
                for (name in listOf("cursor", "limit")) {
                    if (queryParameters.none { it.name == name }) {
                        throw ModelException("${node.where}: it pages by cursor and takes no `$name`")
                    }
                }
                Pagination(paged.strings("sort"), paged.optionalString("defaultSort"))
            }
        return Operation(
            operationId = node.string("operationId"),
            method = node.string("method"),
            path = node.string("path"),
            namespace = node.optionalString("namespace"),
            unit = unit(node, statesUnits),
            idempotent = node.boolean("idempotent"),
            timeoutMs = node.optionalInt("timeoutMs"),
            idempotencyKey = node.boolean("idempotencyKey"),
            conditional = node.boolean("conditional"),
            notModified = node.flag("notModified"),
            pagination = pagination,
            credentials = credentials,
            pathParameters = node.optionalNodes("pathParameters").map(::parameter),
            queryParameters = queryParameters,
            headerParameters = headerParameters,
            requestBody =
                node.optionalNode("requestBody")?.let { body ->
                    RequestBody(
                        body.boolean("required"),
                        body.nodes("content").map { content ->
                            MediaContent(
                                content.string("mediaType"),
                                type(content.node("type")),
                                content.optionalNodes("encoding").map {
                                    PartEncoding(it.string("property"), it.string("contentType"))
                                },
                            )
                        },
                    )
                },
            success = success(node.node("success")),
            errors =
                node.optionalNodes("errors").map {
                    ErrorResponse(it.int("status"), it.optionalNode("metadata")?.let(::payload))
                },
            summary = node.text("summary"),
            description = node.text("description"),
            deprecated = node.flag("deprecated"),
            tasks = if (node.has("tasks")) node.strings("tasks") else emptyList(),
        )
    }

    private fun parameter(node: Node): Parameter =
        Parameter(
            name = node.string("name"),
            // A path parameter is required by OpenAPI's own rule, and the model may leave it unsaid.
            required = node.optionalBoolean("required") ?: true,
            type = type(node.node("type")),
            description = node.text("description"),
            default = node.optionalValue("default"),
            hasDefault = node.has("default"),
            deprecated = node.flag("deprecated"),
        )

    private fun success(node: Node): Success {
        val kind = node.string("kind")
        if (kind !in setOf("envelope", "event-stream", "binary")) {
            throw ModelException("${node.where}: a success of kind `$kind`, which this printer does not know")
        }
        val responses = responses(node)
        return Success(
            kind = kind,
            data = node.optionalNode("data")?.let(::payload),
            metadata = node.optionalNode("metadata")?.let(::payload),
            event = node.optionalNode("event")?.let(::type),
            mediaTypes = if (node.has("mediaTypes")) node.strings("mediaTypes") else emptyList(),
            responses = responses,
            documents =
                node
                    .optionalNodes(
                        "documents",
                    ).map { MediaContent(it.string("mediaType"), type(it.node("type"))) },
        )
    }

    private fun payload(node: Node): Payload =
        Payload(node.optionalBoolean("required") ?: true, type(node.node("type")))

    private fun data(node: Node): DataSurface =
        DataSurface(
            origin = node.string("origin"),
            endpoint = node.string("endpoint"),
            operations =
                node.nodes("operations").map {
                    DataOperationModel(
                        name = it.string("name"),
                        input = it.string("input"),
                        inputType = it.string("inputType"),
                        resultType = it.string("resultType"),
                        scopes = it.strings("scopes"),
                        timeoutMs = it.int("timeoutMs"),
                        credentials = credentials(it.node("credentials")),
                        responses = responses(it.node("success")),
                        selection = it.nodes("selection").map(::selection),
                        document = it.string("document"),
                        description = it.text("description"),
                        deprecated = it.flag("deprecated"),
                    )
                },
            objects = node.nodes("objects").map(::dataType),
            inputs = node.nodes("inputs").map(::dataType),
            enums =
                node.nodes("enums").map { enum ->
                    DataEnum(
                        enum.string("name"),
                        enum.text("description"),
                        enum.nodes("members").map {
                            DataEnumMember(it.string("name"), it.text("description"), it.flag("deprecated"))
                        },
                    )
                },
            scalars = node.nodes("scalars").map(::scalar),
        )

    private fun selection(node: Node): SelectionNode =
        SelectionNode(node.string("name"), if (node.has("fields")) node.nodes("fields").map(::selection) else null)

    private fun dataType(node: Node): DataType =
        DataType(
            name = node.string("name"),
            description = node.text("description"),
            fields =
                node.nodes("fields").map {
                    DataField(
                        name = it.string("name"),
                        required = it.optionalBoolean("required") ?: false,
                        type = type(it.node("type")),
                        description = it.text("description"),
                        default = it.optionalValue("default"),
                        hasDefault = it.has("default"),
                        deprecated = it.flag("deprecated"),
                    )
                },
        )

    /**
     * A scalar of the SDL, mapped by the wire it travels in and never by its name: one of GraphQL's
     * own is built in and has no wire, and a declared one has a wire and is not built in. A string
     * wire is a timestamp, a URL or a plain string; a GeoJSON wire names its geometry.
     */
    private fun scalar(node: Node): DataScalar {
        val name = node.string("name")
        val builtin = node.flag("builtin")
        val wire = node.optionalNode("wire")
        if (builtin == (wire != null)) {
            throw ModelException("${node.where}: a scalar is built in or travels in a wire, one of the two")
        }
        if (wire == null) return DataScalar(name, builtin, null, null)
        return when (val kind = wire.string("kind")) {
            "geojson" -> {
                DataScalar(name, builtin, null, wire.string("geometry"))
            }

            "string" -> {
                val format = wire.optionalString("format")
                if (format != null && format != "date-time" && format != "uri") {
                    throw ModelException(
                        "${wire.where}: a string wire of format `$format`, which this printer does not know",
                    )
                }
                DataScalar(name, builtin, ScalarType("string", format, null, null, null, false), null)
            }

            else -> {
                throw ModelException("${wire.where}: a scalar's wire of kind `$kind`, which this printer does not know")
            }
        }
    }

    /** One JSON object of the model, and the path that names it in an error. */
    private class Node(
        private val map: Map<*, *>,
        val where: String,
    ) {
        fun has(key: String): Boolean = map.containsKey(key)

        fun node(key: String): Node = optionalNode(key) ?: missing(key)

        fun optionalNode(key: String): Node? =
            map[key]?.let { value ->
                val nested = value as? Map<*, *> ?: wrong(key, "an object")
                Node(nested, "$where.$key")
            }

        fun nodes(key: String): List<Node> = if (has(key)) optionalNodes(key) else missing(key)

        fun optionalNodes(key: String): List<Node> {
            val list = map[key] ?: return emptyList()
            val items = list as? List<*> ?: wrong(key, "an array")
            return items.mapIndexed { index, item ->
                val nested = item as? Map<*, *> ?: wrong("$key[$index]", "an object")
                Node(nested, "$where.$key[$index]${(nested["name"] ?: nested["operationId"])?.let { " ($it)" } ?: ""}")
            }
        }

        fun string(key: String): String = optionalString(key) ?: missing(key)

        fun optionalString(key: String): String? = map[key]?.let { it as? String ?: wrong(key, "a string") }

        /** Documentation, which decides no shape, and is a string where the model writes it. */
        fun text(key: String): String? = optionalString(key)

        fun strings(key: String): List<String> = values(key).map { it as? String ?: wrong(key, "an array of strings") }

        fun values(key: String): List<Any> {
            val list = (map[key] ?: missing(key)) as? List<*> ?: wrong(key, "an array")
            return list.map { it ?: wrong(key, "an array without null") }
        }

        fun int(key: String): Int = optionalInt(key) ?: missing(key)

        fun optionalInt(key: String): Int? = map[key]?.let { it as? Int ?: wrong(key, "an integer") }

        fun boolean(key: String): Boolean = optionalBoolean(key) ?: missing(key)

        fun optionalBoolean(key: String): Boolean? = map[key]?.let { it as? Boolean ?: wrong(key, "a boolean") }

        /** A flag the model writes only where it is true. */
        fun flag(key: String): Boolean = optionalBoolean(key) ?: false

        fun value(key: String): Any = map[key] ?: missing(key)

        fun optionalValue(key: String): Any? = map[key]

        private fun missing(key: String): Nothing = throw ModelException("$where: `$key` is missing")

        private fun wrong(
            key: String,
            expected: String,
        ): Nothing = throw ModelException("$where: `$key` is not $expected")
    }
}
