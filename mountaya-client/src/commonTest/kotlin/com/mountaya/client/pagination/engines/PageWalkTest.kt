package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PageWalkTest {
    /** A listing's builder as a printed one is: its cursor and limit, and a filter beside them. */
    private class Query : PagedParameters() {
        override var pageCursor: String? = null
        override var pageLimit: Int? = null
        var role: String? = null
    }

    /** Answers each page by its cursor and records every query it was sent. */
    private class Listing(
        private val pages: Map<String?, Pair<List<Int>, String?>>,
    ) {
        val queries = mutableListOf<Query>()

        fun page(
            paged: PagedParameters.() -> Unit,
            caller: Query.() -> Unit = {
            },
        ): Response<List<Int>, RestSchema.ListMetadata> {
            val query = Query().apply(caller).apply(paged)
            queries += query
            val (rows, next) = pages[query.pageCursor] ?: (emptyList<Int>() to null)
            return Response(
                rows,
                RestSchema.ListMetadata(
                    RestSchema.PaginationMetadata(limit = query.pageLimit ?: 0, next_cursor = next),
                ),
                status = 200,
                requestId = "r-${queries.size}",
            )
        }
    }

    @Test
    fun rowsAreReadThroughEveryPageAnEmptyOneIncludedEachRequestCarryingTheRest() =
        runTest {
            val listing =
                Listing(
                    mapOf(
                        null to (listOf(0, 1) to "c2"),
                        "c2" to (emptyList<Int>() to "c2x"),
                        "c2x" to (listOf(2) to null),
                    ),
                )
            val rows =
                PageWalk(pageSize = 2) { paged ->
                    listing.page(paged) {
                        pageLimit = 9
                        role = "admin"
                    }
                }.toList()

            assertEquals(listOf(0, 1, 2), rows)
            assertEquals(listOf(null, "c2", "c2x"), listing.queries.map { it.pageCursor })
            assertTrue(listing.queries.all { it.pageLimit == 2 && it.role == "admin" })
        }

    @Test
    fun aPageIsRequestedOnlyAsTheRowsBeforeItAreReadUpToTheCeiling() =
        runTest {
            val listing =
                Listing(
                    mapOf(
                        null to (listOf(0) to "c1"),
                        "c1" to (listOf(1) to "c2"),
                        "c2" to (listOf(2) to null),
                    ),
                )
            val walk = PageWalk(maxPages = 2) { paged -> listing.page(paged) }

            assertEquals(listOf(0), walk.take(1).toList())
            assertEquals(1, listing.queries.size)
            assertEquals(listOf(0, 1), walk.toList())
            assertEquals(3, listing.queries.size)
        }

    @Test
    fun aCursorSentBeforeIsRefusedWithTheBaseApiExceptionOnceItsPageIsRead() =
        runTest {
            val listing = Listing(mapOf("c5" to (listOf(5) to "c6"), "c6" to (listOf(6) to "c5")))
            val rows = mutableListOf<Int>()
            val error =
                assertFailsWith<ApiException> {
                    PageWalk { paged -> listing.page(paged) { pageCursor = "c5" } }.collect { rows += it }
                }

            assertEquals(listOf(5, 6), rows)
            assertEquals("ApiError", error.name)
            assertEquals(200, error.status)
            assertEquals("r-2", error.requestId)
        }

    @Test
    fun aPageSizeOrACeilingOutOfItsRangeIsRefusedBeforeAnyRequest() {
        for ((size, ceiling) in listOf<Pair<Int, Int?>>(0 to null, 101 to null, 50 to 0)) {
            assertFailsWith<ConfigurationException> {
                PageWalk<Int>(pageSize = size, maxPages = ceiling) { throw IllegalStateException("no request is sent") }
            }
        }
    }

    @Test
    fun aCancelledCollectorRequestsNoFurtherPageAndEndsTheWalkWithItsOwnCancellation() =
        runTest {
            val listing = Listing(mapOf(null to (listOf(0) to "c1"), "c1" to (listOf(1) to null)))
            val reader =
                launch {
                    PageWalk { paged -> listing.page(paged) }
                        .onEach { throw CancellationException("the caller left") }
                        .collect()
                }
            reader.join()

            assertTrue(reader.isCancelled)
            assertEquals(1, listing.queries.size)
        }

    @Test
    fun aPageThatNeverAppliesItsParametersIsRefused() =
        runTest {
            val listing = Listing(mapOf(null to (listOf(0) to null)))
            assertFailsWith<ConfigurationException> { PageWalk { _ -> listing.page({}) }.collect() }
        }
}
