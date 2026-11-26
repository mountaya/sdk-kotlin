package com.mountaya.build.gates

/**
 * HotPath is one entry of the contract's `sdk/hot-paths.yaml`, as the hot-path gate reads it: its kebab-case [id] and
 * the SDKs that budget it, [sdks]. The sentence its budget holds is the contract's, and the gate never reads it.
 */
data class HotPath(
    /** The path's kebab-case id. */
    val id: String,
    /** The SDKs that budget it. */
    val sdks: List<String>,
) {
    /** Encodes this entry as one line, the form a task input carries it in. */
    fun encode(): String = "$id$SEPARATOR${sdks.joinToString(",")}"

    /** The line form an entry is carried in. */
    companion object {
        private const val SEPARATOR = "|"

        /** Decodes a line written by [encode]. */
        fun decode(line: String): HotPath {
            val parts = line.split(SEPARATOR, limit = 2)
            require(parts.size == 2) { "Not a hot path: $line" }
            return HotPath(parts[0], parts[1].split(',').filter { it.isNotEmpty() })
        }
    }
}
