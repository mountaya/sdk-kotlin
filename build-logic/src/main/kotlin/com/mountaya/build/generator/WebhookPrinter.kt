package com.mountaya.build.generator

import com.mountaya.build.generator.KotlinText.kdoc
import com.mountaya.build.generator.SchemaPrinter.deprecation
import com.mountaya.build.generator.SchemaPrinter.printClass

/**
 * Prints the webhook event each public type of the REST surface's `webhooks` is delivered as: one
 * class extending `WebhookEvent` to a type, carrying the envelope's members, the object the type
 * carries at `data.object` and, at `data.previous_attributes`, the former value of each field its
 * change moved, and `Unknown` for an event a build cannot type.
 *
 * Every fact is checked against the model before a line is printed, and an entry this printer
 * cannot print as stated is refused by name: an envelope other than `WebhookEvent`, an envelope
 * whose `type` or `data` is not the shape every entry narrows, an object that names no object
 * component, a previous attribute that is no field of its object, a type listed twice or folded to
 * a name another class takes, and a set of types that differs from the members of the enum the
 * envelope's `type` names.
 */
internal class WebhookPrinter(
    private val model: OpenApiSurface,
    private val types: OpenApiTypes,
) {
    companion object {
        /** The one component a webhook event is delivered in, whose name the printed class takes in the package. */
        const val ENVELOPE = "WebhookEvent"

        /** The classes the printed class declares of its own, which no type's class may take. */
        private val OWN_CLASSES = setOf("Typed", "Unknown", "Payload", "PayloadData", "Serializer", "Companion")

        /** The members the printed class and its two kinds of event declare of their own, which no envelope member may take. */
        private val OWN_MEMBERS = setOf("payload", "event")

        /** The names the payload's type parameters take. */
        private val PARAMETERS = listOf("Object", "PreviousAttributes")

        /**
         * Returns the class a type is read as: its dots and underscores folded to UpperCamelCase, so
         * `organization.ai_provider_config.updated` is `OrganizationAiProviderConfigUpdated`.
         */
        fun className(type: String): String =
            type.split('.', '_').filter { it.isNotEmpty() }.joinToString("") { KotlinText.capitalized(it) }
    }

    /** One webhook entry, checked and named. */
    private class Entry(
        val webhook: Webhook,
        /** The class the type is read as. */
        val name: String,
        /** The class its previous attributes are printed as, or null where it names none. */
        val previous: String?,
        /** Each named field of its object, in the entry's order. */
        val fields: List<Property>,
    )

    /** The component the envelope's `data` names, and its two members. */
    private class DataComponent(
        val name: String,
        val objectMember: Property,
        val previous: Property,
        /**
         * Whether `object` is a union, which holds an object of a kind it cannot read as its
         * `Unknown`, rather than any value, which holds one as it is.
         */
        val objectIsUnion: Boolean,
    )

    private val namespace = types.namespace
    private val site = "$namespace.$ENVELOPE"

    /** A class's documentation, nested in the printed class, wrapped to its line. */
    private fun classDoc(text: String): String = KotlinText.wrapped(text, 100 - 4 - 3)

    /** A property's documentation, in a class nested in the printed class, wrapped to its line. */
    private fun propertyDoc(text: String): String = KotlinText.wrapped(text, 100 - 8 - 3)

    /** Appends a KDoc of the printer's own [paragraphs], each wrapped to a line of 100 columns at [indent]. */
    private fun StringBuilder.prose(
        indent: String,
        paragraphs: List<String>,
    ) = kdoc(indent, paragraphs.map { KotlinText.wrapped(it, 100 - indent.length - 3) })

    /** Prints `WebhookEvent.kt`'s body, or returns null where the surface delivers no webhook. */
    fun print(): String? {
        if (model.webhooks.isEmpty()) return null
        val envelope = envelopeProperties()
        val type = typeProperty(envelope)
        val data = dataComponent(envelope)
        val entries = checked(model.webhooks, type)
        if (!isString(type.type) && !isStringEnum(type.type)) {
            throw ModelException(
                "$site.type: neither a string nor an enum of strings, which a type a build does not know is held as",
            )
        }
        val members = envelope.filter { it.name != "type" && it.name != "data" }
        for (member in members) {
            if (member.name in OWN_MEMBERS) {
                throw ModelException("$site.${member.name}: read as a member an event holds of its own")
            }
        }
        return buildString { printEvent(entries, envelope, type, members, data) }
    }

    // Checks

    /** Returns the envelope component's properties, refusing an envelope that is no object component. */
    private fun envelopeProperties(): List<Property> {
        val schema = model.schemas.firstOrNull { it.name == ENVELOPE }
        if (schema !is ObjectSchema || schema.role == "envelope") {
            throw ModelException("$site: the webhook envelope names no object component")
        }
        return schema.properties
    }

    /** Returns the envelope's `type`, which must always be present: the class an event is read as is the one it names. */
    private fun typeProperty(envelope: List<Property>): Property {
        val type = envelope.firstOrNull { it.name == "type" }
        if (type == null || !type.required || type.type.nullable) {
            throw ModelException(
                "$site: an envelope with no required `type`, which names the class an event is read as",
            )
        }
        return type
    }

    /**
     * Returns the component the envelope's required `data` names, with its `object` and
     * `previous_attributes`, refusing a `data` of any other shape, since every entry narrows those two
     * members and nothing else, and refusing an `object` that is neither a union nor any value, or a
     * `previous_attributes` that is no map of any value, since an event this build cannot type holds
     * each as it arrived.
     */
    private fun dataComponent(envelope: List<Property>): DataComponent {
        val where = "$site.data"
        val data = envelope.firstOrNull { it.name == "data" }
        val reference = data?.type as? RefType
        val schema = reference?.let { ref -> model.schemas.firstOrNull { it.name == ref.name } }
        if (data == null || !data.required || data.type.nullable || reference == null || schema !is ObjectSchema) {
            throw ModelException("$where: an envelope whose required `data` names no object component")
        }
        val objectMember = schema.properties.firstOrNull { it.name == "object" }
        val previous = schema.properties.firstOrNull { it.name == "previous_attributes" }
        if (schema.properties.size != 2 || objectMember == null || !objectMember.required || previous == null ||
            previous.required
        ) {
            throw ModelException(
                "$where: `${schema.name}` is not a required `object` and an optional `previous_attributes`, " +
                    "which are what an entry narrows",
            )
        }
        val objectType = objectMember.type
        val objectIsUnion =
            when {
                objectType is RefType && model.schemas.firstOrNull { it.name == objectType.name } is UnionSchema -> true

                objectType is AnyType -> false

                else -> throw ModelException(
                    "$where.object: neither a union nor any value, so an object of a kind this build does not read " +
                        "cannot be held as it arrived",
                )
            }
        val previousType = previous.type
        if (previousType !is MapType || previousType.keys != null || previousType.values !is AnyType) {
            throw ModelException(
                "$where.previous_attributes: no map of any value, so the attributes of an event this build cannot " +
                    "type cannot be held as they arrived",
            )
        }
        return DataComponent(schema.name, objectMember, previous, objectIsUnion)
    }

    /** Checks every entry against the model, and names it. */
    private fun checked(
        webhooks: List<Webhook>,
        type: Property,
    ): List<Entry> {
        val entries = mutableListOf<Entry>()
        val listed = mutableSetOf<String>()
        val names = mutableMapOf<String, String>()
        for (webhook in webhooks) {
            val where = "rest.webhooks.${webhook.type}"
            if (webhook.envelope != ENVELOPE) {
                throw ModelException(
                    "$where: delivered in `${webhook.envelope}`, and this printer prints the `$ENVELOPE` envelope alone",
                )
            }
            val objectSchema =
                model.schemas.firstOrNull { it.name == webhook.objectName } as? ObjectSchema
                    ?: throw ModelException("$where: `${webhook.objectName}` names no object component")
            val fields =
                webhook.previousAttributes.map { field ->
                    objectSchema.properties.firstOrNull { it.name == field }
                        ?: throw ModelException(
                            "$where: the previous attribute `$field` is no field of `${webhook.objectName}`",
                        )
                }
            if (webhook.previousAttributes.toSet().size != fields.size) {
                throw ModelException("$where: a previous attribute named twice")
            }
            if (!listed.add(webhook.type)) throw ModelException("$where: a type listed twice")
            val name = className(webhook.type)
            if (!KotlinText.isPlain(name)) throw ModelException("$where: no class can be named `$name`")
            names[name]?.let { other -> throw ModelException("$where: read as `$name`, the class `$other` is read as") }
            names[name] = webhook.type
            if (name in OWN_CLASSES) {
                throw ModelException("$where: read as `$name`, a class the event declares of its own")
            }
            val previous = if (fields.isEmpty()) null else "${name}PreviousAttributes"
            entries += Entry(webhook, name, previous, fields)
        }
        val taken = names.keys + OWN_CLASSES
        for (entry in entries) {
            val previous = entry.previous ?: continue
            if (previous in taken || entries.count { it.previous == previous } > 1) {
                throw ModelException(
                    "rest.webhooks.${entry.webhook.type}: its previous attributes cannot be named `$previous`",
                )
            }
        }
        checkTypes(listed, type)
        return entries
    }

    /**
     * Refuses a set of types that differs from the members of the enum the envelope's `type` names,
     * so a type with no entry is never read past, and an entry no type names is never printed.
     */
    private fun checkTypes(
        listed: Set<String>,
        type: Property,
    ) {
        val reference = type.type as? RefType ?: return
        val schema = model.schemas.firstOrNull { it.name == reference.name } as? EnumSchema ?: return
        val declared = schema.members.mapNotNull { it.value as? String }.toSet()
        val unlisted = (declared - listed).sorted()
        val undeclared = (listed - declared).sorted()
        if (unlisted.isEmpty() && undeclared.isEmpty()) return
        throw ModelException(
            "rest.webhooks and `$namespace.${schema.name}` disagree: " +
                (if (unlisted.isEmpty()) "" else "no webhook for ${unlisted.joinToString(", ")}; ") +
                (if (undeclared.isEmpty()) "" else "no member for ${undeclared.joinToString(", ")}; ") +
                "each type is one webhook and one member",
        )
    }

    private fun isString(type: TypeNode): Boolean =
        type is ScalarType && type.kind == "string" && type.format == null && type.enumName == null &&
            type.constant == null

    private fun isStringEnum(type: TypeNode): Boolean {
        val reference = type as? RefType ?: return false
        val schema = model.schemas.firstOrNull { it.name == reference.name }
        return schema is EnumSchema && schema.valueType == "string"
    }

    // The class

    private fun StringBuilder.printEvent(
        entries: List<Entry>,
        envelope: List<Property>,
        type: Property,
        members: List<Property>,
        data: DataComponent,
    ) {
        val typeKotlin = types.kotlin(type.type)
        prose(
            "",
            listOf(
                "WebhookEvent is a webhook event as Mountaya delivers it to an endpoint, typed by its `type`: one " +
                    "class to each public type the contract's `webhooks` lists, carrying every member of the " +
                    "envelope, the object the type carries at `data.object` and, at `data.previous_attributes`, the " +
                    "former value of each field its change moved.",
                "An event this build cannot type is [Unknown], holding the contract's envelope, " +
                    "`$namespace.$ENVELOPE`: one of a type it does not know, which a later contract adds, or one " +
                    "whose payload its types do not read. It is delivered rather than refused, so a receiver " +
                    "still acknowledges it and deduplicates it by its `id`, and every member of the envelope is " +
                    "read off every event alike. A type the contract adds is one more class, so a `when` over an " +
                    "event ends in `else`.",
            ),
        )
        append("@kotlinx.serialization.Serializable(with = WebhookEvent.Serializer::class)\n")
        append("public abstract class WebhookEvent internal constructor() {\n")
        val inner = "    "
        kdoc(inner, listOf("The event's type, as the wire spells it."))
        append(inner).append("public abstract val type: ").append(typeKotlin).append('\n')
        for (member in members) {
            append('\n')
            kdoc(inner, listOf(member.description ?: "The envelope's `${member.name}`."))
            deprecation(inner, member.deprecated, member.description)
            append(inner).append("public abstract val ").append(KotlinText.identifier(member.name)).append(": ")
            append(accessorType(member)).append('\n')
        }
        append('\n')
        printTyped(members, envelope.first { it.name == "data" }, inner)
        for (entry in entries) {
            append('\n')
            printEntry(entry, type, inner)
        }
        append('\n')
        printUnknown(members, type, inner)
        append('\n')
        printClass(payloadSpec(envelope), inner)
        append('\n')
        printClass(payloadDataSpec(data), inner)
        for (entry in entries) {
            val spec = previousSpec(entry) ?: continue
            append('\n')
            printClass(spec, inner)
        }
        append('\n')
        printSerializer(entries, envelope, type, members, data, inner)
        append("}\n")
    }

    /** The Kotlin type an envelope member is read as on every event. */
    private fun accessorType(member: Property): String =
        types.base(member.type) + if (member.type.nullable || !member.required) "?" else ""

    /** Prints the intermediate class every event of a type this build knows extends. */
    private fun StringBuilder.printTyped(
        members: List<Property>,
        data: Property,
        indent: String,
    ) {
        val inner = "$indent    "
        prose(
            indent,
            listOf(
                "Typed is an event of a type this build knows: every member of the contract's envelope, " +
                    "`$namespace.$ENVELOPE`, but its `type`, which its class names, held in its [payload], with the " +
                    "[data] that type carries.",
                "`Object` is the object the type carries at `data.object`, and `PreviousAttributes` the former " +
                    "value of each field its change moved, `Nothing` for a type whose change moves none.",
            ),
        )
        append(indent).append("public abstract class Typed<Object : kotlin.Any, PreviousAttributes : kotlin.Any> ")
        append("internal constructor(\n")
        append(inner).append("/** The event as it was delivered, but its `type`, which its class names. */\n")
        append(inner).append("public val payload: WebhookEvent.Payload<Object, PreviousAttributes>,\n")
        append(indent).append(") : WebhookEvent() {\n")
        kdoc(inner, listOf(data.description ?: "The contract's `data`."))
        append(inner).append("public val data: WebhookEvent.PayloadData<Object, PreviousAttributes>\n")
        append(inner).append("    get() = payload.data\n")
        for (member in members) {
            append('\n')
            printOverride(member, "payload", inner)
        }
        append('\n')
        append(inner).append("override fun equals(other: kotlin.Any?): kotlin.Boolean =\n")
        append(
            inner,
        ).append("    other is WebhookEvent.Typed<*, *> && other.type == this.type && other.payload == this.payload\n")
        append('\n')
        append(
            inner,
        ).append("override fun hashCode(): kotlin.Int = 31 * this.type.hashCode() + this.payload.hashCode()\n")
        append('\n')
        append(inner).append("override fun toString(): kotlin.String =\n")
        append(inner).append("    \"WebhookEvent(type=\" + this.type + \", payload=\" + this.payload + \")\"\n")
        append(indent).append("}\n")
    }

    /** Prints the override of [member] that reads it off [holder]. */
    private fun StringBuilder.printOverride(
        member: Property,
        holder: String,
        indent: String,
    ) {
        deprecation(indent, member.deprecated, member.description)
        if (member.deprecated) append(indent).append("@kotlin.Suppress(\"DEPRECATION\")\n")
        val name = KotlinText.identifier(member.name)
        append(indent)
            .append("override val ")
            .append(name)
            .append(": ")
            .append(accessorType(member))
            .append('\n')
        append(indent)
            .append("    get() = ")
            .append(holder)
            .append('.')
            .append(name)
            .append('\n')
    }

    /** Prints the class one type is read as. */
    private fun StringBuilder.printEntry(
        entry: Entry,
        type: Property,
        indent: String,
    ) {
        val inner = "$indent    "
        val summary =
            entry.webhook.summary
                ?.replace(Regex("\\s+"), " ")
                ?.trim()
        prose(indent, listOf("`${entry.webhook.type}`" + if (summary == null) "." else ": $summary"))
        val parameters = "${types.named(entry.webhook.objectName)}, ${entry.previous?.let {
            "WebhookEvent.$it"
        } ?: "kotlin.Nothing"}"
        append(indent).append("public class ").append(entry.name).append("(\n")
        append(inner).append("payload: WebhookEvent.Payload<").append(parameters).append(">,\n")
        append(indent).append(") : WebhookEvent.Typed<").append(parameters).append(">(payload) {\n")
        append(inner).append("override val type: ").append(types.kotlin(type.type)).append('\n')
        val literal = KotlinText.literal(entry.webhook.type)
        append(inner).append("    get() = ")
        append(if (isString(type.type)) literal else "${types.base(type.type)}($literal)").append('\n')
        append(indent).append("}\n")
    }

    /** Prints the class an event this build cannot type is read as. */
    private fun StringBuilder.printUnknown(
        members: List<Property>,
        type: Property,
        indent: String,
    ) {
        val inner = "$indent    "
        prose(
            indent,
            listOf(
                "Unknown is an event this build cannot type: a type it does not know, or a payload its types do " +
                    "not read, as the envelope holds it.",
            ),
        )
        append(indent).append("public class Unknown(\n")
        append(inner).append("/** The event as the contract's envelope holds it. */\n")
        append(inner).append("public val event: ").append(types.named(ENVELOPE)).append(",\n")
        append(indent).append(") : WebhookEvent() {\n")
        append(inner).append("override val type: ").append(types.kotlin(type.type)).append('\n')
        append(inner).append("    get() = event.type\n")
        for (member in members) {
            append('\n')
            printOverride(member, "event", inner)
        }
        append('\n')
        append(inner).append("override fun equals(other: kotlin.Any?): kotlin.Boolean =\n")
        append(inner).append("    other is WebhookEvent.Unknown && other.event == this.event\n")
        append('\n')
        append(inner).append("override fun hashCode(): kotlin.Int = this.event.hashCode()\n")
        append('\n')
        append(inner).append("override fun toString(): kotlin.String = \"Unknown(\" + this.event + \")\"\n")
        append(indent).append("}\n")
    }

    /** The payload: every member of the envelope but its `type`, its `data` the generic one. */
    private fun payloadSpec(envelope: List<Property>): ClassSpec {
        val properties =
            envelope.filter { it.name != "type" }.mapIndexed { index, property ->
                val spec = ClassSpecs.openApiProperty(property, index, threeStates = false, types)
                if (property.name != "data") {
                    spec
                } else {
                    PropertySpec(
                        name = "data",
                        role = PropertyRole.Required,
                        type = "WebhookEvent.PayloadData<Object, PreviousAttributes>",
                        nullable = false,
                        fixed = null,
                        description = property.description ?: "The contract's `data`.",
                        default = null,
                        hasDefault = false,
                        deprecated = property.deprecated,
                        bytes = false,
                        index = index,
                    )
                }
            }
        return ClassSpecs.checked(
            ClassSpec(
                "Payload",
                "WebhookEvent",
                classDoc(
                    "Payload is an event of a type this build knows, as it was delivered: every member of the " +
                        "contract's envelope, `$namespace.$ENVELOPE`, but its `type`, which the event's class names, " +
                        "with the `data` that type carries.\n\n`Object` is the object the type carries at " +
                        "`data.object`, and `PreviousAttributes` the former value of each field its change moved, " +
                        "`Nothing` for a type whose change moves none.",
                ),
                properties,
                typeParameters = PARAMETERS,
            ),
        )
    }

    /** What an event of a type this build knows says about its object. */
    private fun payloadDataSpec(data: DataComponent): ClassSpec =
        ClassSpecs.checked(
            ClassSpec(
                "PayloadData",
                "WebhookEvent",
                classDoc(
                    "PayloadData is what an event of a type this build knows says about its object: the object " +
                        "as the type carries it, and the former value of each field its change moved.",
                ),
                listOf(
                    PropertySpec(
                        name = "object",
                        role = PropertyRole.Required,
                        type = "Object",
                        nullable = false,
                        fixed = null,
                        description = data.objectMember.description ?: "The contract's `object`.",
                        default = null,
                        hasDefault = false,
                        deprecated = false,
                        bytes = false,
                        index = 0,
                    ),
                    PropertySpec(
                        name = "previous_attributes",
                        role = PropertyRole.Optional,
                        type = "PreviousAttributes",
                        nullable = false,
                        fixed = null,
                        description =
                            propertyDoc(
                                (data.previous.description ?: "The contract's `previous_attributes`.") +
                                    "\n\nIt is always null where `PreviousAttributes` is `Nothing`, a type whose " +
                                    "change moves no field: a `previous_attributes` such an event carries is not " +
                                    "read, as a key this build does not know is not.",
                            ),
                        default = null,
                        hasDefault = false,
                        deprecated = false,
                        bytes = false,
                        index = 1,
                    ),
                ),
                typeParameters = PARAMETERS,
            ),
        )

    /** The former value of each field an entry's change moved, or null where it names none. */
    private fun previousSpec(entry: Entry): ClassSpec? {
        val name = entry.previous ?: return null
        val properties =
            entry.fields.mapIndexed { index, field ->
                val nullable = field.type.nullable
                PropertySpec(
                    name = field.name,
                    role = if (nullable) PropertyRole.ThreeState else PropertyRole.Optional,
                    type = types.base(field.type),
                    nullable = nullable,
                    fixed = null,
                    description =
                        propertyDoc(
                            (field.description ?: "The contract's `${field.name}`.") +
                                if (nullable) {
                                    "\n\nThe value it held before the change, a `Stated` holding null where it held " +
                                        "null, and null where the change did not move it."
                                } else {
                                    "\n\nThe value it held before the change, and null where the change did not move it."
                                },
                        ),
                    default = null,
                    hasDefault = false,
                    deprecated = field.deprecated,
                    bytes = types.isBytes(field.type),
                    index = index,
                    statesNull = nullable,
                )
            }
        return ClassSpecs.checked(
            ClassSpec(
                name,
                "WebhookEvent",
                classDoc(
                    "$name is the former value of each field a `${entry.webhook.type}` event's change moved, as " +
                        "its `data.previous_attributes` carries them, each typed as " +
                        "`$namespace.${entry.webhook.objectName}` types it: a field the change did not move is null.",
                ),
                properties,
            ),
        )
    }

    // The serializer

    private fun StringBuilder.printSerializer(
        entries: List<Entry>,
        envelope: List<Property>,
        type: Property,
        members: List<Property>,
        data: DataComponent,
        indent: String,
    ) {
        val inner = "$indent    "
        val body = "$inner    "
        val json = "kotlinx.serialization.json"
        val failure = "kotlinx.serialization.SerializationException"
        prose(
            indent,
            listOf(
                "Serializer reads an event as the class its `type` names, or as [Unknown] for a type this build " +
                    "does not know and for a payload the class's types do not read, and writes it as the envelope " +
                    "it was delivered in.",
            ),
        )
        append(indent).append("internal object Serializer : kotlinx.serialization.KSerializer<WebhookEvent> {\n")
        prose(
            inner,
            listOf("The envelope's members in the order the contract declares them, which an event is written in."),
        )
        append(inner).append("private val order: kotlin.collections.List<kotlin.String> =\n")
        append(inner).append("    kotlin.collections.listOf(")
        append(envelope.joinToString(", ") { KotlinText.literal(it.name) }).append(")\n\n")
        append(inner).append("override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =\n")
        append(inner).append("    kotlinx.serialization.descriptors.buildClassSerialDescriptor(")
        append(KotlinText.literal("${types.packageName}.WebhookEvent")).append(")\n\n")

        // Writing.
        append(inner).append("override fun serialize(\n")
        append(body).append("encoder: kotlinx.serialization.encoding.Encoder,\n")
        append(body).append("value: WebhookEvent,\n")
        append(inner).append(") {\n")
        append(body).append("val output =\n")
        append(body).append("    encoder as? $json.JsonEncoder\n")
        append(
            body,
        ).append("        ?: throw $failure(").append(KotlinText.literal("WebhookEvent is written to JSON only."))
        append(")\n")
        append(body).append("val written =\n")
        append(body).append("    when (value) {\n")
        for (entry in entries) {
            append(body).append("        is WebhookEvent.").append(entry.name).append(" ->\n")
            append(body).append("            output.json.encodeToJsonElement(").append(payloadSerializer(entry))
            append(", value.payload)\n")
        }
        append(body).append("        is WebhookEvent.Unknown -> {\n")
        append(body).append("            output.encodeJsonElement(output.json.encodeToJsonElement(")
        append(types.named(ENVELOPE)).append(".serializer(), value.event))\n")
        append(body).append("            return\n")
        append(body).append("        }\n")
        append(body).append("        else -> throw $failure(")
        append(KotlinText.literal("WebhookEvent has no class of this kind.")).append(")\n")
        append(body).append("    }\n")
        append(body).append("val members =\n")
        append(body).append("    written as? $json.JsonObject\n")
        append(
            body,
        ).append(
            "        ?: throw $failure(",
        ).append(KotlinText.literal("A webhook event is written as a JSON object."))
        append(")\n")
        append(body).append("val type = output.json.encodeToJsonElement(").append(types.serializer(type.type))
        append(", value.type)\n")
        append(body).append("output.encodeJsonElement(\n")
        append(body).append("    $json.JsonObject(\n")
        append(body).append("        kotlin.collections.buildMap<kotlin.String, $json.JsonElement> {\n")
        append(body).append("            for (name in order) {\n")
        append(
            body,
        ).append("                if (name == \"type\") put(name, type) else members[name]?.let { put(name, it) }\n")
        append(body).append("            }\n")
        append(body).append("        },\n")
        append(body).append("    ),\n")
        append(body).append(")\n")
        append(inner).append("}\n\n")

        // Reading.
        append(
            inner,
        ).append("override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): WebhookEvent {\n")
        append(body).append("val input =\n")
        append(body).append("    decoder as? $json.JsonDecoder\n")
        append(
            body,
        ).append("        ?: throw $failure(").append(KotlinText.literal("WebhookEvent is read from JSON only."))
        append(")\n")
        append(body).append("val json =\n")
        append(body).append("    input.decodeJsonElement() as? $json.JsonObject\n")
        append(
            body,
        ).append("        ?: throw $failure(").append(KotlinText.literal("A webhook event is a JSON object."))
        append(")\n")
        append(body).append("val type =\n")
        append(body).append("    (json[\"type\"] as? $json.JsonPrimitive)?.takeIf { it.isString }?.content\n")
        append(body).append("        ?: throw $failure(")
        append(KotlinText.literal("A webhook event names its type in a string `type`.")).append(")\n")
        append(body).append("val typed =\n")
        append(body).append("    when (type) {\n")
        for (entry in entries) {
            val read = if (entry.previous == null) "unread(json)" else "json"
            append(body).append("        ").append(KotlinText.literal(entry.webhook.type)).append(" ->\n")
            append(body).append("            readOrNull {\n")
            append(body).append("                WebhookEvent.").append(entry.name).append("(\n")
            append(body).append("                    input.json.decodeFromJsonElement(")
            append(payloadSerializer(entry)).append(", ").append(read).append("),\n")
            append(body).append("                )\n")
            append(body).append("            }\n")
        }
        append(body).append("        else -> null\n")
        append(body).append("    }\n")
        append(body).append("return typed ?: WebhookEvent.Unknown(untyped(input, json, type))\n")
        append(inner).append("}\n\n")

        prose(
            inner,
            listOf("Returns the event [read] reads, or null where its payload does not read as its types say."),
        )
        append(inner).append("private inline fun readOrNull(read: () -> WebhookEvent): WebhookEvent? =\n")
        append(inner).append("    try {\n")
        append(inner).append("        read()\n")
        append(inner).append("    } catch (unread: kotlin.IllegalArgumentException) {\n")
        append(inner).append("        null\n")
        append(inner).append("    }\n\n")

        prose(
            inner,
            listOf(
                "Returns [event] without its `data.previous_attributes`, which a type whose change moves no field " +
                    "never reads.",
            ),
        )
        append(inner).append("private fun unread(event: $json.JsonObject): $json.JsonObject {\n")
        append(body).append("val data = event[\"data\"] as? $json.JsonObject ?: return event\n")
        append(body).append("if (\"previous_attributes\" !in data) return event\n")
        append(
            body,
        ).append("return $json.JsonObject(event + (\"data\" to $json.JsonObject(data - \"previous_attributes\")))\n")
        append(inner).append("}\n\n")

        printUntyped(envelope, type, data, inner)
        append('\n')
        prose(
            inner,
            listOf(
                "Unread is what a type whose change moves no field reads its previous attributes with: nothing, " +
                    "since they are never read.",
            ),
        )
        append(inner).append("private object Unread : kotlinx.serialization.KSerializer<kotlin.Nothing> {\n")
        append(body).append("override val descriptor: kotlinx.serialization.descriptors.SerialDescriptor =\n")
        append(body).append("    kotlinx.serialization.descriptors.buildClassSerialDescriptor(\"kotlin.Nothing\")\n\n")
        append(body).append("override fun serialize(\n")
        append(body).append("    encoder: kotlinx.serialization.encoding.Encoder,\n")
        append(body).append("    value: kotlin.Nothing,\n")
        append(body).append(") {\n")
        append(body).append("}\n\n")
        append(
            body,
        ).append("override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): kotlin.Nothing =\n")
        append(body).append("    throw $failure(")
        append(KotlinText.literal("A type whose change moves no field reads no previous attributes.")).append(")\n")
        append(inner).append("}\n")
        append(indent).append("}\n")
    }

    /** The serializer of an entry's payload. */
    private fun payloadSerializer(entry: Entry): String =
        "WebhookEvent.Payload.serializer(${types.named(entry.webhook.objectName)}.serializer(), " +
            (entry.previous?.let { "WebhookEvent.$it.serializer()" } ?: "Unread") + ")"

    /**
     * Prints the reading of an event this build cannot type: the envelope as its own types read it,
     * or, where they do not read its object either, the envelope's members as a payload reads them,
     * its object and its previous attributes held as they arrived.
     */
    private fun StringBuilder.printUntyped(
        envelope: List<Property>,
        type: Property,
        data: DataComponent,
        indent: String,
    ) {
        val inner = "$indent    "
        val json = "kotlinx.serialization.json"
        val qualified = types.named(ENVELOPE)
        prose(
            indent,
            listOf(
                "Returns an event this build cannot type as the envelope: as the envelope's own types read it, or, " +
                    "where they do not read its object either, with its object and its previous attributes as they " +
                    "arrived. It throws where the body holds no envelope at all.",
            ),
        )
        if (envelope.any { it.deprecated }) append(indent).append("@kotlin.Suppress(\"DEPRECATION\")\n")
        append(indent).append("private fun untyped(\n")
        append(inner).append("input: $json.JsonDecoder,\n")
        append(inner).append("json: $json.JsonObject,\n")
        append(inner).append("type: kotlin.String,\n")
        append(indent).append("): $qualified {\n")
        append(inner).append("try {\n")
        append(inner).append("    return input.json.decodeFromJsonElement($qualified.serializer(), json)\n")
        append(inner).append("} catch (unread: kotlin.IllegalArgumentException) {\n")
        append(inner).append("}\n")
        append(inner).append("val payload =\n")
        append(inner).append("    input.json.decodeFromJsonElement(\n")
        append(inner).append("        WebhookEvent.Payload.serializer(\n")
        append(inner).append("            $json.JsonElement.serializer(),\n")
        append(inner).append("            kotlinx.serialization.builtins.MapSerializer(")
        append("kotlinx.serialization.serializer<kotlin.String>(), $json.JsonElement.serializer()),\n")
        append(inner).append("        ),\n")
        append(inner).append("        json,\n")
        append(inner).append("    )\n")
        val objectValue =
            if (data.objectIsUnion) {
                val union = (data.objectMember.type as RefType).name
                append(inner).append("val arrived =\n")
                append(inner).append("    payload.data.`object` as? $json.JsonObject\n")
                append(inner).append("        ?: throw kotlinx.serialization.SerializationException(")
                append(KotlinText.literal("A webhook event's object is a JSON object.")).append(")\n")
                "${types.named(union)}.Unknown(arrived)"
            } else {
                "payload.data.`object`"
            }
        val previous = KotlinText.identifier(data.previous.name)
        append(inner).append("val data =\n")
        append(inner)
            .append("    ")
            .append(types.named(data.name))
            .append("(`object` = ")
            .append(objectValue)
        append(") {\n")
        append(
            inner,
        ).append("        payload.data.previous_attributes?.let { this.").append(previous).append(" = it }\n")
        append(inner).append("    }\n")
        val required = mutableListOf<String>()
        val optional = mutableListOf<String>()
        for (property in envelope) {
            val name = KotlinText.identifier(property.name)
            val constant = (property.type as? ScalarType)?.constant
            if (property.discriminatorValue != null || constant != null) continue
            val value =
                when (property.name) {
                    "type" -> if (isString(type.type)) "type" else "${types.base(type.type)}(type)"
                    "data" -> "data"
                    else -> "payload.$name"
                }
            if (property.required) {
                required += "$name = $value"
            } else {
                optional += "$value?.let { this.$name = it }"
            }
        }
        append(inner).append("return $qualified(\n")
        for (argument in required) append(inner).append("    ").append(argument).append(",\n")
        if (optional.isEmpty()) {
            append(inner).append(")\n")
        } else {
            append(inner).append(") {\n")
            for (assignment in optional) append(inner).append("    ").append(assignment).append('\n')
            append(inner).append("}\n")
        }
        append(indent).append("}\n")
    }
}
