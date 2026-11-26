package com.mountaya.geo

/**
 * LayersPanelPress is what a press on a row of a layers picker asks for (`geo.layers.picker-rows`): the
 * command of the row's choice with the row's slug, which the layers selection carries out, or, for a row
 * showing its lock, the report that the entry was pressed, which selects nothing.
 *
 * Two are equal where they are of one kind and hold the same.
 */
public sealed interface LayersPanelPress {
    /** Command is the selection's command for [choice], with the entry's [slug]. */
    public class Command(
        /** The choice whose command the press asks for. */
        public val choice: LayersPanelChoice,
        /** The entry's slug. */
        public val slug: String,
    ) : LayersPanelPress {
        override fun equals(other: Any?): Boolean = other is Command && other.choice == choice && other.slug == slug

        override fun hashCode(): Int = choice.hashCode() * 31 + slug.hashCode()

        override fun toString(): String = "Command(choice=$choice, slug=$slug)"
    }

    /** Locked is the report that the entry [slug] names, out of the viewer's reach, was pressed. */
    public class Locked(
        /** The entry's slug. */
        public val slug: String,
    ) : LayersPanelPress {
        override fun equals(other: Any?): Boolean = other is Locked && other.slug == slug

        override fun hashCode(): Int = slug.hashCode() * 31 + 1

        override fun toString(): String = "Locked($slug)"
    }
}
