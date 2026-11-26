package com.mountaya.geo

import kotlin.jvm.JvmInline
import kotlin.time.Duration

/**
 * SearchPause is how long a query goes unchanged before a search asks a round for it (`geo.search.typing-pause`): a
 * length of time of zero or more, zero asking a round as the query is typed.
 *
 * It is built by [of], which is the refusal: a pause below zero is no length of time to wait, so it is no
 * `SearchPause` and no search is ever built over one, as a cap outside 1 to [SEARCH_LIMIT_MAX] is no [SearchLimit].
 * [standard] is the contract's own, [searchTypingPause], which a search waits unless it is given another. It is a
 * value over a `Duration` and no open vocabulary, so its default instance is `standard`, the word every SDK gives
 * one. Two are equal where they hold one duration.
 */
@JvmInline
public value class SearchPause private constructor(
    /** The pause, zero or more. */
    public val duration: Duration,
) {
    override fun toString(): String = "SearchPause($duration)"

    /** The factory of a pause, and the holder of the contract's own. */
    public companion object {
        /** The contract's typing pause, [searchTypingPause], 150 ms, which a search waits unless it is given another. */
        public val standard: SearchPause = SearchPause(searchTypingPause)

        /** Answers the pause [duration], or null where it is below zero, which is no length of time to wait. */
        public fun of(duration: Duration): SearchPause? = if (duration < Duration.ZERO) null else SearchPause(duration)
    }
}
