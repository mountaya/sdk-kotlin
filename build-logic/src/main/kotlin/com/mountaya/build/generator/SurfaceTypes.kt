package com.mountaya.build.generator

/**
 * The Kotlin a surface's types are printed as: each type node's Kotlin type, the serializer that
 * reads it, and the text a value of it becomes in a path, a query or a form.
 *
 * Every name is written in full, so no printed file needs an import and no contract name can
 * shadow a platform one: inside `RestSchema` the contract's `Collection` is a type of its own, and
 * Kotlin's is `kotlin.collections.Collection`.
 */
internal abstract class SurfaceTypes(
    /**
     * The object the surface's types are nested in: `RestSchema`, `TileSchema` or `DataSchema`, and `AdminSchema`
     * for the REST API's admin unit.
     */
    val namespace: String,
    /** The package the namespace object sits in, which the serial name of a printed serializer starts with. */
    val packageName: String,
) {
    /** Returns the Kotlin type of [type], `?` included where it may be null. */
    fun kotlin(type: TypeNode): String = base(type) + if (type.nullable) "?" else ""

    /** Returns the Kotlin type of [type] as if it could not be null. */
    abstract fun base(type: TypeNode): String

    /**
     * Returns the Kotlin type [type] is held in by a class a request sends, as if it could not be null:
     * [base], each web address in it marked to be written by the serializer that refuses one with no
     * scheme, so a caller's is refused as the request's body is written and an answer's is read as sent.
     */
    fun sentBase(type: TypeNode): String = sent(base(type))

    /** Returns the expression of a serializer for [type], reading `null` too where it may be null. */
    fun serializer(type: TypeNode): String {
        val serializer = baseSerializer(type)
        return if (type.nullable) "com.mountaya.client.nullableSerializer($serializer)" else serializer
    }

    /** Returns the expression of a serializer for [type] as if it could not be null. */
    protected abstract fun baseSerializer(type: TypeNode): String

    /**
     * Returns the expression of the text the value [value] of [type] is written as in a path, a
     * query or a form field, [value] being known not to be null. A type with no single text, such
     * as an array or an object, is refused: the contract states no rule for it.
     */
    abstract fun text(
        type: TypeNode,
        value: String,
    ): String

    /** Whether [type] is a file's bytes, which a form sends as a file rather than as text. */
    fun isBytes(type: TypeNode): Boolean = type is ScalarType && type.kind == "string" && type.format == "binary"

    /** Returns the Kotlin of a scalar of the OpenAPI vocabulary, which the Data API's scalars are carried as. */
    protected fun scalar(type: ScalarType): String =
        when (type.kind) {
            "string" -> {
                when (type.format) {
                    null, "uuid", "date", "email" -> "kotlin.String"

                    "date-time" -> INSTANT

                    "uri" -> URL

                    "binary" -> "kotlin.ByteArray"

                    else -> throw ModelException(
                        "a string of format `${type.format}`, which this printer does not know",
                    )
                }
            }

            "integer" -> {
                when (type.format) {
                    null, "int32" -> "kotlin.Int"

                    "int64" -> "kotlin.Long"

                    else -> throw ModelException(
                        "an integer of format `${type.format}`, which this printer does not know",
                    )
                }
            }

            "number" -> {
                when (type.format) {
                    null, "double", "float" -> "kotlin.Double"

                    else -> throw ModelException(
                        "a number of format `${type.format}`, which this printer does not know",
                    )
                }
            }

            "boolean" -> {
                "kotlin.Boolean"
            }

            else -> {
                throw ModelException("a scalar of kind `${type.kind}`, which this printer does not know")
            }
        }

    /** Returns the serializer of a scalar of the OpenAPI vocabulary. */
    protected fun scalarSerializer(type: ScalarType): String =
        when (val kotlin = scalar(type)) {
            INSTANT -> "com.mountaya.client.TimestampSerializer"
            URL -> "$URL.serializer()"
            else -> "kotlinx.serialization.serializer<$kotlin>()"
        }

    /** Returns the text of a scalar of the OpenAPI vocabulary. */
    protected fun scalarText(
        type: ScalarType,
        value: String,
    ): String =
        when (scalar(type)) {
            "kotlin.String" -> value

            // A timestamp is written as a body writes it, to the millisecond.
            INSTANT -> "com.mountaya.client.formatTimestamp($value)"

            // A web address a caller sends is held to a scheme, as one a body carries is.
            URL -> "com.mountaya.client.SentUrlSerializer.sentText($value)"

            "kotlin.ByteArray" -> throw ModelException("a file's bytes have no text: only a form field sends them")

            else -> "$value.toString()"
        }

    companion object {
        const val INSTANT = "kotlin.time.Instant"
        const val URL = "com.mountaya.client.Url"
        const val JSON_ELEMENT = "kotlinx.serialization.json.JsonElement"
        const val JSON_OBJECT = "kotlinx.serialization.json.JsonObject"

        /**
         * The annotation a web address a request writes is marked with: written by the serializer that refuses one
         * with no scheme.
         */
        private const val SENT_URL =
            "@kotlinx.serialization.Serializable(with = com.mountaya.client.SentUrlSerializer::class)"

        /** [URL] as a whole name, never the start of a longer one such as `UrlTemplate`. */
        private val URL_NAME = Regex(Regex.escape(URL) + "(?![A-Za-z0-9_])")

        /** Returns the Kotlin type [kotlin] with each web address in it marked to be written as a request writes one. */
        fun sent(kotlin: String): String = URL_NAME.replace(kotlin) { "$SENT_URL $URL" }

        /** The hand-written class RFC 7946's [geometry] is, `Geometry` naming any of them. */
        fun geoJson(geometry: String): String {
            val shapes =
                setOf(
                    "Geometry",
                    "Point",
                    "MultiPoint",
                    "LineString",
                    "MultiLineString",
                    "Polygon",
                    "MultiPolygon",
                    "GeometryCollection",
                )
            if (geometry !in shapes) throw ModelException("`$geometry` is not one of RFC 7946's geometries")
            return "com.mountaya.client.GeoJson$geometry"
        }
    }
}

