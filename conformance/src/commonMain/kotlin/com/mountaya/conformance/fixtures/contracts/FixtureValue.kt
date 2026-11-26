package com.mountaya.conformance

/**
 * FixtureValue is a JSON value read from a fixture, keeping what a fixture means by it.
 *
 * An object keeps its keys in the order the file writes them, because some fixtures carry an
 * ordered tree as an object. `null`, a key that is absent and an empty object stay three
 * different things: an absent key is no entry at all, `null` is [Null], and `{}` is an [Object]
 * with no entries.
 */
public sealed interface FixtureValue {
    /** Null is JSON's `null`. */
    public object Null : FixtureValue

    /** Bool is `true` or `false`. */
    public class Bool(
        /** The value. */
        public val value: Boolean,
    ) : FixtureValue

    /**
     * Number is any JSON number, and the three JSON cannot carry: `{ "$number": "NaN" }`,
     * `{ "$number": "Infinity" }` and `{ "$number": "-Infinity" }`. `16` and `16.0` are the same
     * number.
     */
    public class Number(
        /** The value. A fixture's integers are all exact at this precision. */
        public val value: Double,
    ) : FixtureValue

    /** Text is a JSON string. */
    public class Text(
        /** The value. */
        public val value: String,
    ) : FixtureValue

    /** Array is a JSON array. */
    public class Array(
        /** The items, in order. */
        public val items: List<FixtureValue>,
    ) : FixtureValue

    /** Object is a JSON object, its keys in the file's order. */
    public class Object(
        /** The entries, in the order the file writes them. */
        public val entries: List<Pair<String, FixtureValue>>,
    ) : FixtureValue {
        /** The keys, in order. */
        public val keys: List<String> get() = entries.map { it.first }

        /** Returns the value at [key], or null when the key is absent. */
        public operator fun get(key: String): FixtureValue? = entries.firstOrNull { it.first == key }?.second

        /** Returns the number at [key], failing with a sentence when it is absent or not a number. */
        public fun number(key: String): Double = numberOrNull(key) ?: throw AssertionError("`$key` is absent")

        /** Returns the number at [key], or null when the key is absent or `null`; fails on any other value. */
        public fun numberOrNull(key: String): Double? = read(key, "a number") { (it as? Number)?.value }

        /** Returns the string at [key], failing with a sentence when it is absent or not a string. */
        public fun text(key: String): String = textOrNull(key) ?: throw AssertionError("`$key` is absent")

        /** Returns the string at [key], or null when the key is absent or `null`; fails on any other value. */
        public fun textOrNull(key: String): String? = read(key, "a string") { (it as? Text)?.value }

        /** Returns the boolean at [key], failing with a sentence when it is absent or not a boolean. */
        public fun bool(key: String): Boolean =
            read(key, "a boolean") { (it as? Bool)?.value } ?: throw AssertionError("`$key` is absent")

        /** Returns the items of the array at [key], failing with a sentence when it is absent or not an array. */
        public fun items(key: String): List<FixtureValue> =
            read(key, "an array") { (it as? Array)?.items } ?: throw AssertionError("`$key` is absent")

        /** Returns the object at [key], or null when the key is absent or `null`; fails on any other value. */
        public fun objectOrNull(key: String): Object? = read(key, "an object") { it as? Object }

        private fun <T> read(
            key: String,
            expected: String,
            narrow: (FixtureValue) -> T?,
        ): T? {
            val value = get(key)
            if (value == null || value is Null) return null
            return narrow(value) ?: throw AssertionError("`$key` is not $expected")
        }
    }
}
