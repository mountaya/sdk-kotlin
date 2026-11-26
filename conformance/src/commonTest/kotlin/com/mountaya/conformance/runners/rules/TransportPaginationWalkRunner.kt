package com.mountaya.conformance

import com.mountaya.admin.admin
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.client.ExchangeBody
import com.mountaya.client.ExchangeRequest
import com.mountaya.client.ExchangeResponse
import com.mountaya.client.HttpExchange
import com.mountaya.client.MountayaException
import com.mountaya.client.PageWalk
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

/**
 * `transport.pagination.walk`: the case's listing walked through `PageWalk` over the printed
 * `getOrganizationMembers`, the admin unit's, which `mountaya-admin` declares on the client as
 * `admin`, a scripted exchange answering the page whose cursor each request carries.
 *
 * The caller's query is the printed builder's own: `cursor`, `limit`, `role` and `sort`, the four
 * the fixture writes, set before `paged()`. Every request is read back off the exchange: the
 * `limit` and the `cursor` it carried, and the rest of its query. A row is a member whose `user_id`
 * is its number in the listing, so the rows a walk answered are compared as a count where they are
 * the listing's first, and as the numbers themselves where they are not. A walk built with a size
 * or a ceiling out of its range is refused where it is built, and is answered as one that made no
 * request.
 */
object TransportPaginationWalkRunner : ConformanceRunner {
    /** A page the listing answers: its rows from [from], [count] of them, and the cursor after it. */
    private class Page(
        val from: Int,
        val count: Int,
        val next: String?,
    )

    /** Request is what one page's request carried under `query`. */
    private class Request(
        val cursor: String?,
        val limit: Int?,
        val rest: List<Pair<String, String>>,
    )

    /** Lister answers each request with the page whose cursor it carries, and records what each carried. */
    private class Lister(
        private val pages: Map<String?, Page>,
    ) : HttpExchange {
        val requests = mutableListOf<Request>()

        override suspend fun send(request: ExchangeRequest): ExchangeResponse {
            val query =
                request.url
                    .toString()
                    .substringAfter('?', "")
                    .split('&')
                    .filter { it.isNotEmpty() }
                    .map { it.substringBefore('=') to decode(it.substringAfter('=')) }
            val cursor = query.firstOrNull { it.first == "cursor" }?.second
            val limit = query.firstOrNull { it.first == "limit" }?.second?.toIntOrNull()
            requests +=
                Request(
                    cursor,
                    limit,
                    query.filter { it.first != "cursor" && it.first != "limit" }.sortedBy { it.first },
                )
            val page = pages[cursor] ?: throw AssertionError("no page is named by the cursor `${cursor ?: "null"}`")
            val rows =
                (page.from until page.from + page.count).joinToString(",") {
                    """{"user_id":"$it","role":"member","joined_at":"2026-01-01T00:00:00Z"}"""
                }
            val next = page.next?.let { "\"$it\"" } ?: "null"
            val pagination = """{"limit":${limit ?: 0},"next_cursor":$next}"""
            val body = """{"data":[$rows],"extensions":{"metadata":{"pagination":$pagination}}}"""
            return ExchangeResponse(200, body = ExchangeBody(body.encodeToByteArray()))
        }

        private fun decode(text: String): String =
            buildString {
                val bytes = mutableListOf<Byte>()
                var index = 0
                while (index < text.length) {
                    if (text[index] == '%') {
                        bytes += text.substring(index + 1, index + 3).toInt(16).toByte()
                        index += 3
                    } else {
                        bytes += text[index].code.toByte()
                        index += 1
                    }
                }
                append(bytes.toByteArray().decodeToString())
            }
    }

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val defaults = case.root("defaults")
        val constants = case.root("constants")
        if (constants.number("maxPageSize").toInt() != PageWalk.maxPageSize ||
            defaults.number("pageSize").toInt() != PageWalk.defaultPageSize ||
            defaults.numberOrNull("maxPages") != null
        ) {
            throw AssertionError("the fixture's defaults are not the walk's")
        }
        val input = case.fields
        val options = input.objectOrNull("options") ?: FixtureValue.Object(emptyList())
        val pageSize = options.numberOrNull("pageSize")?.toInt() ?: PageWalk.defaultPageSize
        val maxPages = options.numberOrNull("maxPages")?.toInt()
        val abortAfter = input.numberOrNull("abortAfterRows")?.toInt()
        val pages =
            input.items("pages").associate { item ->
                val page = item as? FixtureValue.Object ?: throw AssertionError("a page is no object")
                page.textOrNull("cursor") to
                    Page(page.number("from").toInt(), page.number("count").toInt(), page.textOrNull("next"))
            }
        val written =
            input.objectOrNull("query")?.entries.orEmpty().map { (name, value) ->
                name to
                    when (value) {
                        is FixtureValue.Text -> value.value
                        is FixtureValue.Number -> value.value.toLong().toString()
                        else -> throw AssertionError("the query's `$name` is no string or whole number")
                    }
            }
        val callerCursor = written.firstOrNull { it.first == "cursor" }?.second

        val lister = Lister(pages)
        val client = Scripted.client(scope, lister, credential = Scripted.session)
        val rows = mutableListOf<Int>()
        var outcome = "ended"
        try {
            val walk: Flow<AdminSchema.OrganizationMember> =
                PageWalk(pageSize = pageSize, maxPages = maxPages) { paged ->
                    client.admin.members.getOrganizationMembers("org") {
                        for ((name, value) in written) {
                            when (name) {
                                "cursor" -> cursor = value
                                "limit" -> limit = value.toInt()
                                "role" -> role = RestSchema.OrganizationRole(value)
                                "sort" -> sort = AdminSchema.GetOrganizationMembersSort(value)
                                else -> throw AssertionError("the printed listing takes no `$name`")
                            }
                        }
                        paged()
                    }
                }
            // The caller is a job of its own, which cancels itself once it has read its rows.
            val reading =
                scope.launch {
                    try {
                        walk
                            .onEach { member ->
                                rows += member.user_id.toInt()
                                if (abortAfter != null && rows.size >= abortAfter) {
                                    throw CancellationException("the caller's own reason")
                                }
                            }.collect()
                    } catch (error: MountayaException) {
                        outcome = error.name
                    }
                }
            reading.join()
            if (reading.isCancelled) outcome = "aborted"
        } catch (error: MountayaException) {
            outcome = error.name
        }
        if (outcome == "ended" && rows.isNotEmpty() &&
            lister.requests.isEmpty()
        ) {
            throw AssertionError("rows came from nowhere")
        }
        val limits = lister.requests.map { it.limit }.toSet()
        if (limits.size > 1) throw AssertionError("the walk's requests carried different limits")
        val others = lister.requests.map { it.rest }.toSet()
        if (others.size > 1) throw AssertionError("the walk's requests carried different queries")
        val first = pages[callerCursor]?.from ?: 0
        val answered =
            if (first == 0 && rows == List(rows.size) { it }) {
                Scripted.number(rows.size)
            } else {
                FixtureValue.Array(rows.map { Scripted.number(it) })
            }
        return ConformanceOutcome.Value(
            Scripted.objectOf(
                "cursors" to FixtureValue.Array(lister.requests.map { Scripted.text(it.cursor) }),
                "limit" to (limits.firstOrNull()?.let { Scripted.number(it) } ?: FixtureValue.Null),
                "outcome" to FixtureValue.Text(outcome),
                "query" to
                    FixtureValue.Object(
                        others.firstOrNull().orEmpty().map { it.first to FixtureValue.Text(it.second) },
                    ),
                "rows" to answered,
            ),
        )
    }
}
