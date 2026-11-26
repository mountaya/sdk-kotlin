package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchFetch is how one request of a search reaches the Data API: the app's call that answers a search input with
 * a search result, its client's `data.search` in one line, or a call through a transport of its own.
 *
 * It is the seam that keeps one search: the engine holds the whole of a round above it, the typing pause, the
 * request per branch, the abandon of a round a later one replaced, the fall back from the visible area and the
 * rows, whoever makes the request. A request a round abandons is withdrawn by cancelling its coroutine, which a call
 * through the client hands its exchange.
 *
 * ```kotlin
 * val fetch = SearchFetch { input -> mountaya.data.search(input) }
 * ```
 *
 * The fetch is then handed to a `SearchEngine`, which makes every request of a search through it.
 */
public fun interface SearchFetch {
    /** Answers [input] with the Data API's search result, or throws what the request failed with. */
    public suspend fun search(input: DataSchema.SearchInput): DataSchema.SearchResult
}
