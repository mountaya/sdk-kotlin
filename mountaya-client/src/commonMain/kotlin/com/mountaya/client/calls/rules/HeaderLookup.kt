package com.mountaya.client

/**
 * HeaderLookup reads a header out of a set of them, its name compared in any letter case, as HTTP
 * compares one.
 *
 * Only A to Z fold: a header name is ASCII, and a locale's case rules have no say in it.
 */
internal object HeaderLookup {
    /**
     * Returns the value of the header named [name] in [headers], or null where there is none.
     * Where the set spells the name twice in two cases, the values are joined by a comma and a
     * space, in the order of the names, as HTTP reads a header sent twice.
     */
    fun value(
        name: String,
        headers: Map<String, String>,
    ): String? {
        val wanted = asciiLowercase(name)
        val values = headers.entries.filter { asciiLowercase(it.key) == wanted }.sortedBy { it.key }
        if (values.isEmpty()) return null
        return values.joinToString(", ") { it.value }
    }

    /**
     * Returns [headers] folded once: each name lowered as [asciiLowercase] lowers it, in the order the set first spells
     * it, holding the value [value] reads for it, so a name the set spells twice in two cases holds both values joined
     * by a comma and a space, in the order of the names. A header read of the fold is one lookup, by its name lowered,
     * where [value] reads every header again.
     */
    fun fold(headers: Map<String, String>): Map<String, String> {
        val folded = LinkedHashMap<String, String>()
        var repeated: MutableSet<String>? = null
        for ((name, text) in headers) {
            val lowered = asciiLowercase(name)
            if (lowered !in folded) {
                folded[lowered] = text
                continue
            }
            val names = repeated ?: LinkedHashSet<String>()
            names += lowered
            repeated = names
        }
        // A name spelled in two cases is rare, and its values are joined as a lookup of it joins them.
        for (lowered in repeated.orEmpty()) folded[lowered] = value(lowered, headers).orEmpty()
        return folded
    }

    /** Whether [headers] holds a header named [name], in any letter case. */
    fun contains(
        name: String,
        headers: Map<String, String>,
    ): Boolean {
        val wanted = asciiLowercase(name)
        return headers.keys.any { asciiLowercase(it) == wanted }
    }

    /** [text] with A to Z lowered and every other character as it is: [text] itself where it holds no capital. */
    fun asciiLowercase(text: String): String {
        if (text.none { it in 'A'..'Z' }) return text
        return buildString(text.length) {
            for (character in text) append(if (character in 'A'..'Z') character + ('a' - 'A') else character)
        }
    }
}
