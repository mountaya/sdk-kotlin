package com.mountaya.geo

/**
 * SearchListStatus is what a result list's status line announces (`geo.search.list-state`), each named by the key
 * of the word it reads in the `search` section of the elements' words: that a branch failed, that nothing was
 * found, or how many rows the list shows.
 *
 * Two are equal where they are of one kind and announce the same count.
 */
public sealed interface SearchListStatus {
    /** The key of the word the status reads in the `search` section: `error`, `emptyTitle` or `resultCount`. */
    public val key: String

    /** Error announces that a branch under the filter failed, `search.error`. */
    public object Error : SearchListStatus {
        override val key: String = "error"

        override fun toString(): String = "SearchListStatus.Error"
    }

    /** EmptyTitle announces that nothing was found, `search.emptyTitle`. */
    public object EmptyTitle : SearchListStatus {
        override val key: String = "emptyTitle"

        override fun toString(): String = "SearchListStatus.EmptyTitle"
    }

    /**
     * ResultCount announces how many rows the list shows, `search.resultCount`, a counted word
     * (`geo.i18n.counted-words`).
     */
    public class ResultCount(
        /** How many rows the list shows. */
        public val count: Int,
    ) : SearchListStatus {
        override val key: String = "resultCount"

        override fun equals(other: Any?): Boolean = other is ResultCount && other.count == count

        override fun hashCode(): Int = count

        override fun toString(): String = "SearchListStatus.ResultCount($count)"
    }
}
