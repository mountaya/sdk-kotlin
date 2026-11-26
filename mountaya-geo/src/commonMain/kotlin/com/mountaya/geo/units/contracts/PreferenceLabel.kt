package com.mountaya.geo

/**
 * PreferenceLabel is what an option of a preferences picker is labelled with
 * (`geo.units.preference-choices`): a word of the elements' `preferences` section, or, for a language, its
 * name in itself, which no language translates.
 *
 * Two are equal where they are of one kind and spell the same text.
 */
public sealed interface PreferenceLabel {
    /** Word is the key of a word of the elements' `preferences` section, `metric`. */
    public class Word(
        /** The word's key. */
        public val word: String,
    ) : PreferenceLabel {
        override fun equals(other: Any?): Boolean = other is Word && other.word == word

        override fun hashCode(): Int = word.hashCode()

        override fun toString(): String = "Word($word)"
    }

    /** Name is a language's name in itself, `Deutsch`, as `languageEndonyms` writes it. */
    public class Name(
        /** The name. */
        public val name: String,
    ) : PreferenceLabel {
        override fun equals(other: Any?): Boolean = other is Name && other.name == name

        override fun hashCode(): Int = name.hashCode() * 31 + 1

        override fun toString(): String = "Name($name)"
    }
}