/**
 * The types of the REST API or the Tile API, as the files of one [unit] print them.
 *
 * A component of [unit] is named in [namespace], and an inline schema always is, since a unit prints every inline
 * schema its own components and operations hold. The admin unit names a component of the core too, in full, through
 * [core], the core's types as a file of another module names them; the core names nothing of the admin unit, and a
 * reference to one is refused, since a core that compiled only beside its admin unit would be no core.
 */
internal class OpenApiTypes(
    namespace: String,
    private val schemas: Map<String, Schema>,
    /** The unit whose files these types are printed into. */
    val unit: ClientUnit = ClientUnit.Core,
    /** `Rest` or `Tiles`, the surface's name in the operation table: a walk reads the REST API's listings alone. */
    val surface: String = "Rest",
    packageName: String = ClientPrinter.PACKAGE,
    /** The core's types as a file of another unit names them, every name in full, or null where [unit] is the core. */
    private val core: OpenApiTypes? = null,
) : SurfaceTypes(namespace, packageName) {
    /**
     * Whether the surface declares a component named [name], the envelope's included. A type the
     * printer reads and prints nothing for is held to this, so a name naming no component is
     * refused rather than read past.
     */
    fun declares(name: String): Boolean = name in schemas

    /**
     * The printed schema a reference names. The envelope is read by hand and never referenced from a printed type,
     * and a unit names no component of another unit but the core's.
     */
    fun schema(reference: RefType): Schema {
        val schema =
            schemas[reference.name]
                ?: throw ModelException("$namespace: `${reference.name}` is referenced and not declared")
        if (schema.role == "envelope") {
            throw ModelException(
                "$namespace: `${reference.name}` is part of the envelope, which is read by hand, and a printed type refers to it",
            )
        }
        if (schema.unit != unit && (schema.unit != ClientUnit.Core || core == null)) {
            throw ModelException(
                "$namespace: `${reference.name}` is the ${schema.unit.wire} unit's, and the ${unit.wire} unit names " +
                    "nothing another unit declares but the core's",
            )
        }
        return schema
    }

    /** Returns the component [name] as a file of this unit names it. */
    fun named(name: String): String = base(RefType(name, false))

    /** The types [schema] is named through: these, or the core's where it is the core's and these another unit's. */
    private fun home(schema: Schema): OpenApiTypes =
        if (schema.unit == unit) {
            this
        } else {
            checkNotNull(core) {
                "`${schema.name}` is the ${schema.unit.wire} unit's, and the ${unit.wire} unit's types hold no core"
            }
        }

    override fun base(type: TypeNode): String =
        when (type) {
            is ScalarType -> if (type.enumName != null) "$namespace.${type.enumName}" else scalar(type)

            is ArrayType -> "kotlin.collections.List<${kotlin(type.items)}>"

            is MapType -> "kotlin.collections.Map<${type.keys?.let(
                ::kotlin,
            ) ?: "kotlin.String"}, ${kotlin(type.values)}>"

            is InlineObject -> "$namespace.${type.name}"

            is InlineUnion -> "$namespace.${type.name}"

            is RefType -> schema(type).let { "${home(it).namespace}.${it.name}" }

            // A value that is always null is the envelope's own open type, holding `JsonNull`.
            is AnyType, is NullType -> JSON_ELEMENT
        }

    override fun baseSerializer(type: TypeNode): String =
        when (type) {
            is ScalarType -> {
                if (type.enumName !=
                    null
                ) {
                    "$namespace.${type.enumName}.serializer()"
                } else {
                    scalarSerializer(type)
                }
            }

            is ArrayType -> {
                "kotlinx.serialization.builtins.ListSerializer(${serializer(type.items)})"
            }

            is MapType -> {
                "kotlinx.serialization.builtins.MapSerializer(" +
                    "${type.keys?.let(
                        ::serializer,
                    ) ?: "kotlinx.serialization.serializer<kotlin.String>()"}, ${serializer(type.values)})"
            }

            is InlineObject -> {
                "$namespace.${type.name}.serializer()"
            }

            is InlineUnion -> {
                "$namespace.${type.name}.serializer()"
            }

            is RefType -> {
                when (val schema = schema(type)) {
                    is AliasSchema -> home(schema).serializer(schema.type)
                    is GeoJsonSchema -> "${geoJson(schema.geometry)}.serializer()"
                    else -> "${home(schema).namespace}.${schema.name}.serializer()"
                }
            }

            is AnyType, is NullType -> {
                "$JSON_ELEMENT.serializer()"
            }
        }

    override fun text(
        type: TypeNode,
        value: String,
    ): String =
        when (type) {
            is ScalarType -> {
                when {
                    type.enumName == null -> scalarText(type, value)
                    type.kind == "string" -> "$value.value"
                    else -> "$value.value.toString()"
                }
            }

            is RefType -> {
                when (val schema = schema(type)) {
                    is EnumSchema -> if (schema.valueType == "string") "$value.value" else "$value.value.toString()"

                    is AliasSchema -> home(schema).text(schema.type, value)

                    else -> throw ModelException(
                        "$namespace: a `${schema.name}` has no text, and the contract states no rule for one",
                    )
                }
            }

            else -> {
                throw ModelException(
                    "$namespace: an ${type::class.simpleName} has no text, and the contract states no rule for one",
                )
            }
        }
}

