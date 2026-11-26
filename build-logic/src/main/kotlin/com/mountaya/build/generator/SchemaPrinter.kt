package com.mountaya.build.generator

import com.mountaya.build.generator.KotlinText.kdoc

/**
 * Prints the declarations a namespace object holds: classes with their builders and factories,
 * open enums, unions, and aliases.
 *
 * Every class is plain, never a `data class`, so a property the contract adds changes no
 * constructor a caller calls. Its constructor is private, and a caller builds one through the
 * factory its companion declares: the required properties as parameters and the optional ones
 * set in a trailing builder, which is the only shape that survives a contract adding an
 * optional property without changing a call site.
 */
internal object SchemaPrinter {
    private const val SERIALIZABLE = "@kotlinx.serialization.Serializable"
    private const val WRITE_ONLY = "A builder is written, never read back."

    /** What a class holding a secret says of how it prints. */
    private const val SECRETS =
        "Its `toString` prints every property but its secrets, each printed as `<redacted>`, so a log never " +
            "carries one, and each secret stays readable."

    /** What a secret property says of how its class prints it. */
    private const val SECRET =
        "It is a secret: its class's `toString` prints it as `<redacted>`, and it stays readable here."

    /** The text a secret is printed as. */
    private const val REDACTED = "<redacted>"

    /** Prints [spec] at [indent]. */
    fun StringBuilder.printClass(
        spec: ClassSpec,
        indent: String,
    ) {
        val inner = "$indent    "
        kdoc(indent, listOf(spec.description, if (spec.properties.any { it.secret }) SECRETS else null))
        append(indent).append(SERIALIZABLE).append('\n')
        append(indent).append("public class ").append(spec.name).append(spec.declaredParameters)
        append(" private constructor(\n")
        for (property in spec.properties) printConstructorProperty(property, "$indent    ")
        append(indent).append(")")
        if (spec.union != null) {
            append(" : ")
                .append(spec.namespace)
                .append('.')
                .append(spec.union)
                .append("()")
        }
        append(" {\n")

        var first = true

        fun separate() {
            if (!first) append('\n')
            first = false
        }

        for (property in spec.properties.filter { it.role == PropertyRole.ThreeState }) {
            separate()
            kdoc(inner, propertyDoc(property))
            deprecation(inner, property.deprecated, property.description)
            append(inner).append("public val ").append(KotlinText.identifier(property.name)).append(": ")
            append(property.readType).append('\n')
            append(inner).append("    get() = ").append(property.settable)
            append(if (property.statesNull) ".stated\n" else ".valueOrNull\n")
        }

        val suppress = if (spec.properties.any { it.deprecated }) "$inner@kotlin.Suppress(\"DEPRECATION\")\n" else ""
        separate()
        append(suppress)
        printEquals(spec, inner)
        append('\n')
        append(suppress)
        printHashCode(spec, inner)
        append('\n')
        append(suppress)
        printToString(spec, inner)
        append('\n')
        printBuilder(spec, inner)
        append('\n')
        printFactory(spec, inner, suppress)
        append(indent).append("}\n")
    }

    private fun StringBuilder.printConstructorProperty(
        property: PropertySpec,
        indent: String,
    ) {
        val name = KotlinText.identifier(property.name)
        when (property.role) {
            PropertyRole.ThreeState -> {
                append(indent).append("@kotlinx.serialization.SerialName(").append(KotlinText.literal(property.name))
                append(")\n")
                append(indent).append("private val ").append(property.settable).append(": ")
                append(ClassSpecs.settableType(property))
                append(" = ").append(ClassSpecs.ABSENT).append(",\n")
            }

            PropertyRole.Fixed -> {
                kdoc(indent, propertyDoc(property))
                append(indent)
                append("@kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)\n")
                deprecation(indent, property.deprecated, property.description)
                append(indent)
                    .append("public val ")
                    .append(name)
                    .append(": ")
                    .append(property.readType)
                append(" = ").append(property.fixed).append(",\n")
            }

            PropertyRole.Required -> {
                kdoc(indent, propertyDoc(property))
                // A required property that may be null reads null where an answer leaves it out, as
                // `sdk-swift` reads it, and is always written, a null included, so a request still
                // carries it.
                if (property.nullable) {
                    append(indent)
                    append("@kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.ALWAYS)\n")
                }
                deprecation(indent, property.deprecated, property.description)
                append(indent)
                    .append("public val ")
                    .append(name)
                    .append(": ")
                    .append(property.serialReadType)
                append(if (property.nullable) " = null,\n" else ",\n")
            }

            PropertyRole.Optional -> {
                kdoc(indent, propertyDoc(property))
                deprecation(indent, property.deprecated, property.description)
                append(indent).append("public val ").append(name).append(": ")
                append(property.serialReadType).append(" = null,\n")
            }
        }
    }

