package com.mountaya.build.generator

import com.mountaya.build.generator.KotlinText.kdoc
import com.mountaya.build.generator.SchemaPrinter.deprecation
import com.mountaya.build.generator.SchemaPrinter.writeOnly

/**
 * Prints the methods a caller awaits, the optional-parameter builders they take, and the table
 * of what each operation declares.
 *
 * A method settles what its signature decides, the path, the query, the headers and the body,
 * and hands the rest to the transport through `OperationSender`, the one seam between the
 * generated surface and the network.
 */
internal object OperationPrinter {
    /** The package of every hand-written type a printed method names: the client's one (SPEC.md section 6). */
    private const val CLIENT = "com.mountaya.client"
    private const val SENDER = "$CLIENT.OperationSender"
    private const val OPTIONS = "$CLIENT.RequestOptions"
    private const val WIRE_JSON = "$CLIENT.wireJson"
    private const val ENCODE_SEGMENT = "$CLIENT.encodePathSegment"
    private const val PAGED_PARAMETERS = "$CLIENT.PagedParameters"

    /**
     * The marker of what one of Mountaya's modules reads of another's and no consumer does (SPEC.md section 7), which
     * a paged builder's cursor and limit carry, as `PagedParameters` declares them: the walk's alone, and no part of
     * the published API.
     */
    const val INTERNAL_API = "$CLIENT.InternalMountayaApi"
    private const val PAGE_WALK = "$CLIENT.PageWalk"
    private const val FLOW = "kotlinx.coroutines.flow.Flow"

    /** The component whose `pagination.next_cursor` a walk reads the next page from, which `PageWalk` names. */
    private const val LIST_METADATA = "ListMetadata"

    /** The names a printed method gives its own parameters and locals, which no contract parameter may take. */
    private val RESERVED = setOf("body", "options", "parameters", "given", "query", "headers", "input", "select")

    /** The names a walk gives its own parameters beside its method's, which no parameter of a paged operation may take. */
    private val WALK_RESERVED = setOf("pageSize", "maxPages", "paged")

    /** The walk printed beside a paged operation's method: `walk` before its operation's name. */
    fun walkName(operation: Operation): String = "walk" + KotlinText.capitalized(operation.operationId)

    /** How wide a walk's own prose runs, so its lines, in a resource's accessor, stay within 100 columns. */
    private const val WALK_DOC_WIDTH = 100 - 8 - 3

    /** The class an operation's optional parameters are set in, in its surface's namespace. */
    fun parametersClass(operation: Operation): String = KotlinText.capitalized(operation.operationId) + "Parameters"

    /** The builder's storage of the `Idempotency-Key` a caller sets, where the operation takes one. */
    private const val KEY_SLOT = "idempotencyKeySlot"

    /** The builder's storage of the `If-Match` a caller sets, where the operation takes one. */
    private const val TAG_SLOT = "ifMatchSlot"

    /** Whether [operation]'s builder holds anything a printed method reads back. */
    private fun readsBuilder(operation: Operation): Boolean =
        optionalParameters(operation).isNotEmpty() || operation.idempotencyKey || operation.conditional

    /** The optional parameters of [operation]: its optional query parameters, then its optional headers. */
    private fun optionalParameters(operation: Operation): List<Parameter> =
        (operation.queryParameters + operation.headerParameters).filterNot { it.required }

