package com.mountaya.build.generator

/**
 * One printed class: an object component, an inline object, a Data API type, or the optional
 * parameters of an operation. Everything a language decides about it is decided here, once, and
 * the printer only spells it.
 */
internal class ClassSpec(
    /** The name inside the namespace. */
    val name: String,
    /** The namespace it sits in: `RestSchema`, `TileSchema` or `DataSchema`. */
    val namespace: String,
    val description: String?,
    val properties: List<PropertySpec>,
    /** The union this class is a member of, which it extends, or null. */
    val union: String? = null,
    /** The type parameters the class is generic over, in order, each bounded by `kotlin.Any`. None for a component. */
    val typeParameters: List<String> = emptyList(),
) {
    val qualified: String get() = "$namespace.$name"

    /** The type parameters as a declaration writes them, bounds included, or nothing. */
    val declaredParameters: String
        get() = if (typeParameters.isEmpty()) "" else typeParameters.joinToString(", ", "<", ">") { "$it : kotlin.Any" }

    /** The type parameters as a use of the class writes them, or nothing. */
    val usedParameters: String
        get() = if (typeParameters.isEmpty()) "" else typeParameters.joinToString(", ", "<", ">")

    /** The class with a star for each type parameter, as an `is` check names it. */
    val starred: String
        get() = qualified + if (typeParameters.isEmpty()) "" else typeParameters.joinToString(", ", "<", ">") { "*" }
}

/** How a property is set, which decides its constructor parameter, its builder and its encoding. */
internal enum class PropertyRole {
    /** A parameter of the factory: the caller always says it, `null` included where it may be null. */
    Required,

    /** Set in the builder or not at all. Left out of a request until set. */
    Optional,

    /** Optional and nullable on an object a request sends: left out, a value, or `null`, which clears. */
    ThreeState,

    /** Fixed by the contract: a `const`, or a union member's discriminator. Always written, never set. */
    Fixed,
}

internal class PropertySpec(
    /** The contract's name, verbatim. */
    val name: String,
    val role: PropertyRole,
    /** The Kotlin type as if it could not be null. */
    val type: String,
    /** Whether the contract says it may be null. */
    val nullable: Boolean,
    /** The Kotlin source of the fixed value, for [PropertyRole.Fixed]. */
    val fixed: String?,
    val description: String?,
    /** The server's default, shown in the documentation, where it states one. */
    val default: Any?,
    val hasDefault: Boolean,
    val deprecated: Boolean,
    /** Whether it holds a file's bytes, compared and hashed by content. */
    val bytes: Boolean,
    /** Its position in the class, which names its storage. */
    val index: Int,
    /** Whether the server writes it and a request never sends it. */
    val readOnly: Boolean = false,
    /** Whether a request sends it and the server never answers it. */
    val writeOnly: Boolean = false,
    /**
     * Whether a three-state property is read as what the payload stated, a `Stated` holding `null`
     * where it stated `null`, and null where it stated nothing, rather than as the value alone.
     */
    val statesNull: Boolean = false,
    /**
     * The Kotlin type the class stores it in as if it could not be null: [type], each web address in it
     * marked to be written as a request writes one where a request sends the class.
     */
    val serialType: String = type,
    /** Whether the model marks it a secret, which the class's `toString` prints as `<redacted>`. */
    val secret: Boolean = false,
) {
    /** The Kotlin type of the public property a caller reads. */
    val readType: String
        get() =
            when (role) {
                PropertyRole.Required, PropertyRole.Fixed -> if (nullable) "$type?" else type
                PropertyRole.Optional -> "$type?"
                PropertyRole.ThreeState -> if (statesNull) "${ClassSpecs.STATED}<$type?>?" else "$type?"
            }

    /** [readType] as the constructor declares it, its web addresses marked as [serialType] marks them. */
    val serialReadType: String
        get() =
            when (role) {
                PropertyRole.Required, PropertyRole.Fixed -> if (nullable) "$serialType?" else serialType
                PropertyRole.Optional -> "$serialType?"
                PropertyRole.ThreeState -> readType
            }

    /** The Kotlin type a builder property is assigned: `null` only where it clears, or where it may be null anyway. */
    val builderType: String
        get() = if (nullable) "$type?" else type

    /** The private constructor parameter holding a three-state property. */
    val settable: String get() = "settable$index"

    /** The builder's storage for an optional property. */
    val slot: String get() = "slot$index"
}

/** Builds the [ClassSpec]s of one surface from the model. */
internal object ClassSpecs {
    private const val SETTABLE = "com.mountaya.client.Settable"

    /**
     * The class an object component, or an inline object, of an OpenAPI surface is printed as.
     * [threeStates] says a request sends it as JSON, so each of its properties that is not required
     * and is nullable has three states, as the model's own format says: left out, a value, and
     * `null`. That reaches every such object, a value object a merge body embeds included, and none
     * a response alone carries.
     */
    fun openApi(
        name: String,
        description: String?,
        properties: List<Property>,
        threeStates: Boolean,
        types: OpenApiTypes,
        union: String?,
    ): ClassSpec =
        checked(
            ClassSpec(
                name,
                types.namespace,
                description,
                properties.mapIndexed { index, property -> openApiProperty(property, index, threeStates, types) },
                union,
            ),
        )

