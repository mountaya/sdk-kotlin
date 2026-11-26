package com.mountaya.geo

/**
 * SearchSectionState is how one section of a search stands: loading while its branch is in flight, settled once it
 * answered, or failed (`geo.search.live-round`).
 *
 * It stands per section, since the branches fail apart: the API answers the ones that worked and reports the ones
 * that did not, so a points-of-interest outage leaves the itineraries beside it as they are. Two failed states are
 * equal where they hold the very same failure.
 */
public sealed interface SearchSectionState {
    /** Whether the branch is in flight. */
    public val isLoading: Boolean get() = this is Loading

    /** Whether the branch answered. */
    public val isSettled: Boolean get() = this is Settled

    /** Why the branch failed, or null for a branch that did not. */
    public val failure: Throwable? get() = null

    /** Loading is a branch in flight. */
    public object Loading : SearchSectionState {
        override fun toString(): String = "SearchSectionState.Loading"
    }

    /** Settled is a branch that answered, with its rows or with none. */
    public object Settled : SearchSectionState {
        override fun toString(): String = "SearchSectionState.Settled"
    }

    /**
     * Failed is a branch that failed, holding why: what the search's transport threw, or a
     * [SearchBranchException] where the Data API answered the branch as failed.
     */
    public class Failed(
        /** Why the branch failed. */
        override val failure: Throwable,
    ) : SearchSectionState {
        override fun equals(other: Any?): Boolean = other is Failed && other.failure === failure

        override fun hashCode(): Int = failure.hashCode()

        override fun toString(): String = "SearchSectionState.Failed($failure)"
    }
}