    /** Prints the builder of [operation]'s optional parameters. */
    fun StringBuilder.printParameters(
        operation: Operation,
        types: OpenApiTypes,
        indent: String,
    ) {
        val name = parametersClass(operation)
        val optional = optionalParameters(operation)
        // The key and the tag ride the builder under their own names, which a query parameter
        // would shadow.
        val own =
            listOfNotNull(
                "idempotencyKey".takeIf { operation.idempotencyKey },
                "ifMatch".takeIf { operation.conditional },
            )
        val shared = optional.map { KotlinText.identifier(it.name) }.filter { it in own }
        if (shared.isNotEmpty()) {
            throw ModelException(
                "${operation.operationId}: a parameter named ${shared.joinToString()}, which the call's own builder property takes",
            )
        }
        kdoc(
            indent,
            listOf(
                "$name sets the optional parameters of `${operation.operationId}`, each by " +
                    "assigning it. Every operation " +
                    "takes one, even with nothing in it, so a parameter the contract adds changes no call site.",
            ),
        )
        // A paged operation's builder is the one a walk reads its cursor back through and sets its
        // cursor and limit on, so it extends the class the walk names.
        val paged =
            operation.pagination?.let {
                val cursor = optional.indexOfFirst { it.name == "cursor" }
                val limit = optional.indexOfFirst { it.name == "limit" }
                if (cursor < 0 || limit < 0) {
                    throw ModelException(
                        "${operation.operationId}: it pages by cursor, and its `cursor` or its `limit` is required",
                    )
                }
                if (types.base(optional[cursor].type) != "kotlin.String" ||
                    types.base(optional[limit].type) != "kotlin.Int"
                ) {
                    throw ModelException(
                        "${operation.operationId}: it pages by cursor, and its `cursor` is no string or its `limit` no integer",
                    )
                }
                cursor to limit
            }
        append(indent).append("public class ").append(name).append(" internal constructor()")
        if (paged != null) append(" : ").append(PAGED_PARAMETERS).append("()")
        append(" {\n")
        val inner = "$indent    "
        optional.forEachIndexed { index, parameter ->
            append(inner).append("internal var slot").append(index).append(": ")
            append(types.base(parameter.type)).append("? = null\n")
        }
        if (operation.idempotencyKey) append(inner).append("internal var $KEY_SLOT: kotlin.String? = null\n")
        if (operation.conditional) append(inner).append("internal var $TAG_SLOT: kotlin.String? = null\n")
        if (paged != null) {
            for ((property, slot, type) in listOf(
                Triple("pageCursor", paged.first, "kotlin.String"),
                Triple("pageLimit", paged.second, "kotlin.Int"),
            )) {
                append('\n')
                append(inner).append("@").append(INTERNAL_API).append('\n')
                append(inner)
                    .append("override var ")
                    .append(property)
                    .append(": ")
                    .append(type)
                    .append("?\n")
                append(inner).append("    get() = slot").append(slot).append('\n')
                append(inner).append("    set(value) {\n")
                append(inner).append("        slot").append(slot).append(" = value\n")
                append(inner).append("    }\n")
            }
        }
        optional.forEachIndexed { index, parameter ->
            append('\n')
            kdoc(inner, parameterDoc(parameter))
            deprecation(inner, parameter.deprecated, parameter.description)
            writeOnly(inner, KotlinText.identifier(parameter.name), types.kotlin(parameter.type), "slot$index", "value")
        }
        if (operation.idempotencyKey) {
            append('\n')
            kdoc(
                inner,
                listOf(
                    "The `Idempotency-Key` every attempt of the call sends: the caller's, which makes " +
                        "two of its calls one write, or, left unset, one the client draws once.",
                ),
            )
            writeOnly(inner, "idempotencyKey", "kotlin.String", KEY_SLOT, "value")
        }
        if (operation.conditional) {
            append('\n')
            kdoc(
                inner,
                listOf(
                    "The `etag` the caller read the resource under, or `\"*\"`, sent as `If-Match` exactly as given, " +
                        "and nothing is sent where it is left unset.",
                ),
            )
            writeOnly(inner, "ifMatch", "kotlin.String", TAG_SLOT, "value")
        }
        append(indent).append("}\n")
    }

    private fun parameterDoc(parameter: Parameter): List<String?> =
        listOf(
            parameter.description,
            if (parameter.hasDefault) {
                "Left out, the server applies its default: `${KotlinText.shown(parameter.default)}`."
            } else {
                null
            },
        )

    /** The path parameters of [operation], in the order its path names them. */
    private fun pathParameters(operation: Operation): List<Parameter> {
        val named = Regex("\\{([^}]+)}").findAll(operation.path).map { it.groupValues[1] }.toList()
        val declared = operation.pathParameters.associateBy { it.name }
        if (named.toSet() != declared.keys || named.size != declared.size) {
            throw ModelException(
                "${operation.operationId}: its path names ${named.joinToString()}, and it declares ${declared.keys.joinToString()}",
            )
        }
        return named.map { declared.getValue(it) }
    }

    /**
     * The class [operation]'s body is passed as where the contract accepts it in more than one media
     * type, one subclass per type, or null where it accepts one or none.
     */
    fun bodyClass(operation: Operation): String? =
        operation.requestBody
            ?.content
            ?.takeIf { it.size > 1 }
            ?.let { KotlinText.capitalized(operation.operationId) + "Body" }

    /** The subclass of a body class that carries a body sent as [content]'s media type. */
    private fun mediaCase(
        operation: Operation,
        content: MediaContent,
    ): String =
        when (content.mediaType) {
            "application/json" -> "Json"
            "multipart/form-data" -> "Multipart"
            else -> throw ModelException("${operation.operationId}: no encoding for a `${content.mediaType}` body")
        }

    /** Prints the class [operation]'s body is passed as, where it is accepted in more than one media type. */
    fun StringBuilder.printBody(
        operation: Operation,
        types: OpenApiTypes,
        indent: String,
    ) {
        val name = bodyClass(operation) ?: return
        val qualified = "${types.namespace}.$name"
        val inner = "$indent    "
        kdoc(
            indent,
            listOf(
                "$name is the body of `${operation.operationId}`, which the contract accepts in more than one " +
                    "media type: one subclass per media type, each carrying the body sent as that type.",
            ),
        )
        append(indent).append("public abstract class ").append(name).append(" internal constructor() {\n")
        operation.requestBody?.content.orEmpty().forEachIndexed { index, content ->
            if (index > 0) append('\n')
            val case = mediaCase(operation, content)
            kdoc(inner, listOf("$case is the body sent as `${content.mediaType}`."))
            append(inner).append("public class ").append(case).append("(\n")
            append(inner).append("    /** The body. */\n")
            append(inner).append("    public val value: ").append(types.kotlin(content.type)).append(",\n")
            append(inner).append(") : ").append(qualified).append("() {\n")
            append(inner).append("    override fun equals(other: kotlin.Any?): kotlin.Boolean = other is ")
            append(qualified).append('.').append(case).append(" && other.value == this.value\n\n")
            append(inner).append("    override fun hashCode(): kotlin.Int = this.value.hashCode()\n\n")
            append(inner).append("    override fun toString(): kotlin.String = \"$case(\" + this.value + \")\"\n")
            append(inner).append("}\n")
        }
        append(indent).append("}\n")
    }