    /** The value [property] is compared, hashed and shown by: the storage of a three-state one, the property otherwise. */
    private fun field(property: PropertySpec): String =
        if (property.role == PropertyRole.ThreeState) property.settable else KotlinText.identifier(property.name)

    private fun StringBuilder.printEquals(
        spec: ClassSpec,
        indent: String,
    ) {
        append(indent).append("override fun equals(other: kotlin.Any?): kotlin.Boolean =\n")
        append(indent).append("    other is ").append(spec.starred)
        for (property in spec.properties) {
            val field = field(property)
            append(" &&\n").append(indent).append("        ")
            if (property.bytes) {
                append("other.")
                    .append(field)
                    .append(".contentEquals(this.")
                    .append(field)
                    .append(")")
            } else {
                append("other.").append(field).append(" == this.").append(field)
            }
        }
        append('\n')
    }

    private fun StringBuilder.printHashCode(
        spec: ClassSpec,
        indent: String,
    ) {
        if (spec.properties.isEmpty()) {
            append(indent).append("override fun hashCode(): kotlin.Int = 0\n")
            return
        }
        append(indent).append("override fun hashCode(): kotlin.Int {\n")
        append(indent).append("    var hash = 0\n")
        for (property in spec.properties) {
            val field = "this.${field(property)}"
            val nullable = property.role != PropertyRole.ThreeState && property.readType.endsWith("?")
            val hash =
                when {
                    property.bytes && nullable -> "($field?.contentHashCode() ?: 0)"
                    property.bytes -> "$field.contentHashCode()"
                    nullable -> "($field?.hashCode() ?: 0)"
                    else -> "$field.hashCode()"
                }
            append(indent).append("    hash = 31 * hash + ").append(hash).append('\n')
        }
        append(indent).append("    return hash\n")
        append(indent).append("}\n")
    }

    private fun StringBuilder.printToString(
        spec: ClassSpec,
        indent: String,
    ) {
        append(indent).append("override fun toString(): kotlin.String =\n")
        append(indent).append("    ").append(KotlinText.literal("${spec.name}("))
        spec.properties.forEachIndexed { index, property ->
            val label = (if (index == 0) "" else ", ") + property.name + "="
            val size = if (property.readType.endsWith("?")) "?.size" else ".size"
            val value =
                when {
                    property.secret -> KotlinText.literal(REDACTED)
                    property.bytes -> "this.${field(property)}$size + \" bytes\""
                    property.role == PropertyRole.ThreeState -> "this.${KotlinText.identifier(property.name)}"
                    else -> "this.${field(property)}"
                }
            append(" +\n").append(indent).append("        ").append(KotlinText.literal(label))
            append(" + ").append(value)
        }
        append(" +\n")
            .append(indent)
            .append("        ")
            .append(KotlinText.literal(")"))
            .append('\n')
    }

