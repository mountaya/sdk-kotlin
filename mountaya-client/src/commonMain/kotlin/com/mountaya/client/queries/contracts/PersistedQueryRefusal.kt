package com.mountaya.client

import com.mountaya.client.generated.ApiErrorCode

/**
 * PersistedQueryRefusal is what a server means when it refuses a request that sent a document's
 * digest without the document, with the words each server spells it in.
 *
 * Both spellings of each are kept, a `message` alone or a `message` with an `extensions.code`,
 * because a client that reads only one of them misses half the servers it will meet.
 */
internal enum class PersistedQueryRefusal(
    /** The `message` values that say it. */
    val messages: Set<String>,
    /** The `extensions.code` values that say it. */
    val codes: Set<ApiErrorCode>,
) {
    /**
     * The server does not hold the document yet and asks for it. This is the ordinary first answer
     * to a digest, and the request is sent again with the document beside it.
     */
    Register(
        messages = setOf("PersistedQueryNotFound"),
        codes = setOf(ApiErrorCode("PERSISTED_QUERY_NOT_FOUND")),
    ),

    /**
     * The server declines the protocol, which opts the transport out of it for its lifetime. It is
     * read on any attempt that sent a digest, and [unsupportedEntries] declines it too, on a
     * digest-only attempt alone.
     */
    Unsupported(
        messages = setOf("PersistedQueryNotSupported"),
        codes = setOf(ApiErrorCode("PERSISTED_QUERY_NOT_SUPPORTED")),
    ),
    ;

    /** The entries that decline the protocol only by their code and their message together. */
    companion object {
        /**
         * Each entry's `extensions.code`, then its `message`: the Data API with the extension turned
         * off finds no operation in a body that carries no document, so the entry declines the
         * protocol only in answer to a digest-only attempt. The same code with any other message,
         * and a parse failure, are ordinary failures.
         */
        val unsupportedEntries: List<Pair<ApiErrorCode, String>> =
            listOf(ApiErrorCode("GRAPHQL_VALIDATION_FAILED") to "no operation provided")
    }
}
