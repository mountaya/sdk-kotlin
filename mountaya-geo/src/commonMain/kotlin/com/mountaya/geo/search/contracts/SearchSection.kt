package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchSection is one section of a search: the branch it answers, how it stands, and its rows, in the order the
 * branch answered them (`geo.search.section-order`).
 *
 * Its [id] is its branch, so a list keyed by it keeps its sections in place as the branches land. Two are equal
 * where every member is, a failure being the very same failure.
 */
public class SearchSection(
    /** The branch the section answers. */
    public val id: DataSchema.SearchTarget,
    /** How the section stands. */
    public val state: SearchSectionState,
    /** The section's rows, none while it loads or where it failed. */
    public val rows: List<SearchRow> = emptyList(),
) {
    override fun equals(other: Any?): Boolean =
        other is SearchSection && other.id == id && other.state == state && other.rows == rows

    override fun hashCode(): Int = (id.hashCode() * 31 + state.hashCode()) * 31 + rows.hashCode()

    override fun toString(): String = "SearchSection($id, $state, rows=${rows.map { it.title }})"
}