    /** Prints [operation]'s method. */
    fun StringBuilder.printMethods(
        operation: Operation,
        table: String,
        types: OpenApiTypes,
        indent: String,
    ) {
        val path = pathParameters(operation)
        val required =
            operation.queryParameters.filter { it.required } + operation.headerParameters.filter { it.required }
        val names = (path + required).map { it.name }
        val clash = names.filter { it in RESERVED }.toSet() + names.groupBy { it }.filterValues { it.size > 1 }.keys
        if (clash.isNotEmpty()) {
            throw ModelException(
                "${operation.operationId}: a parameter named ${clash.joinToString()}, which the printed method's own names would shadow",
            )
        }
        val inner = "$indent    "
        val parameters = "${types.namespace}.${parametersClass(operation)}"
        val body = operation.requestBody
        val bodyType =
            body?.let { declared ->
                bodyClass(operation)?.let { "${types.namespace}.$it" } ?: types.kotlin(declared.content.single().type)
            }
        val bodyDoc =
            body?.let { declared ->
                if (declared.content.size > 1) {
                    "@param body The request body, in one of the media types the operation accepts."
                } else {
                    "@param body The request body, sent as `${declared.content.single().mediaType}`."
                }
            }
        val tags =
            (path + required).map { "@param ${KotlinText.identifier(it.name)} ${it.description?.trim().orEmpty()}" } +
                listOfNotNull(
                    bodyDoc,
                    "@param options What this call overrides of its client's configuration.",
                    "@param parameters Sets the optional parameters, each by assigning it.",
                )
        kdoc(indent, listOf(operation.summary, operation.description, tags.joinToString("\n")))
        deprecation(indent, operation.deprecated, operation.description)

        val streams = operation.success.kind == "event-stream"
        val keyword = if (streams) "public fun " else "public suspend fun "
        append(indent).append(keyword).append(operation.operationId).append("(\n")
        for (parameter in path + required) {
            append(inner).append("${KotlinText.identifier(parameter.name)}: ${types.kotlin(parameter.type)},\n")
        }
        if (body != null && bodyType != null) {
            append(inner).append("body: ").append(bodyType).append(if (body.required) ",\n" else "? = null,\n")
        }
        append(inner).append("options: $OPTIONS = $OPTIONS(),\n")
        append(inner).append("parameters: ").append(parameters).append(".() -> kotlin.Unit = {},\n")
        append(indent).append("): ").append(returnType(operation, types)).append(" {\n")

        val optional = optionalParameters(operation)
        if (readsBuilder(operation)) {
            append(inner).append("val given = ").append(parameters).append("().apply(parameters)\n")
        } else {
            append(inner).append(parameters).append("().apply(parameters)\n")
        }
        append(inner).append("val call =\n")
        append(inner).append("    $CLIENT.OperationCall(\n")
        val argument = "$inner        "
        append(argument).append("facts = $table.${operation.operationId},\n")
        append(argument).append("path = ").append(pathExpression(operation, path, types)).append(",\n")
        append(argument).append("query =")
        appendPairs(operation.queryParameters, optional, types, argument)
        append(",\n")
        append(argument).append("headers =")
        appendPairs(operation.headerParameters, optional, types, argument)
        append(",\n")
        append(argument).append("body = ").append(bodyExpression(operation, types, argument)).append(",\n")
        if (operation.idempotencyKey) append(argument).append("idempotencyKey = given.$KEY_SLOT,\n")
        if (operation.conditional) append(argument).append("ifMatch = given.$TAG_SLOT,\n")
        append(argument).append("options = options,\n")
        append(inner).append("    )\n")
        append(inner).append("return ").append(sendExpression(operation, types)).append('\n')
        append(indent).append("}\n")
    }