    private fun StringBuilder.printBuilder(
        spec: ClassSpec,
        indent: String,
    ) {
        val settable = spec.properties.filter { it.role == PropertyRole.Optional || it.role == PropertyRole.ThreeState }
        kdoc(indent, listOf("Builder sets the optional properties of a [${spec.qualified}], each by assigning it."))
        append(indent).append("public class Builder").append(spec.declaredParameters)
        append(" internal constructor() {\n")
        val inner = "$indent    "
        for (property in settable) {
            if (property.role == PropertyRole.ThreeState) {
                append(inner).append("internal var ").append(property.slot).append(": ")
                append(ClassSpecs.settableType(property))
                append(" = ").append(ClassSpecs.ABSENT).append('\n')
            } else {
                append(inner).append("internal var ").append(property.slot).append(": ")
                append(property.type).append("? = null\n")
            }
        }
        for (property in settable) {
            append('\n')
            val doc = propertyDoc(property).toMutableList()
            if (property.role == PropertyRole.ThreeState && property.statesNull) {
                doc += "Left unset it states nothing; set to `null` it states that it held `null`."
            } else if (property.role == PropertyRole.ThreeState) {
                doc +=
                    "Left unset it is not sent and the stored value is untouched; set to `null` it is sent as `null`, which clears it."
            }
            kdoc(inner, doc)
            deprecation(inner, property.deprecated, property.description)
            val stored = if (property.role == PropertyRole.ThreeState) ClassSpecs.present("value") else "value"
            writeOnly(inner, KotlinText.identifier(property.name), property.builderType, property.slot, stored)
        }
        append(indent).append("}\n")
    }

    private fun StringBuilder.printFactory(
        spec: ClassSpec,
        indent: String,
        suppress: String,
    ) {
        val required = spec.properties.filter { it.role == PropertyRole.Required }
        kdoc(indent, listOf("Builds a [${spec.qualified}]."))
        append(indent).append("public companion object {\n")
        val inner = "$indent    "
        val doc =
            mutableListOf(
                "Returns a [${spec.qualified}] of the required properties given here and the optional ones [optional] sets.",
            )
        kdoc(inner, doc)
        append(suppress.replace(indent, inner))
        append(inner).append("public operator fun ")
        if (spec.typeParameters.isNotEmpty()) append(spec.declaredParameters).append(' ')
        append("invoke(\n")
        for (property in required) {
            append(inner).append("    ").append(KotlinText.identifier(property.name)).append(": ")
            append(property.readType).append(",\n")
        }
        val builder = "${spec.qualified}.Builder${spec.usedParameters}"
        append(inner).append("    optional: ").append(builder).append(".() -> kotlin.Unit = {},\n")
        append(inner)
            .append("): ")
            .append(spec.qualified)
            .append(spec.usedParameters)
            .append(" {\n")
        append(inner).append("    val given = ").append(builder).append("().apply(optional)\n")
        append(inner)
            .append("    return ")
            .append(spec.qualified)
            .append(spec.usedParameters)
            .append("(\n")
        for (property in spec.properties) {
            val name = KotlinText.identifier(property.name)
            val argument =
                when (property.role) {
                    PropertyRole.Required -> "$name = $name"
                    PropertyRole.Optional -> "$name = given.${property.slot}"
                    PropertyRole.ThreeState -> "${property.settable} = given.${property.slot}"
                    PropertyRole.Fixed -> continue
                }
            append(inner).append("        ").append(argument).append(",\n")
        }
        append(inner).append("    )\n")
        append(inner).append("}\n")
        append(indent).append("}\n")
    }

    /** Prints a write-only builder property named [name] of [type], storing [stored] into [slot]. */
    fun StringBuilder.writeOnly(
        indent: String,
        name: String,
        type: String,
        slot: String,
        stored: String,
    ) {
        append(indent)
            .append("public var ")
            .append(name)
            .append(": ")
            .append(type)
            .append('\n')
        append(indent).append("    @kotlin.Deprecated(").append(KotlinText.literal(WRITE_ONLY))
        append(", level = kotlin.DeprecationLevel.HIDDEN)\n")
        append(indent).append("    get() = throw kotlin.UnsupportedOperationException(")
        append(KotlinText.literal(WRITE_ONLY)).append(")\n")
        append(indent).append("    set(value) {\n")
        append(indent)
            .append("        ")
            .append(slot)
            .append(" = ")
            .append(stored)
            .append('\n')
        append(indent).append("    }\n")
    }

    /**
     * The documentation of a property: its description, the server's default where it states one,
     * and which way it travels where only one way does.
     */
    private fun propertyDoc(property: PropertySpec): List<String?> =
        listOf(
            property.description,
            if (property.hasDefault) {
                "Left out, the server applies its default: `${KotlinText.shown(property.default)}`."
            } else {
                null
            },
            when {
                property.readOnly -> "The server writes it, and a request never sends it."
                property.writeOnly -> "A request sends it, and the server never answers it."
                else -> null
            },
            if (property.secret) SECRET else null,
        )

