package com.mountaya.client

/**
 * Returns what the errors that answered [attempt] mean, or null where none is a refusal: an
 * ordinary error reaches the caller untouched.
 *
 * It is read before the rule that a Data API answer carrying errors is a failure, since the
 * ordinary answer to a digest the server does not hold yet is one. Any entry may carry the refusal,
 * whatever its place, each spelled by its message alone or by its code, and an entry declining the
 * protocol is read before one asking for the document, since the first is a statement about the
 * server and the second about one document. Only a digest-only attempt is asked for its document,
 * and only one is declined by [PersistedQueryRefusal.unsupportedEntries], so an attempt that sent
 * its document reads a declined protocol alone, and one that sent no digest reads no refusal.
 */
internal fun readPersistedQueryRefusal(
    errors: List<ErrorEntry>,
    attempt: PersistedQueryAttempt,
): PersistedQueryRefusal? {
    if (attempt.digest == null) return null
    val declines =
        errors.any { entry ->
            entry.spells(PersistedQueryRefusal.Unsupported) ||
                (
                    attempt.isDigestOnly &&
                        PersistedQueryRefusal.unsupportedEntries.any { it == (entry.code to entry.message) }
                )
        }
    val asks = attempt.isDigestOnly && errors.any { it.spells(PersistedQueryRefusal.Register) }
    return when {
        declines -> PersistedQueryRefusal.Unsupported
        asks -> PersistedQueryRefusal.Register
        else -> null
    }
}

/** Whether the entry carries [refusal] by its message alone or by its code. */
private fun ErrorEntry.spells(refusal: PersistedQueryRefusal): Boolean {
    val code = code
    return message in refusal.messages || (code != null && code in refusal.codes)
}
