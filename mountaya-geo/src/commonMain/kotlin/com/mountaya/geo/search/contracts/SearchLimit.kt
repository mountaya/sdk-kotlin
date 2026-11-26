package com.mountaya.geo

import kotlin.jvm.JvmInline

/**
 * SearchLimit is the cap a search round asks each branch for: a whole number from 1 to [SEARCH_LIMIT_MAX], the range
 * the Data API takes (`geo.search.query`).
 *
 * It is built by [of], which is the refusal: a cap outside the range is no `SearchLimit`, so a search never asks a
 * round the API would refuse once per branch, as an error line in every section, for a round that did nothing
 * wrong. A search given none asks the API's own default. Two are equal where they hold one cap.
 */
@JvmInline
public value class SearchLimit private constructor(
    /** The cap, from 1 to [SEARCH_LIMIT_MAX]. */
    public val value: Int,
) {
    override fun toString(): String = "SearchLimit($value)"

    /** The factory of a cap. */
    public companion object {
        /** Answers the cap [value], or null where it lies outside 1 to [SEARCH_LIMIT_MAX] (`isSearchLimit`). */
        public fun of(value: Int): SearchLimit? = if (isSearchLimit(value)) SearchLimit(value) else null
    }
}
