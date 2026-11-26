package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * ErrorEnvelope is what a failing answer's body carries: its entries, and its
 * `extensions.metadata`.
 *
 * A body is read for what it holds and refused for nothing: an answer whose body is no JSON, or
 * whose `errors` are no list, carries no entry, and the error raised from it is still the one its
 * status names.
 */
internal class ErrorEnvelope(
    /** Every member of `errors` that is an entry, an object with a string `message`, in order. */
    val errors: List<ErrorEntry>,
    /**
     * Whether the body carries `errors` at all and not as null, which on the Data API makes a
     * success an error even where none of them is an entry.
     */
    val hasErrors: Boolean,
    /** `extensions.metadata` exactly as sent, or null where the body carries none or `null`. */
    val metadata: JsonElement?,
) {
    companion object {
        /** What a body that holds no envelope carries: nothing. */
        val empty: ErrorEnvelope = ErrorEnvelope(emptyList(), hasErrors = false, metadata = null)

        /** Returns what [body] carries, or nothing where it is no JSON object. */
        fun read(body: ByteArray): ErrorEnvelope {
            val members =
                try {
                    wireJson.parseToJsonElement(body.decodeToString()) as? JsonObject
                } catch (malformed: SerializationException) {
                    null
                }
            return members?.let(::read) ?: empty
        }

        /** Returns what a body already read as JSON carries. */
        fun read(members: JsonObject): ErrorEnvelope = read(members["errors"], members["extensions"])

        /**
         * Returns what a body carries in its [errors] and its [extensions], each as sent, or null where
         * the body left it out, for a body whose other members were read on their own.
         */
        fun read(
            errors: JsonElement?,
            extensions: JsonElement?,
        ): ErrorEnvelope {
            val hasErrors = errors != null && errors !is JsonNull
            val entries = (errors as? JsonArray)?.mapNotNull(::entry).orEmpty()
            val metadata = (extensions as? JsonObject)?.get("metadata")?.takeIf { it !is JsonNull }
            return ErrorEnvelope(entries, hasErrors, metadata)
        }

        /**
         * Returns one member of `errors` as an entry, or null where it is no object with a string
         * `message`. Its code is its `extensions.code` where that is a string, and its path is kept
         * only where every step of it is a string or a whole number.
         */
        fun entry(value: JsonElement): ErrorEntry? {
            val members = value as? JsonObject ?: return null
            val message = (members["message"] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
            val code =
                ((members["extensions"] as? JsonObject)?.get("code") as? JsonPrimitive)
                    ?.takeIf { it.isString }
                    ?.content
                    ?.let(::ApiErrorCode)
            return ErrorEntry(message, code, path((members["path"] as? JsonArray)))
        }

        private fun path(steps: JsonArray?): List<ErrorEntry.PathComponent>? {
            steps ?: return null
            return steps.map { step ->
                val primitive = step as? JsonPrimitive ?: return null
                if (primitive.isString) {
                    ErrorEntry.PathComponent.Key(primitive.content)
                } else {
                    val number = primitive.doubleOrNull ?: return null
                    if (number != kotlin.math.floor(number) || number < Int.MIN_VALUE || number > Int.MAX_VALUE) {
                        return null
                    }
                    ErrorEntry.PathComponent.Index(number.toInt())
                }
            }
        }
    }
}
