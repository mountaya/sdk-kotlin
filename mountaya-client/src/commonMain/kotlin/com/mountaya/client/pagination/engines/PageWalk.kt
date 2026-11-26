package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * PageWalk reads a listing paged by cursor one row at a time, requesting a page only as its
 * collector takes the rows before it.
 *
 * ```
 * val itineraries = PageWalk(pageSize = 20) { paged ->
 *     client.studio.itineraries.getItineraries {
 *         sort = RestSchema.GetItinerariesSort.UpdatedAtDescending
 *         paged()
 *     }
 * }
 * itineraries.collect { itinerary -> show(itinerary) }
 * ```
 *
 * The first request carries the caller's own `cursor`, where the builder names one, so a walk
 * resumes where an earlier one stopped; each later one carries the `next_cursor` the page before it
 * named. A page may hold fewer rows than it was asked for, or none, and is followed by its cursor
 * all the same: only a null `next_cursor` ends a listing. The walk stops at that page, at the
 * ceiling on requests the caller states, which leaves the rest unread and raises nothing, or at a
 * cursor it has already sent, which it refuses to follow: it hands on that page's rows and then
 * raises the base `ApiException`, since following it would read the listing again without end
 * and stopping there would answer part of a listing as if it were the whole.
 *
 * A collector that is cancelled, or that stops early with `take` or `first`, requests no further
 * page, and the walk ends with the collector's own `CancellationException`.
 */
public object PageWalk {
    /** How many rows a page is asked for where the caller names none. */
    public val defaultPageSize: Int = 50

    /** The most rows the API puts on a page. */
    public val maxPageSize: Int = 100

    /**
     * Returns the cold walk of the listing [page] requests: each call of [page] is handed `paged`,
     * which sets the page's `cursor` and `limit` on the printed builder, and which the call applies
     * last in its builder, after the filters it sends on every page.
     *
     * [pageSize] is how many rows each request asks for, from 1 to [maxPageSize], and [maxPages] the
     * most requests the walk makes, at least 1, or null for none: the listing is then read to its
     * last page. Either out of its range throws [ConfigurationException] here, before any flow is
     * collected, and a page that never applies `paged` throws it once that page has answered.
     */
    public operator fun <Row> invoke(
        pageSize: Int = defaultPageSize,
        maxPages: Int? = null,
        page: suspend (paged: PagedParameters.() -> Unit) -> Response<List<Row>, RestSchema.ListMetadata>,
    ): Flow<Row> = pages(pageSize, maxPages, { metadata -> metadata.pagination.next_cursor }, page)

    /**
     * Returns the cold walk of the listing [page] requests, as [invoke] does, each page's metadata read by
     * [nextCursor] for the cursor of the page after it: a printed listing's `ListMetadata`, or the open map a
     * call by declaration reads, whose cursor is null where it names none it can read.
     */
    internal fun <Row, Metadata> pages(
        pageSize: Int,
        maxPages: Int?,
        nextCursor: (Metadata) -> String?,
        page: suspend (paged: PagedParameters.() -> Unit) -> Response<List<Row>, Metadata>,
    ): Flow<Row> {
        if (pageSize !in 1..maxPageSize) {
            throw ConfigurationException("A page holds from 1 to $maxPageSize rows, and $pageSize is asked for.")
        }
        if (maxPages != null && maxPages < 1) {
            throw ConfigurationException("A walk makes at least one request, and a ceiling of $maxPages allows none.")
        }
        return flow {
            val sent = mutableSetOf<String>()
            var cursor: String? = null
            var isFirst = true
            var requested = 0
            while (maxPages == null || requested < maxPages) {
                currentCoroutineContext().ensureActive()
                var applied = false
                val response =
                    page {
                        // The first page goes from the cursor the caller named, and every later one
                        // from the cursor the page before it named.
                        if (isFirst) cursor = pageCursor
                        pageCursor = cursor
                        pageLimit = pageSize
                        applied = true
                    }
                if (!applied) {
                    throw ConfigurationException(
                        "A walk's page did not apply its parameters: call `paged()` last in the operation's builder.",
                    )
                }
                isFirst = false
                cursor?.let { sent += it }
                requested += 1
                val next = nextCursor(response.metadata)
                for (row in response.data) emit(row)
                if (next == null) return@flow
                if (next in sent) {
                    throw ApiException(
                        response.status,
                        emptyList(),
                        null,
                        null,
                        response.requestId,
                        null,
                        null,
                        null,
                        null,
                    )
                }
                cursor = next
            }
        }
    }
}
