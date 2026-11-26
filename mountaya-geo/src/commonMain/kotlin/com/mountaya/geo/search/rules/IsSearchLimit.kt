package com.mountaya.geo

/**
 * SEARCH_LIMIT_MAX is the most results a round of a search asks for, the ceiling the Data API holds a
 * search's `limit` to and the one a viewport-restricted search shares.
 */
public const val SEARCH_LIMIT_MAX: Int = 50

/**
 * isSearchLimit answers whether [limit] is a cap a search round may ask for: a whole number from 1
 * to [SEARCH_LIMIT_MAX]. A search refuses any other before it asks a round, rather than sending one
 * the API refuses; a cap that is no whole number is one an `Int` cannot hold.
 */
public fun isSearchLimit(limit: Int): Boolean = limit in 1..SEARCH_LIMIT_MAX