    /** Prints `@Deprecated` with the description's own first sentence where the contract deprecates the declaration. */
    fun StringBuilder.deprecation(
        indent: String,
        deprecated: Boolean,
        description: String?,
    ) {
        if (!deprecated) return
        append(indent).append("@kotlin.Deprecated(").append(KotlinText.literal(KotlinText.firstSentence(description)))
        append(")\n")
    }

    /**
     * Prints an open enum named [name] in [namespace]: a value class over the wire's own type, with a
     * companion constant per member. A value this build has never seen decodes as itself. Two members
     * printed under one name, or one under a name the value class declares of its own, are refused.
     */
    fun StringBuilder.printEnum(
        namespace: String,
        name: String,
        description: String?,
        wireType: String,
        members: List<EnumConstant>,
        indent: String,
    ) {
        EnumMembers.check("$namespace.$name", members)
        val inner = "$indent    "
        kdoc(
            indent,
            listOf(
                description,
                "It is open: a value the contract adds after this build decodes as itself, and the constants below are the values it names today.",
            ),
        )
        append(indent).append(SERIALIZABLE).append('\n')
        append(indent).append("@kotlin.jvm.JvmInline\n")
        append(indent).append("public value class ").append(name).append("(\n")
        append(inner).append("/** The value, exactly as the wire carries it. */\n")
        append(inner).append("public val value: ").append(wireType).append(",\n")
        append(indent).append(") {\n")
        append(inner).append("override fun toString(): kotlin.String = ")
        append(if (wireType == "kotlin.String") "value" else "value.toString()").append('\n')
        append('\n')
        kdoc(inner, listOf("The values the contract names."))
        append(inner).append("public companion object {\n")
        members.forEachIndexed { index, member ->
            if (index > 0) append('\n')
            kdoc("$inner    ", listOf(member.description))
            deprecation("$inner    ", member.deprecated, member.description)
            append(inner)
                .append("    public val ")
                .append(KotlinText.identifier(member.name))
                .append(": ")
                .append(name)
            append(" = ")
                .append(name)
                .append("(")
                .append(member.value)
                .append(")\n")
        }
        append(inner).append("}\n")
        append(indent).append("}\n")
    }

    /**
     * One named value of an open enum: [name], what it is printed under, read off [source], the name the model gives
     * it, and [value], already Kotlin source.
     */
    class EnumConstant(
        val name: String,
        val source: String,
        val value: String,
        val description: String?,
        val deprecated: Boolean,
    )