/** The types of the Data API, whose scalars map onto the same Kotlin types as the OpenAPI formats that mean the same. */
internal class DataTypes(
    private val data: DataSurface,
) : SurfaceTypes("DataSchema", ClientPrinter.PACKAGE) {
    private val scalars = data.scalars.associateBy { it.name }
    private val declared =
        (data.objects.map { it.name } + data.inputs.map { it.name } + data.enums.map { it.name })
            .toSet()

    override fun base(type: TypeNode): String =
        when (type) {
            is ArrayType -> "kotlin.collections.List<${kotlin(type.items)}>"

            is RefType -> scalars[type.name]?.let(::scalarType) ?: declaredName(type.name)

            else -> throw ModelException(
                "$namespace: a field of kind ${type::class.simpleName}, which the SDL does not have",
            )
        }

    override fun baseSerializer(type: TypeNode): String =
        when (type) {
            is ArrayType -> "kotlinx.serialization.builtins.ListSerializer(${serializer(type.items)})"

            is RefType -> scalars[type.name]?.let(::scalarSerializer) ?: "${declaredName(type.name)}.serializer()"

            else -> throw ModelException(
                "$namespace: a field of kind ${type::class.simpleName}, which the SDL does not have",
            )
        }

    override fun text(
        type: TypeNode,
        value: String,
    ): String = throw ModelException("$namespace: a Data API value is sent as a GraphQL variable, never as text")

    private fun declaredName(name: String): String {
        if (name !in declared) throw ModelException("$namespace: `$name` is referenced and not declared")
        return "$namespace.$name"
    }

    private fun scalarType(declared: DataScalar): String {
        val wire = declared.wire
        return when {
            declared.geometry != null -> geoJson(declared.geometry)

            wire is ScalarType -> scalar(wire)

            declared.builtin -> builtin(declared.name)

            else -> throw ModelException(
                "$namespace: the scalar `${declared.name}` has no wire type this printer knows",
            )
        }
    }

    private fun scalarSerializer(declared: DataScalar): String {
        val wire = declared.wire
        return when {
            declared.geometry != null -> "${geoJson(declared.geometry)}.serializer()"
            wire is ScalarType -> scalarSerializer(wire)
            else -> "kotlinx.serialization.serializer<${builtin(declared.name)}>()"
        }
    }

    private fun builtin(name: String): String =
        when (name) {
            "Boolean" -> "kotlin.Boolean"
            "Float" -> "kotlin.Double"
            "Int" -> "kotlin.Int"
            "ID", "String" -> "kotlin.String"
            else -> throw ModelException("$namespace: the built-in scalar `$name`, which GraphQL does not define")
        }

    /** Whether [data] declares the type [name] as an enum. */
    fun isEnum(name: String): Boolean = data.enums.any { it.name == name }
}
