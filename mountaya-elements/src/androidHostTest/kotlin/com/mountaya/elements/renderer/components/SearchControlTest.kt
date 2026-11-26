package com.mountaya.elements

import com.mountaya.client.InternalMountayaApi
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.SearchListStatus
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The search box's status line on the host JVM, Compose left out: the words the box's own strings name stand over the
 * scope's, a count among them as a template whose `{count}` takes the count in the scope's locale.
 */
@OptIn(InternalMountayaApi::class)
class SearchControlTest {
    @Test
    fun theStatusLineReadsTheBoxsOwnStringsItsCountATemplateFilledInTheScopesLocale() {
        val own =
            ElementsScope.standard.withStrings(
                mapOf("search.resultCount" to "{count} huts", "search.emptyTitle" to "No hut"),
            )

        assertEquals("3 huts", searchStatusText(SearchListStatus.ResultCount(3), own))
        assertEquals("1,234 huts", searchStatusText(SearchListStatus.ResultCount(1_234), own))
        assertEquals("No hut", searchStatusText(SearchListStatus.EmptyTitle, own))
        assertEquals("", searchStatusText(null, own))
        assertEquals("3 results", searchStatusText(SearchListStatus.ResultCount(3), ElementsScope.standard))
        assertEquals("Nothing found", searchStatusText(SearchListStatus.EmptyTitle, ElementsScope.standard))
    }
}