    /**
     * Prints the walk beside [operation]'s method, where it pages by cursor: the method's path parameters and
     * the query parameters it requires, a page size defaulting to the `default` its `limit` states, a ceiling on
     * requests and the call's options, then the method's own trailing builder, answering the listing's rows as
     * a cold `Flow` through `PageWalk` over the method. A paged operation the walk cannot read is refused.
     */
    fun StringBuilder.printWalk(
        operation: Operation,
        types: OpenApiTypes,
        indent: String,
    ) {
        val row = walkRow(operation, types)
        val pageSize = walkPageSize(operation)
        val path = pathParameters(operation)
        val required =
            operation.queryParameters.filter { it.required } + operation.headerParameters.filter { it.required }
        val shadowed = (path + required).map { it.name }.filter { it in WALK_RESERVED }
        if (shadowed.isNotEmpty()) {
            throw ModelException(
                "${operation.operationId}: a parameter named ${shadowed.joinToString()}, which its walk's own names would shadow",
            )
        }
        val inner = "$indent    "
        val parameters = "${types.namespace}.${parametersClass(operation)}"
        val tags =
            (path + required).map { "@param ${KotlinText.identifier(it.name)} ${it.description?.trim().orEmpty()}" } +
                listOf(
                    "@param pageSize How many rows each request asks for, from 1 to 100.",
                    "@param maxPages The most requests the walk makes, at least 1, or null to read the listing to its last page.",
                    "@param options What every request of the walk overrides of its client's configuration.",
                    "@param parameters Sets the optional parameters every request sends, each by assigning it.",
                )
        kdoc(
            indent,
            listOf(
                KotlinText.wrapped(
                    "Walks the listing `${operation.operationId}` answers, one row at a time, requesting a page " +
                        "only as its collector takes the rows before it (`transport.pagination.walk`).",
                    WALK_DOC_WIDTH,
                ),
                KotlinText.wrapped(
                    "Every request sends what [parameters] sets, its filters and `sort` included, with its `limit` " +
                        "replaced by [pageSize] and its `cursor` by the one the walk has reached, starting from the " +
                        "one [parameters] names, so a walk resumes where an earlier one stopped. It follows every " +
                        "page for as long as a `next_cursor` names one, an empty page included, and stops at a null " +
                        "`next_cursor`, at [maxPages] requests, which raises nothing, or at a cursor it has already " +
                        "sent, which raises the base `ApiException`.",
                    WALK_DOC_WIDTH,
                ),
                KotlinText.wrapped(
                    "It throws `ConfigurationException` here, before any flow is collected, for a [pageSize] " +
                        "outside 1 to 100 or a [maxPages] under 1. A collector that is cancelled, or that stops " +
                        "early, requests no further page.",
                    WALK_DOC_WIDTH,
                ),
                tags.joinToString("\n"),
            ),
        )
        deprecation(indent, operation.deprecated, operation.description)
        // The walk calls the method it walks, which the contract deprecates with it.
        if (operation.deprecated) append(indent).append("@kotlin.Suppress(\"DEPRECATION\")\n")
        append(indent).append("public fun ").append(walkName(operation)).append("(\n")
        for (parameter in path + required) {
            append(inner).append("${KotlinText.identifier(parameter.name)}: ${types.kotlin(parameter.type)},\n")
        }
        append(inner).append("pageSize: kotlin.Int = ").append(pageSize).append(",\n")
        append(inner).append("maxPages: kotlin.Int? = null,\n")
        append(inner).append("options: $OPTIONS = $OPTIONS(),\n")
        append(inner).append("parameters: ").append(parameters).append(".() -> kotlin.Unit = {},\n")
        append(indent).append("): $FLOW<$row> =\n")
        append(inner).append("$PAGE_WALK(pageSize, maxPages) { paged ->\n")
        append(inner).append("    ").append(operation.operationId).append("(\n")
        for (parameter in path + required) {
            val name = KotlinText.identifier(parameter.name)
            append(inner)
                .append("        ")
                .append(name)
                .append(" = ")
                .append(name)
                .append(",\n")
        }
        append(inner).append("        options = options,\n")
        append(inner).append("    ) {\n")
        append(inner).append("        parameters()\n")
        append(inner).append("        paged()\n")
        append(inner).append("    }\n")
        append(inner).append("}\n")
    }

    /**
     * The type of a row of [operation]'s listing: the items of the list its `data` holds, which its metadata,
     * `ListMetadata`, pages. A walk reads the REST surface's `ListMetadata`, the one `PageWalk` names, so a
     * listing on any other surface, one that takes a body, one whose `data` is no list and one whose metadata is
     * not that component are refused.
     */
    private fun walkRow(
        operation: Operation,
        types: OpenApiTypes,
    ): String {
        val id = operation.operationId
        if (types.surface != "Rest") {
            throw ModelException("$id: it pages by cursor on a surface whose `$LIST_METADATA` no walk reads")
        }
        val body = operation.requestBody
        if (body != null) throw ModelException("$id: it pages by cursor and takes a body, which no walk sends")
        val data = operation.success.data
        val items = (data?.type as? ArrayType)?.items
        if (operation.success.kind != "envelope" || data == null || !data.required || data.type.nullable ||
            items == null
        ) {
            throw ModelException("$id: it pages by cursor, and its `data` is no list a walk reads its rows from")
        }
        val metadata = operation.success.metadata
        val page = metadata?.type as? RefType
        if (metadata == null || !metadata.required || page == null || page.nullable || page.name != LIST_METADATA) {
            throw ModelException("$id: it pages by cursor, and its metadata is not the `$LIST_METADATA` a walk reads")
        }
        return types.kotlin(items)
    }

    /** The page size a walk of [operation] asks for where its caller names none: the `default` its `limit` states. */
    private fun walkPageSize(operation: Operation): String {
        val limit = operation.queryParameters.firstOrNull { it.name == "limit" }
        val default = limit?.default
        if (limit == null || !limit.hasDefault || (default !is Int && default !is Long)) {
            throw ModelException(
                "${operation.operationId}: it pages by cursor, and its `limit` states no whole number of rows by default",
            )
        }
        return default.toString()
    }

