package com.mountaya.client

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * Envelope reads a success: a REST or Tile answer's envelope, and a Data API answer's field.
 *
 * A body is read in one pass over its text, straight into the types it is read with, and is never
 * built as a tree of JSON first: only the small members a failure and a page are read from,
 * `errors` and `extensions`, are kept as JSON. A member the envelope does not know is skipped, as the
 * client's `Json` skips one.
 */
internal object Envelope {
    /**
     * Reads an envelope's `data` with [data] and its `extensions.metadata` with [metadata], or
     * throws a decoder's failure where the body is no envelope of those types.
     *
     * The envelope comes back whole, a `data` the contract types as null included. Metadata the
     * envelope leaves out is null where [metadata] reads a null, which is what an operation that
     * declares none passes, and a decoding failure where the operation declares it.
     */
    fun <Value, Metadata> rest(
        body: ByteArray,
        data: KSerializer<Value>,
        metadata: KSerializer<Metadata>,
    ): Pair<Value, Metadata> {
        val read = wireJson.decodeFromString(RestReader(data), body.decodeToString())
        val found = objectOrNone(read.extensions, "extensions")?.get("metadata")
        val value =
            when {
                found != null -> {
                    wireJson.decodeFromJsonElement(metadata, found)
                }

                metadata.descriptor.isNullable -> {
                    wireJson.decodeFromJsonElement(metadata, JsonNull)
                }

                else -> {
                    throw SerializationException(
                        "The operation declares `extensions.metadata`, and the answer carries none.",
                    )
                }
            }
        return read.data to value
    }

    /** DataAnswer is what a Data API answer holds: what its body says of failure, and the operation's own field. */
    class DataAnswer<Result>(
        /** What the body says of failure. */
        val envelope: ErrorEnvelope,
        /** The operation's field, or null where `data` is null or holds no such field. */
        val result: Result?,
    )

    /** Reads a Data API answer for the field [operation] names with [result], or throws a decoder's failure. */
    fun <Result : Any> data(
        body: ByteArray,
        operation: String,
        result: KSerializer<Result>,
    ): DataAnswer<Result> = wireJson.decodeFromString(DataReader(operation, result), body.decodeToString())

    /** [value] as an object, or null where it is absent or null; any other value does not decode. */
    private fun objectOrNone(
        value: JsonElement?,
        member: String,
    ): JsonObject? =
        when (value) {
            null, is JsonNull -> null
            is JsonObject -> value
            else -> throw SerializationException("The answer's `$member` is no object.")
        }

    /** The index of an envelope's `data` in each reader's descriptor. */
    private const val DATA = 0

    /** The index of an envelope's `extensions` in each reader's descriptor. */
    private const val EXTENSIONS = 1

    /** The index of a Data API answer's `errors` in its reader's descriptor. */
    private const val ERRORS = 2

    /** The index of the operation's own field in the reader of a Data API answer's `data`. */
    private const val FIELD = 0

    /** What the members a failure and a page are read from are kept as: JSON, as sent. */
    private val json: KSerializer<JsonElement> = JsonElement.serializer()

    /** A value read, kept apart from the null a member that never arrived leaves. */
    private class Held<Value>(
        val value: Value,
    )

    /** What a REST or Tile envelope holds: its `data`, read with its type, and its `extensions` as sent. */
    private class RestRead<Value>(
        val data: Value,
        val extensions: JsonElement?,
    )

    /** Reads a REST or Tile envelope, its `data` with [data], refusing one that carries no `data`. */
    private class RestReader<Value>(
        private val data: KSerializer<Value>,
    ) : DeserializationStrategy<RestRead<Value>> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("com.mountaya.client.Envelope.Rest") {
                element("data", data.descriptor, isOptional = true)
                element("extensions", JsonElement.serializer().descriptor, isOptional = true)
            }

        override fun deserialize(decoder: Decoder): RestRead<Value> {
            var payload: Held<Value>? = null
            var extensions: JsonElement? = null
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        DATA -> payload = Held(decodeSerializableElement(descriptor, DATA, data))
                        EXTENSIONS -> extensions = decodeSerializableElement(descriptor, EXTENSIONS, json)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> throw SerializationException("The envelope holds no member at $index.")
                    }
                }
            }
            val found = payload ?: throw SerializationException("The envelope carries no `data`.")
            return RestRead(found.value, extensions)
        }
    }

    /** Reads a Data API answer: its `data` for the one field [operation] names, its `errors` and its `extensions`. */
    private class DataReader<Result : Any>(
        operation: String,
        result: KSerializer<Result>,
    ) : DeserializationStrategy<DataAnswer<Result>> {
        /** The answer's `data`, an object, or null. */
        private val fields = FieldReader(operation, result).nullable

        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("com.mountaya.client.Envelope.Data") {
                element("data", fields.descriptor, isOptional = true)
                element("extensions", JsonElement.serializer().descriptor, isOptional = true)
                element("errors", JsonElement.serializer().descriptor, isOptional = true)
            }

        override fun deserialize(decoder: Decoder): DataAnswer<Result> {
            var field: Result? = null
            var errors: JsonElement? = null
            var extensions: JsonElement? = null
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        DATA -> field = decodeSerializableElement(descriptor, DATA, fields)?.value
                        EXTENSIONS -> extensions = decodeSerializableElement(descriptor, EXTENSIONS, json)
                        ERRORS -> errors = decodeSerializableElement(descriptor, ERRORS, json)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> throw SerializationException("The answer holds no member at $index.")
                    }
                }
            }
            return DataAnswer(ErrorEnvelope.read(errors, extensions), field)
        }
    }

    /** What a Data API answer's `data` holds of its operation: the field, or null where it is null or absent. */
    private class Field<Result>(
        val value: Result?,
    )

    /**
     * Reads a Data API answer's `data`, an object, for the one field [operation] names, with [result]
     * where it holds a value. It is only ever read, and refuses to write one.
     */
    private class FieldReader<Result : Any>(
        operation: String,
        result: KSerializer<Result>,
    ) : KSerializer<Field<Result>> {
        /** The field's own type, a `null` read as no field. */
        private val field = result.nullable

        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("com.mountaya.client.Envelope.Data.$operation") {
                element(operation, field.descriptor, isOptional = true)
            }

        override fun deserialize(decoder: Decoder): Field<Result> {
            var read: Result? = null
            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        FIELD -> read = decodeSerializableElement(descriptor, FIELD, field)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> throw SerializationException("The answer's `data` holds no member at $index.")
                    }
                }
            }
            return Field(read)
        }

        override fun serialize(
            encoder: Encoder,
            value: Field<Result>,
        ): Unit = throw SerializationException("An answer's `data` is read, and never written.")
    }
}
