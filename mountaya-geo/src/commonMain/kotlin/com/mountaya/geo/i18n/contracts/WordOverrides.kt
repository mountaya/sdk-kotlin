package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject

/**
 * WordOverrides is a consumer's own words, folded over a bundle's last: keyed by section, for
 * whichever language is selected, or keyed by language, for that language alone. It is one shape or
 * the other, never both, so an override read from JSON is classified by `wordOverridesOf`, which
 * refuses one naming a language and a section together.
 *
 * Two are equal where they are the same shape and hold the same words.
 */
public sealed interface WordOverrides {
    /** BySection is words for the selected language, keyed by section as the bundle's are. */
    public class BySection(
        /** The words, a tree of sections and words. */
        public val words: JsonObject,
    ) : WordOverrides {
        override fun equals(other: Any?): Boolean = other is BySection && other.words == words

        override fun hashCode(): Int = words.hashCode()

        override fun toString(): String = "WordOverrides.BySection($words)"
    }

    /** ByLanguage is words per language, each language's sections under it. */
    public class ByLanguage(
        /** The words, keyed by the language they are for. */
        public val words: Map<RestSchema.Language, JsonObject>,
    ) : WordOverrides {
        override fun equals(other: Any?): Boolean = other is ByLanguage && other.words == words

        override fun hashCode(): Int = words.hashCode()

        override fun toString(): String = "WordOverrides.ByLanguage($words)"
    }
}