    private fun returnType(
        operation: Operation,
        types: OpenApiTypes,
    ): String =
        when (operation.success.kind) {
            "envelope" -> "$CLIENT.Response<${dataType(operation, types)}, ${metadataType(operation, types)}>"
            "event-stream" -> "$CLIENT.EventStream<${eventType(operation, types)}>"
            "binary" -> "$CLIENT.BinaryResponse"
            else -> throw ModelException("${operation.operationId}: a success of kind `${operation.success.kind}`")
        }

    private fun sendExpression(
        operation: Operation,
        types: OpenApiTypes,
    ): String =
        when (operation.success.kind) {
            "envelope" -> "sender.send(call, ${serializers(operation, types)})"
            "event-stream" -> "sender.stream(call, ${types.serializer(eventNode(operation))})"
            else -> "sender.download(call)"
        }

    /** The serializers an envelope's `data` and `metadata` are read with, in that order. */
    private fun serializers(
        operation: Operation,
        types: OpenApiTypes,
    ): String = "${dataSerializer(operation, types)}, ${metadataSerializer(operation, types)}"

    private fun dataType(
        operation: Operation,
        types: OpenApiTypes,
    ): String {
        val data = operation.success.data ?: return SurfaceTypes.JSON_ELEMENT
        return types.kotlin(data.type) + if (!data.required && !data.type.nullable) "?" else ""
    }

    private fun dataSerializer(
        operation: Operation,
        types: OpenApiTypes,
    ): String {
        val data =
            operation.success.data
                ?: return "${SurfaceTypes.JSON_ELEMENT}.serializer()"
        val serializer = types.serializer(data.type)
        return if (!data.required &&
            !data.type.nullable
        ) {
            "$CLIENT.nullableSerializer($serializer)"
        } else {
            serializer
        }
    }

    private fun metadataType(
        operation: Operation,
        types: OpenApiTypes,
    ): String {
        val metadata = operation.success.metadata ?: return "${SurfaceTypes.JSON_OBJECT}?"
        return types.kotlin(metadata.type) + if (!metadata.required && !metadata.type.nullable) "?" else ""
    }

    private fun metadataSerializer(
        operation: Operation,
        types: OpenApiTypes,
    ): String {
        val metadata =
            operation.success.metadata
                ?: return "$CLIENT.nullableSerializer(${SurfaceTypes.JSON_OBJECT}.serializer())"
        val serializer = types.serializer(metadata.type)
        return if (!metadata.required &&
            !metadata.type.nullable
        ) {
            "$CLIENT.nullableSerializer($serializer)"
        } else {
            serializer
        }
    }

    private fun eventNode(operation: Operation): TypeNode =
        operation.success.event
            ?: throw ModelException("${operation.operationId}: an event stream that declares no event")

    private fun eventType(
        operation: Operation,
        types: OpenApiTypes,
    ): String = types.kotlin(eventNode(operation))

    private fun pathExpression(
        operation: Operation,
        path: List<Parameter>,
        types: OpenApiTypes,
    ): String {
        val byName = path.associateBy { it.name }
        val pieces = mutableListOf<String>()
        var rest = operation.path
        while (rest.isNotEmpty()) {
            val open = rest.indexOf('{')
            if (open < 0) {
                pieces += KotlinText.literal(rest)
                break
            }
            if (open > 0) pieces += KotlinText.literal(rest.substring(0, open))
            val close = rest.indexOf('}', open)
            val parameter = byName.getValue(rest.substring(open + 1, close))
            if (parameter.type.nullable) {
                throw ModelException(
                    "${operation.operationId}: the path parameter ${parameter.name} may be null",
                )
            }
            pieces += "$ENCODE_SEGMENT(${types.text(parameter.type, KotlinText.identifier(parameter.name))})"
            rest = rest.substring(close + 1)
        }
        return pieces.joinToString(" + ")
    }

    /** Appends the name and value pairs of [declared], in its order: a required one from its argument, an optional one from the builder. */
    private fun StringBuilder.appendPairs(
        declared: List<Parameter>,
        optional: List<Parameter>,
        types: OpenApiTypes,
        indent: String,
    ) {
        if (declared.isEmpty()) {
            append(" kotlin.collections.emptyList()")
            return
        }
        append("\n").append(indent).append("    kotlin.collections.buildList {\n")
        for (parameter in declared) {
            val name = KotlinText.literal(parameter.name)
            append(indent).append("        ")
            val slot = optional.indexOf(parameter)
            when {
                slot >= 0 -> {
                    append("given.slot$slot?.let { add($name to ${types.text(parameter.type, "it")}) }")
                }

                parameter.type.nullable -> {
                    val identifier = KotlinText.identifier(parameter.name)
                    append("$identifier?.let { add($name to ${types.text(parameter.type, "it")}) }")
                }

                else -> {
                    append("add($name to ${types.text(parameter.type, KotlinText.identifier(parameter.name))})")
                }
            }
            append('\n')
        }
        append(indent).append("    }")
    }

