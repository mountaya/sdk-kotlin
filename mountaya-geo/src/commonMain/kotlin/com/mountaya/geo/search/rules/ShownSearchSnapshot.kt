package com.mountaya.geo

/**
 * shownSearchSnapshot answers what a result list shows for the query [query] in its box (`geo.search.typing-pause`):
 * the search's own [snapshot] where it names that query, trimmed as a round asks it, and [unanswered], the stand-in
 * for the query (`unansweredSearchSnapshot`), otherwise.
 *
 * A round is asked once the typing pauses, and until then the search's snapshot answers the query the pause last
 * settled, or a round of an earlier one still in flight lands inside the pause; drawn under the text now in the
 * box, its rows would answer a question nobody is asking. So rows that answer another query are never shown under
 * this one. Letter case is part of the query.
 */
internal fun shownSearchSnapshot(
    snapshot: SearchSnapshot,
    query: String,
    unanswered: SearchSnapshot,
): SearchSnapshot = if (snapshot.query == trimWhiteSpace(query)) snapshot else unanswered
