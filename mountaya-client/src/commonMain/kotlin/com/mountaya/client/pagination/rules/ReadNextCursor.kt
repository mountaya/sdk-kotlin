package com.mountaya.client

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Returns the cursor of the page after the one [metadata] describes, its `pagination.next_cursor`, read from the
 * envelope's open map as a call by declaration answers it: null on the last page, and null where the map names no
 * cursor as text, which ends a walk rather than failing it.
 */
internal fun readNextCursor(metadata: JsonObject?): String? {
    val pagination = metadata?.get("pagination") as? JsonObject ?: return null
    val next = pagination["next_cursor"] as? JsonPrimitive ?: return null
    return if (next.isString) next.content else null
}