    /**
     * The property [property] of an OpenAPI surface is printed as, at [index] in its class: fixed
     * where the contract fixes it, required where it requires it, three-state where [threeStates]
     * says a request sends its class and it may be null, and optional otherwise. Where a request sends
     * its class, each web address it holds is written by the serializer that refuses one with no scheme.
     */
    fun openApiProperty(
        property: Property,
        index: Int,
        threeStates: Boolean,
        types: OpenApiTypes,
    ): PropertySpec {
        val type = property.type
        val constant = (type as? ScalarType)?.constant
        val role =
            when {
                property.discriminatorValue != null || constant != null -> PropertyRole.Fixed
                property.required -> PropertyRole.Required
                type.nullable && threeStates -> PropertyRole.ThreeState
                else -> PropertyRole.Optional
            }
        val fixed =
            when {
                property.discriminatorValue != null -> discriminator(type, property.discriminatorValue, types)
                constant != null -> KotlinText.value(constant)
                else -> null
            }
        return PropertySpec(
            name = property.name,
            role = role,
            type = types.base(type),
            nullable = type.nullable,
            fixed = fixed,
            description = property.description,
            default = property.default,
            hasDefault = property.hasDefault,
            deprecated = property.deprecated,
            bytes = types.isBytes(type),
            index = index,
            readOnly = property.readOnly,
            writeOnly = property.writeOnly,
            serialType = if (threeStates) types.sentBase(type) else types.base(type),
            secret = property.secret,
        )
    }

    /** The class a Data API object type is printed as: every field may be absent, since a caller selects. */
    fun dataObject(
        type: DataType,
        types: DataTypes,
    ): ClassSpec =
        checked(
            ClassSpec(
                type.name,
                types.namespace,
                type.description,
                type.fields.mapIndexed { index, field ->
                    PropertySpec(
                        name = field.name,
                        role = PropertyRole.Optional,
                        type = types.base(field.type),
                        nullable = true,
                        fixed = null,
                        description = field.description,
                        default = null,
                        hasDefault = false,
                        deprecated = field.deprecated,
                        bytes = false,
                        index = index,
                    )
                },
            ),
        )

    /**
     * The class a Data API input type is printed as: required fields in the factory, the rest in the
     * builder, each web address written by the serializer that refuses one with no scheme, since a
     * request sends it.
     */
    fun dataInput(
        type: DataType,
        types: DataTypes,
    ): ClassSpec =
        checked(
            ClassSpec(
                type.name,
                types.namespace,
                type.description,
                type.fields.mapIndexed { index, field ->
                    PropertySpec(
                        name = field.name,
                        role = if (field.required) PropertyRole.Required else PropertyRole.Optional,
                        type = types.base(field.type),
                        nullable = field.type.nullable,
                        fixed = null,
                        description = field.description,
                        default = field.default,
                        hasDefault = field.hasDefault,
                        deprecated = field.deprecated,
                        bytes = false,
                        index = index,
                        serialType = types.sentBase(field.type),
                    )
                },
            ),
        )

    /** The Kotlin of a union member's discriminator value, as the type of its property spells it. */
    private fun discriminator(
        type: TypeNode,
        value: String,
        types: OpenApiTypes,
    ): String {
        if (type is RefType &&
            types.schema(type) is EnumSchema
        ) {
            return "${types.base(type)}(${KotlinText.literal(value)})"
        }
        if (type is ScalarType && type.kind == "string" && type.enumName == null) return KotlinText.literal(value)
        throw ModelException("${types.namespace}: a discriminator of a type that is neither a string nor a string enum")
    }

    /** Refuses a class whose names the printer's own would collide with. */
    fun checked(spec: ClassSpec): ClassSpec {
        val names = spec.properties.map { it.name }
        val reserved = setOf("optional", "given") + spec.properties.flatMap { listOf(it.settable, it.slot) }
        val duplicate = names.groupBy { it }.filterValues { it.size > 1 }.keys
        if (duplicate.isNotEmpty()) {
            throw ModelException(
                "${spec.qualified}: declares ${duplicate.joinToString()} twice",
            )
        }
        val clash = names.filter { it in reserved }
        if (clash.isNotEmpty()) {
            throw ModelException(
                "${spec.qualified}: a property named ${clash.joinToString()}, which the printer's own names would shadow",
            )
        }
        spec.properties.forEach { KotlinText.identifier(it.name) }
        return spec
    }

    /** The Kotlin type of the private storage of a three-state property, its web addresses marked as [PropertySpec.serialType] marks them. */
    fun settableType(property: PropertySpec): String = "$SETTABLE<${property.serialType}?>"

    /** The Kotlin of a three-state property nobody set. */
    const val ABSENT: String = "$SETTABLE.Absent"

    /** The value a payload stated, `null` included, which a three-state property that states null reads as. */
    const val STATED: String = "com.mountaya.client.Stated"

    /** The Kotlin of a three-state property set to [value]. */
    fun present(value: String): String = "$SETTABLE.Present($value)"
}