    /**
     * Prints a discriminated union: an abstract class its members extend, and [Unknown] holding
     * what arrived where the discriminator names no member this build knows.
     */
    fun StringBuilder.printUnion(
        union: UnionSchema,
        types: OpenApiTypes,
        indent: String,
    ) {
        val qualified = "${types.namespace}.${union.name}"
        val inner = "$indent    "
        val unknown = "$qualified.Unknown"
        kdoc(
            indent,
            listOf(
                union.description,
                "Each member extends it and is told apart by `${union.discriminator}`. It is " +
                    "open: a member the contract adds " +
                    "after this build decodes as [Unknown], so a `when` over it ends in `else`.",
            ),
        )
        append(indent).append("@kotlinx.serialization.Serializable(with = ").append(qualified)
        append(".Serializer::class)\n")
        append(indent).append("public abstract class ").append(union.name).append(" internal constructor() {\n")
        kdoc(inner, listOf("Unknown is a member this build does not know, holding the object that arrived."))
        append(inner).append("public class Unknown(\n")
        append(inner).append("    /** The member as it arrived. */\n")
        append(inner).append("    public val json: ${SurfaceTypes.JSON_OBJECT},\n")
        append(inner).append(") : ").append(qualified).append("() {\n")
        append(inner).append("    override fun equals(other: kotlin.Any?): kotlin.Boolean = other is ").append(unknown)
        append(" && other.json == json\n\n")
        append(inner).append("    override fun hashCode(): kotlin.Int = json.hashCode()\n\n")
        append(inner).append("    override fun toString(): kotlin.String = \"Unknown(\" + json + \")\"\n")
        append(inner).append("}\n\n")
        append(inner).append("internal object Serializer : kotlinx.serialization.KSerializer<").append(qualified)
        append("> {\n")
        val body = "$inner    "
        append(body).append("override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =\n")
        append(body).append("    kotlinx.serialization.descriptors.buildClassSerialDescriptor(")
        append(KotlinText.literal("${types.packageName}.$qualified")).append(")\n\n")
        append(body).append("override fun serialize(\n")
        append(body).append("    encoder: kotlinx.serialization.encoding.Encoder,\n")
        append(body).append("    value: ").append(qualified).append(",\n")
        append(body).append(") {\n")
        append(body).append("    when (value) {\n")
        for (member in union.members) {
            val type = types.named(member.type)
            append(body).append("        is ").append(type).append(" -> encoder.encodeSerializableValue(")
            append(type).append(".serializer(), value)\n")
        }
        append(body).append("        is ").append(unknown).append(" -> encoder.encodeSerializableValue(")
        append(SurfaceTypes.JSON_OBJECT).append(".serializer(), value.json)\n")
        append(body).append("        else -> throw kotlinx.serialization.SerializationException(")
        append(KotlinText.literal("$qualified has no member of this class.")).append(")\n")
        append(body).append("    }\n")
        append(body).append("}\n\n")
        append(body).append("override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): ")
        append(qualified).append(" {\n")
        append(body).append("    val input =\n")
        append(body).append("        decoder as? kotlinx.serialization.json.JsonDecoder\n")
        append(body).append("            ?: throw kotlinx.serialization.SerializationException(")
        append(KotlinText.literal("$qualified is read from JSON only.")).append(")\n")
        append(body).append("    val json =\n")
        append(body).append("        input.decodeJsonElement() as? ").append(SurfaceTypes.JSON_OBJECT).append('\n')
        append(body).append("            ?: throw kotlinx.serialization.SerializationException(")
        append(KotlinText.literal("$qualified is a JSON object.")).append(")\n")
        append(body).append("    val discriminator = json[").append(KotlinText.literal(union.discriminator))
        append("] as? kotlinx.serialization.json.JsonPrimitive\n")
        append(body).append("    return when (if (discriminator != null && discriminator.isString) ")
        append("discriminator.content else null) {\n")
        for (member in union.members) {
            val type = types.named(member.type)
            append(body).append("        ").append(KotlinText.literal(member.value))
            append(" -> input.json.decodeFromJsonElement(")
            append(type).append(".serializer(), json)\n")
        }
        append(body).append("        else -> ").append(unknown).append("(json)\n")
        append(body).append("    }\n")
        append(body).append("}\n")
        append(inner).append("}\n")
        append(indent).append("}\n")
    }