    private fun bodyExpression(
        operation: Operation,
        types: OpenApiTypes,
        indent: String,
    ): String {
        val body = operation.requestBody ?: return "$CLIENT.RequestBody.Empty"
        val name = bodyClass(operation)
        val written =
            if (name == null) {
                encode(operation, body.content.single(), "body", types, indent)
            } else {
                val qualified = "${types.namespace}.$name"
                buildString {
                    append("\n").append(indent).append("    when (body) {\n")
                    for (content in body.content) {
                        val case = "$qualified.${mediaCase(operation, content)}"
                        val encoded = encode(operation, content, "body.value", types, "$indent        ")
                        append(indent).append("        is $case -> $encoded\n")
                    }
                    append(indent).append("        else -> throw kotlin.IllegalArgumentException(")
                    append(KotlinText.literal("$qualified has no media type of this class.")).append(")\n")
                    append(indent).append("    }")
                }
            }
        val nullable = !body.required || body.content.any { it.type.nullable }
        return if (nullable) "if (body == null) $CLIENT.RequestBody.Empty else $written" else written
    }

    /**
     * The body [value], declared as [content], on the wire's terms: a JSON body written straight to
     * the text sent, never built as a tree of JSON first.
     */
    private fun encode(
        operation: Operation,
        content: MediaContent,
        value: String,
        types: OpenApiTypes,
        indent: String,
    ): String {
        if (mediaCase(operation, content) != "Json") return multipart(operation, content, value, types, indent)
        if (content.encoding.isNotEmpty()) {
            throw ModelException(
                "${operation.operationId}: an `encoding` on a `${content.mediaType}` body, which this printer sends " +
                    "as one JSON document of no parts",
            )
        }
        val serializer = types.serializer(nonNull(content.type))
        return "$CLIENT.RequestBody.Json($WIRE_JSON.encodeToString($serializer, $value))"
    }

    /**
     * The form [content] declares, [value] its body: a part per property in the type's order, a file's sent as the
     * media type the form's `encoding` names for it where it names one, and as the client's default, which
     * RFC 7578 has a sender write, where it names none. An `encoding` this printer cannot send is refused: one for
     * a property the form does not hold, one for a part that is no file, since a text part carries no media type of
     * its own here, and one naming a part twice or a media type no header carries.
     */
    private fun multipart(
        operation: Operation,
        content: MediaContent,
        value: String,
        types: OpenApiTypes,
        indent: String,
    ): String {
        val properties =
            when (val type = content.type) {
                is InlineObject -> type.properties
                is RefType -> (types.schema(type) as? ObjectSchema)?.properties
                else -> null
            } ?: throw ModelException("${operation.operationId}: a form body that is not an object")
        val declared = content.encoding.groupBy { it.property }
        for ((property, encodings) in declared) {
            val where = "${operation.operationId}: the form's `encoding` of `$property`"
            val held = properties.firstOrNull { it.name == property }
            when {
                encodings.size > 1 -> {
                    throw ModelException("$where is stated twice")
                }

                held == null -> {
                    throw ModelException("$where names no part of the form")
                }

                !types.isBytes(held.type) -> {
                    throw ModelException("$where names a part that is no file")
                }

                !isMediaType(encodings.single().contentType) -> {
                    throw ModelException("$where is `${encodings.single().contentType}`, which no header carries")
                }
            }
        }
        val parts =
            properties.joinToString("\n") { property ->
                val name = KotlinText.literal(property.name)
                val field = "$value.${KotlinText.identifier(property.name)}"
                val constant = property.discriminatorValue != null || (property.type as? ScalarType)?.constant != null
                val nullable = !constant && (!property.required || property.type.nullable)
                val fileType = declared[property.name]?.single()?.contentType

                fun part(text: String): String =
                    if (types.isBytes(property.type) && fileType != null) {
                        "add($CLIENT.MultipartPart.File($name, $text, contentType = ${KotlinText.literal(fileType)}))"
                    } else if (types.isBytes(property.type)) {
                        "add($CLIENT.MultipartPart.File($name, $text))"
                    } else {
                        "add($CLIENT.MultipartPart.Text($name, ${types.text(property.type, text)}))"
                    }
                "$indent        " + if (nullable) "$field?.let { ${part("it")} }" else part(field)
            }
        return "$CLIENT.RequestBody.Multipart(\n" +
            "$indent    kotlin.collections.buildList {\n$parts\n$indent    },\n" +
            "$indent)"
    }

    /** Whether [text] is a media type a header carries: printable ASCII, a `/` between two names, nothing else blank. */
    private fun isMediaType(text: String): Boolean =
        text.all { it in '!'..'~' || it == ' ' } &&
            text.substringBefore(';').trim().let { type ->
                type.count { it == '/' } == 1 && type.split('/').all { it.isNotEmpty() && ' ' !in it }
            }

    private fun nonNull(type: TypeNode): TypeNode =
        when (type) {
            is ScalarType -> {
                ScalarType(type.kind, type.format, type.constant, type.enumName, type.enumValues, false, type.enumNames)
            }

            is ArrayType -> {
                ArrayType(type.items, false)
            }

            is MapType -> {
                MapType(type.values, type.keys, false)
            }

            is InlineObject -> {
                InlineObject(type.name, type.properties, type.additional, type.closed, false)
            }

            is InlineUnion -> {
                InlineUnion(type.name, type.members, false)
            }

            is RefType -> {
                RefType(type.name, false)
            }

            is AnyType -> {
                AnyType(false)
            }

            is NullType -> {
                NullType(false)
            }
        }

