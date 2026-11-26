package com.mountaya.elements

/** AccessAnswer is what a map hears that a refusal of access stands or ends on (`map.events.access-standing`). */
internal sealed interface AccessAnswer {
    /** A request refused access, from the source [source], or from none: a style, a sprite, a glyph or the layers manifest. */
    class Refused(
        /** The source refused, or null for a request that names none. */
        val source: String?,
    ) : AccessAnswer {
        override fun equals(other: Any?): Boolean = other is Refused && other.source == source

        override fun hashCode(): Int = source?.hashCode() ?: 0

        override fun toString(): String = "Refused($source)"
    }

    /** A tile of the source [source] loading. */
    class Tile(
        /** The source whose tile loaded. */
        val source: String,
    ) : AccessAnswer {
        override fun equals(other: Any?): Boolean = other is Tile && other.source == source

        override fun hashCode(): Int = source.hashCode()

        override fun toString(): String = "Tile($source)"
    }

    /** A style loading. */
    object Style : AccessAnswer {
        override fun toString(): String = "Style"
    }
}