    /**
     * Prints an inline union with no discriminator: an abstract class with one member class per
     * branch, read by trying each branch in the model's order, the first that decodes winning,
     * and [Unknown] where none does.
     */
    fun StringBuilder.printInlineUnion(
        union: InlineUnion,
        types: SurfaceTypes,
        indent: String,
    ) {
        val qualified = "${types.namespace}.${union.name}"
        val inner = "$indent    "
        val members = union.members.map { member -> inlineMemberName(member) to member }
        val duplicates = members.groupBy { it.first }.filterValues { it.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw ModelException(
                "$qualified: two branches would both be named ${duplicates.joinToString()}",
            )
        }
        kdoc(
            indent,
            listOf(
                "$qualified is one of several shapes the contract does not tell apart by a " +
                    "discriminator. Each branch is tried " +
                    "in the contract's order, the first that reads winning, and [Unknown] holds a value no branch reads.",
            ),
        )
        append(indent).append("@kotlinx.serialization.Serializable(with = ").append(qualified)
        append(".Serializer::class)\n")
        append(indent).append("public abstract class ").append(union.name).append(" internal constructor() {\n")
        for ((name, member) in members) {
            val type = types.kotlin(member)
            kdoc(inner, listOf("$name is the branch that is a `$type`."))
            append(inner).append("public class ").append(name).append("(\n")
            append(inner).append("    /** The value. */\n")
            append(inner).append("    public val value: ").append(type).append(",\n")
            append(inner).append(") : ").append(qualified).append("() {\n")
            append(inner).append("    override fun equals(other: kotlin.Any?): kotlin.Boolean = ")
            append("other is $qualified.$name && other.value == value\n\n")
            append(inner).append("    override fun hashCode(): kotlin.Int = value")
            append(if (member.nullable) "?.hashCode() ?: 0\n\n" else ".hashCode()\n\n")
            append(inner).append("    override fun toString(): kotlin.String = \"$name(\" + value + \")\"\n")
            append(inner).append("}\n\n")
        }
        kdoc(inner, listOf("Unknown is a value no branch reads, as it arrived."))
        append(inner).append("public class Unknown(\n")
        append(inner).append("    /** The value as it arrived. */\n")
        append(inner).append("    public val json: ${SurfaceTypes.JSON_ELEMENT},\n")
        append(inner).append(") : ").append(qualified).append("() {\n")
        append(inner).append("    override fun equals(other: kotlin.Any?): kotlin.Boolean = ")
        append("other is $qualified.Unknown && other.json == json\n\n")
        append(inner).append("    override fun hashCode(): kotlin.Int = json.hashCode()\n\n")
        append(inner).append("    override fun toString(): kotlin.String = \"Unknown(\" + json + \")\"\n")
        append(inner).append("}\n\n")
        append(inner).append("internal object Serializer : kotlinx.serialization.KSerializer<").append(qualified)
        append("> {\n")
        val body = "$inner    "
        append(body).append("override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =\n")
        append(body).append("    kotlinx.serialization.descriptors.buildClassSerialDescriptor(")
        append(KotlinText.literal("${types.packageName}.$qualified")).append(")\n\n")
        append(body).append("override fun serialize(\n")
        append(body).append("    encoder: kotlinx.serialization.encoding.Encoder,\n")
        append(body).append("    value: ").append(qualified).append(",\n")
        append(body).append(") {\n")
        append(body).append("    when (value) {\n")
        for ((name, member) in members) {
            append(body).append("        is $qualified.$name -> encoder.encodeSerializableValue(")
            append(types.serializer(member)).append(", value.value)\n")
        }
        append(body).append("        is $qualified.Unknown -> encoder.encodeSerializableValue(")
        append(SurfaceTypes.JSON_ELEMENT).append(".serializer(), value.json)\n")
        append(body).append("        else -> throw kotlinx.serialization.SerializationException(")
        append(KotlinText.literal("$qualified has no branch of this class.")).append(")\n")
        append(body).append("    }\n")
        append(body).append("}\n\n")
        append(body).append("override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): ")
        append(qualified).append(" {\n")
        append(body).append("    val input =\n")
        append(body).append("        decoder as? kotlinx.serialization.json.JsonDecoder\n")
        append(body).append("            ?: throw kotlinx.serialization.SerializationException(")
        append(KotlinText.literal("$qualified is read from JSON only.")).append(")\n")
        append(body).append("    val json = input.decodeJsonElement()\n")
        for ((name, member) in members) {
            append(body).append("    try {\n")
            append(body).append("        return $qualified.$name(input.json.decodeFromJsonElement(")
            append(types.serializer(member)).append(", json))\n")
            append(body).append("    } catch (unread: kotlin.IllegalArgumentException) {\n")
            append(body).append("    }\n")
        }
        append(body).append("    return $qualified.Unknown(json)\n")
        append(body).append("}\n")
        append(inner).append("}\n")
        append(indent).append("}\n")
    }

    /** The member class a branch of an inline union is printed as. */
    private fun inlineMemberName(member: TypeNode): String =
        when (member) {
            is RefType -> member.name
            is InlineObject -> member.name
            is InlineUnion -> member.name
            is ScalarType -> member.enumName ?: (KotlinText.capitalized(member.kind) + "Value")
            is ArrayType -> "ListValue"
            is MapType -> "MapValue"
            is AnyType -> "AnyValue"
            is NullType -> "NullValue"
        }

    /** Prints a type alias: an `alias` component, or a GeoJSON component naming the hand-written type. */
    fun StringBuilder.printAlias(
        name: String,
        description: String?,
        target: String,
        indent: String,
    ) {
        kdoc(indent, listOf(description))
        append(indent)
            .append("public typealias ")
            .append(name)
            .append(" = ")
            .append(target)
            .append('\n')
    }
}