    /** Prints a Data API method: its input, the caller's selection, and the call's options. */
    fun StringBuilder.printDataMethod(
        operation: DataOperationModel,
        types: DataTypes,
        indent: String,
    ) {
        val inner = "$indent    "
        val input = "${types.namespace}.${operation.input}"
        val result = "${types.namespace}.${operation.resultType}"
        kdoc(
            indent,
            listOf(
                operation.description,
                listOf(
                    "@param input The operation's input.",
                    "@param select The fields to fetch, checked against the operation's field tree " +
                        "before anything is sent, " +
                        "or null for every field.",
                    "@param options What this call overrides of its client's configuration.",
                ).joinToString("\n"),
            ),
        )
        deprecation(indent, operation.deprecated, operation.description)
        append(indent).append("public suspend fun ").append(KotlinText.identifier(operation.name)).append("(\n")
        append(inner).append("input: ").append(input).append(",\n")
        append(inner).append("select: $CLIENT.FieldSelection? = null,\n")
        append(inner)
            .append("options: ")
            .append(OPTIONS)
            .append(" = ")
            .append(OPTIONS)
            .append("(),\n")
        append(indent).append("): ").append(result).append(" {\n")
        append(inner).append("val operation = DataOperations.").append(KotlinText.identifier(operation.name))
        append('\n')
        append(inner).append("val call =\n")
        append(inner).append("    $CLIENT.DataCall(\n")
        append(inner).append("        facts = OperationTable.Data.").append(KotlinText.identifier(operation.name))
        append(",\n")
        append(inner).append("        operation = operation,\n")
        append(inner).append("        document = operation.document(select),\n")
        // The input is written straight to the text sent, never built as a tree of JSON first.
        append(inner).append("        input = $WIRE_JSON.encodeToString($input.serializer(), input),\n")
        append(inner).append("        options = options,\n")
        append(inner).append("    )\n")
        append(inner).append("return sender.query(call, $result.serializer())\n")
        append(indent).append("}\n")
    }

    /** Prints the facts of one OpenAPI operation, as a property of the table. */
    fun StringBuilder.printFacts(
        operation: Operation,
        surface: String,
        indent: String,
    ) {
        val credentials = operation.credentials
        val success =
            when (operation.success.kind) {
                "envelope" -> "Envelope"
                "event-stream" -> "EventStream"
                else -> "Binary"
            }
        append(indent).append("val ").append(operation.operationId).append(": $CLIENT.OperationFacts =\n")
        append(indent).append("    $CLIENT.OperationFacts(\n")
        val argument = "$indent        "
        append(argument).append("surface = $CLIENT.Surface.").append(surface)
        append(",\n")
        append(argument).append("operationId = ").append(KotlinText.literal(operation.operationId)).append(",\n")
        append(argument).append("method = ").append(KotlinText.literal(operation.method)).append(",\n")
        append(argument).append("path = ").append(KotlinText.literal(operation.path)).append(",\n")
        append(argument).append("idempotent = ").append(operation.idempotent).append(",\n")
        printCredentials(credentials, argument)
        append(argument).append("timeoutMs = ").append(operation.timeoutMs?.toString() ?: "null").append(",\n")
        append(argument).append("declaresIdempotencyKey = ").append(operation.idempotencyKey).append(",\n")
        append(argument).append("isConditional = ").append(operation.conditional).append(",\n")
        append(argument).append("answersNotModified = ").append(operation.notModified).append(",\n")
        append(argument).append("pagination =")
        val pagination = operation.pagination
        if (pagination == null) {
            append(" null,\n")
        } else {
            append("\n").append(argument).append("    $CLIENT.Pagination(\n")
            append(argument).append("        sort = kotlin.collections.listOf(")
            append(pagination.sort.joinToString(", ") { KotlinText.literal(it) }).append("),\n")
            append(argument).append("        defaultSort = ")
            append(pagination.defaultSort?.let { KotlinText.literal(it) } ?: "null").append(",\n")
            append(argument).append("    ),\n")
        }
        append(argument).append("success = $CLIENT.SuccessKind.").append(success).append(",\n")
        printSuccessResponses(operation.success.responses, argument)
        append(argument).append("errorStatuses = kotlin.collections.listOf(")
        append(
            operation.errors
                .map { it.status }
                .sorted()
                .joinToString(", "),
        ).append("),\n")
        // The tasks a write runs, in the model's order. The transport reads none of them, and the
        // facts gate holds them to the model.
        append(argument).append("tasks = ")
        if (operation.tasks.isEmpty()) {
            append("kotlin.collections.emptyList()")
        } else {
            append("kotlin.collections.listOf(").append(operation.tasks.joinToString(", ") { KotlinText.literal(it) })
            append(")")
        }
        append(",\n")
        append(indent).append("    )\n")
    }

