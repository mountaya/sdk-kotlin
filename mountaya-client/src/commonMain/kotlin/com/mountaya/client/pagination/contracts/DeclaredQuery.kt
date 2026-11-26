package com.mountaya.client

/** The query parameter a page is asked from. */
private const val CURSOR = "cursor"

/** The query parameter a page's size is asked with. */
private const val LIMIT = "limit"

/**
 * DeclaredQuery is the query of a listing walked by its declaration, `Mountaya.walk`, through which `PageWalk` reads
 * the cursor the caller named and sets each page's: the caller's names and values, in their order, and in place of
 * any `cursor` and `limit` among them, the walk's.
 *
 * The first `cursor` the caller names is the one the walk resumes from, and the first `limit` the one it holds before
 * the walk sets its page size.
 */
internal class DeclaredQuery(
    query: List<Pair<String, String>>,
) : PagedParameters() {
    /** The caller's names and values but its cursor and its limit, in their order. */
    private val rest: List<Pair<String, String>> = query.filter { (name, _) -> name != CURSOR && name != LIMIT }

    override var pageCursor: String? = query.firstOrNull { it.first == CURSOR }?.second

    override var pageLimit: Int? = query.firstOrNull { it.first == LIMIT }?.second?.toIntOrNull()

    /** What the page sends: the caller's names and values, then the cursor where there is one, then the limit. */
    val pairs: List<Pair<String, String>>
        get() = rest + listOfNotNull(pageCursor?.let { CURSOR to it }, pageLimit?.let { LIMIT to it.toString() })
}
