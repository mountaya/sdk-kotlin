package com.mountaya.client

/**
 * FoldedHeaders is a set of headers folded once, as `HeaderLookup.fold` folds them, so each header read of it is one
 * lookup: an answer's, which the transport reads its hook's report, its error and its response from as it arrives. A
 * header is read in any letter case, and answers what `HeaderLookup.value` reads of the headers it was folded from.
 */
internal class FoldedHeaders(
    headers: Map<String, String>,
) {
    /** Every header, by its name lowered, in the order the set first spelled it. */
    val byName: Map<String, String> = HeaderLookup.fold(headers)

    /** Returns the value of the header named [name], read in any letter case, or null where the set holds none. */
    operator fun get(name: String): String? = byName[HeaderLookup.asciiLowercase(name)]
}