    /**
     * Prints what an operation declares of the credentials it takes, as the facts' `credentials` argument, and
     * whether it is declared a read whatever its method, [read], which every Data API query is over its `POST`
     * (`sdk/SPEC.md` section 1) and no REST operation states.
     */
    private fun StringBuilder.printCredentials(
        credentials: Credentials,
        argument: String,
        read: Boolean = false,
    ) {
        append(argument).append("credentials =\n")
        append(argument).append("    $CLIENT.CredentialDeclaration(\n")
        // The model's `required`, `optional` or `none`, as the vocabulary's UpperCamelCase member names it.
        append(argument).append("        actingUser = $CLIENT.CredentialDeclaration.ActingUser.")
        append(KotlinText.capitalized(credentials.actingUser)).append(",\n")
        append(argument).append("        acceptsShareToken = ").append(credentials.acceptsShareToken).append(",\n")
        append(argument).append("        requiresSession = ").append(credentials.requiresSession).append(",\n")
        append(argument).append("        requiresSecretKey = ").append(credentials.requiresSecretKey).append(",\n")
        append(argument).append("        takesNoCredential = ").append(credentials.takesNoCredential).append(",\n")
        append(argument).append("        pendingInvitee = ").append(credentials.pendingInvitee).append(",\n")
        append(argument).append("        read = ").append(read).append(",\n")
        append(argument).append("    ),\n")
    }

    /**
     * Prints each status a success arrives with and the names of the headers it declares, one status a
     * line, in the model's order: they decide nothing at request time, and the facts gate holds them to
     * the model.
     */
    private fun StringBuilder.printSuccessResponses(
        responses: List<SuccessResponse>,
        argument: String,
    ) {
        append(argument).append("successResponses =\n")
        append(argument).append("    kotlin.collections.listOf(\n")
        for (response in responses) {
            append(argument).append("        $CLIENT.SuccessResponse(").append(response.status).append(", ")
            if (response.headers.isEmpty()) {
                append("kotlin.collections.emptyList()")
            } else {
                append("kotlin.collections.listOf(")
                append(response.headers.joinToString(", ") { KotlinText.literal(it.name) }).append(")")
            }
            append("),\n")
        }
        append(argument).append("    ),\n")
    }

    /**
     * Prints the facts of one Data API operation: sent to the endpoint, idempotent, as every Data API
     * operation is, taking the credentials and answering the statuses the model states for it, and
     * waiting the budget the model carries for it. It takes no key and no tag, pages by no cursor,
     * declares no error status and runs no task: its answer is a GraphQL result.
     */
    fun StringBuilder.printDataFacts(
        operation: DataOperationModel,
        endpoint: String,
        indent: String,
    ) {
        append(indent).append("val ").append(KotlinText.identifier(operation.name))
        append(": $CLIENT.OperationFacts =\n")
        append(indent).append("    $CLIENT.OperationFacts(\n")
        val argument = "$indent        "
        append(argument).append("surface = $CLIENT.Surface.Data,\n")
        append(argument).append("operationId = ").append(KotlinText.literal(operation.name)).append(",\n")
        append(argument).append("method = \"POST\",\n")
        append(argument).append("path = ").append(KotlinText.literal(endpoint)).append(",\n")
        append(argument).append("idempotent = true,\n")
        printCredentials(operation.credentials, argument, read = true)
        append(argument).append("timeoutMs = ").append(operation.timeoutMs).append(",\n")
        append(argument).append("declaresIdempotencyKey = false,\n")
        append(argument).append("isConditional = false,\n")
        append(argument).append("answersNotModified = false,\n")
        append(argument).append("pagination = null,\n")
        append(argument).append("success = $CLIENT.SuccessKind.Graphql,\n")
        printSuccessResponses(operation.responses, argument)
        append(argument).append("errorStatuses = kotlin.collections.emptyList(),\n")
        append(argument).append("tasks = kotlin.collections.emptyList(),\n")
        append(indent).append("    )\n")
    }

    /** Prints [operation] as a `DataOperation`: its name, its input type, and its full field tree in the SDL's order. */
    fun StringBuilder.printDataOperation(
        operation: DataOperationModel,
        indent: String,
    ) {
        kdoc(indent, listOf("`${operation.name}`, with its full field tree."))
        append(indent).append("val ").append(KotlinText.identifier(operation.name))
        append(": $CLIENT.DataOperation =\n")
        append(indent).append("    $CLIENT.DataOperation(\n")
        append(indent).append("        name = ").append(KotlinText.literal(operation.name)).append(",\n")
        append(indent).append("        inputType = ").append(KotlinText.literal(operation.inputType)).append(",\n")
        append(indent).append("        selection =\n")
        append(indent).append("            $CLIENT.FieldSelection {\n")
        printSelection(operation.selection, "$indent                ")
        append(indent).append("            },\n")
        append(indent).append("    )\n")
    }

    private fun StringBuilder.printSelection(
        fields: List<SelectionNode>,
        indent: String,
    ) {
        for (field in fields) {
            val beneath = field.fields
            if (beneath == null) {
                append(indent).append("field(").append(KotlinText.literal(field.name)).append(")\n")
            } else {
                append(indent).append("field(").append(KotlinText.literal(field.name)).append(") {\n")
                printSelection(beneath, "$indent    ")
                append(indent).append("}\n")
            }
        }
    }

    /** The type of the sender every accessor holds. */
    const val SENDER_TYPE: String = SENDER
}
