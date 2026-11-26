package com.mountaya.client

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/**
 * IdempotentAnswer is the answer a write was first given, as a 409 coded
 * `IDEMPOTENCY_ANSWER_UNAVAILABLE` names it under `extensions.metadata`: the write was made, and
 * this is what its caller reads the resource again from.
 */
public class IdempotentAnswer(
    /** The status the write was first answered with, or null where the metadata states no whole number there. */
    public val status: Int?,
    /** The `Location` that answer carried, or null where the metadata states no string there. */
    public val location: String?,
    /** The `ETag` that answer carried, or null where the metadata states no string there. */
    public val etag: String?,
) {
    override fun equals(other: Any?): Boolean =
        other is IdempotentAnswer && other.status == status && other.location == location && other.etag == etag

    override fun hashCode(): Int = ((status ?: 0) * 31 + (location?.hashCode() ?: 0)) * 31 + (etag?.hashCode() ?: 0)

    override fun toString(): String = "IdempotentAnswer(status=$status, location=$location, etag=$etag)"

    internal companion object {
        /** Returns what [metadata] states of the first answer, each part null where it states none of its type. */
        fun of(metadata: JsonElement?): IdempotentAnswer {
            val members = metadata as? JsonObject ?: return IdempotentAnswer(null, null, null)
            val status =
                (members["status"] as? JsonPrimitive)
                    ?.takeIf { !it.isString }
                    ?.doubleOrNull
                    ?.takeIf { it == kotlin.math.floor(it) && it >= Int.MIN_VALUE && it <= Int.MAX_VALUE }
                    ?.toInt()
            return IdempotentAnswer(status, text(members["location"]), text(members["etag"]))
        }

        private fun text(value: JsonElement?): String? = (value as? JsonPrimitive)?.takeIf { it.isString }?.content
    }
}
