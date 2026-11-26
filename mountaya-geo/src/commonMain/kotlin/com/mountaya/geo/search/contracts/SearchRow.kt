package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchRow is one search result as a list renders it, whatever section it came from.
 *
 * [id] is the entity's kind and its own identity (`place:`, `poi:` and `way:` then the OpenStreetMap
 * type and id; `itinerary:`, `collection:` and `zone:` then the id), never a position in a list, so a
 * list keyed by it keeps its rows as a slower section lands. [matched] are the runs of [title] the
 * query emphasizes, as ranges of its UTF-16 indexes, empty where it matched none, and [parts] the title
 * cut at them, ready to draw; [activities] are the activities the row advertises, empty where it
 * advertises none.
 *
 * Two are equal where every member is.
 */
public class SearchRow(
    /** The row's identity: its kind, then the entity's own. */
    public val id: String,
    /** What the row is called. */
    public val title: String,
    /** The runs of the title the query matched. */
    public val matched: List<IntRange>,
    /** The second line, or null for a row that is its title alone. */
    public val secondary: SearchRowSecondary?,
    /** The activities the row advertises, as glyphs beside it. */
    public val activities: List<DataSchema.ActivitySlug>,
    /** What picking the row hands back. */
    public val payload: SearchRowPayload,
) {
    /**
     * The title as the parts a row draws it in: each run [matched] holds and each run between them, as the
     * title writes it, in order, so the parts joined are the title, and a row that matched nothing is one
     * unmatched part. The unit every SDK shares, cut once, when first read.
     */
    public val parts: List<HighlightPart> by lazy(LazyThreadSafetyMode.PUBLICATION) { partsOf(title, matched) }

    override fun equals(other: Any?): Boolean =
        other is SearchRow && other.id == id && other.title == title && other.matched == matched &&
            other.secondary == secondary &&
            other.activities == activities && other.payload == payload

    override fun hashCode(): Int = id.hashCode() * 31 + payload.hashCode()

    override fun toString(): String =
        "SearchRow($id, \"$title\", matched=$matched, secondary=$secondary, payload=$payload)"
}
