package com.mountaya.conformance

/**
 * Compares [actual] with [expected] and returns where they first differ, or null when they
 * match.
 *
 * Numbers compare within [tolerance]. An object compares by its set of keys and then key by key,
 * whatever the order: where order is what a fixture proves, it proves it in an array or a string.
 */
internal fun compareFixtureValues(
    expected: FixtureValue,
    actual: FixtureValue,
    tolerance: Tolerance,
    at: String = "$",
): String? =
    when (expected) {
        is FixtureValue.Null -> {
            if (actual is FixtureValue.Null) null else differs(at, expected, actual)
        }

        is FixtureValue.Bool -> {
            if (actual is FixtureValue.Bool &&
                actual.value == expected.value
            ) {
                null
            } else {
                differs(at, expected, actual)
            }
        }

        is FixtureValue.Text -> {
            if (actual is FixtureValue.Text &&
                actual.value == expected.value
            ) {
                null
            } else {
                differs(at, expected, actual)
            }
        }

        is FixtureValue.Number -> {
            if (actual is FixtureValue.Number &&
                tolerance.accepts(expected.value, actual.value)
            ) {
                null
            } else {
                differs(at, expected, actual)
            }
        }

        is FixtureValue.Array -> {
            compareArrays(expected, actual, tolerance, at)
        }

        is FixtureValue.Object -> {
            compareObjects(expected, actual, tolerance, at)
        }
    }

/**
 * Compares the [actual] members of a set with the [expected] array, whatever the order of either, and returns where
 * they first differ, or null when they match: each expected item matched by one member, within [tolerance], and no
 * member left over.
 */
internal fun compareFixtureMembers(
    expected: FixtureValue,
    actual: List<FixtureValue>,
    tolerance: Tolerance,
): String? {
    if (expected !is FixtureValue.Array) {
        return "at $: expected ${describe(expected)}, got the members of a set"
    }
    if (actual.size != expected.items.size) return "at $: expected ${expected.items.size} members, got ${actual.size}"
    val left = actual.toMutableList()
    for ((index, item) in expected.items.withIndex()) {
        val match = left.indexOfFirst { compareFixtureValues(item, it, tolerance) == null }
        if (match < 0) {
            val others = left.joinToString(", ") { describe(it) }
            return "at $[$index]: expected ${describe(item)} among the members, got $others"
        }
        left.removeAt(match)
    }
    return null
}

private fun compareArrays(
    expected: FixtureValue.Array,
    actual: FixtureValue,
    tolerance: Tolerance,
    at: String,
): String? {
    if (actual !is FixtureValue.Array) return differs(at, expected, actual)
    if (actual.items.size != expected.items.size) {
        return "at $at: expected ${expected.items.size} items, got ${actual.items.size}"
    }
    return expected.items.indices.firstNotNullOfOrNull { index ->
        compareFixtureValues(expected.items[index], actual.items[index], tolerance, "$at[$index]")
    }
}

private fun compareObjects(
    expected: FixtureValue.Object,
    actual: FixtureValue,
    tolerance: Tolerance,
    at: String,
): String? {
    if (actual !is FixtureValue.Object) return differs(at, expected, actual)
    val missing = expected.keys - actual.keys.toSet()
    val extra = actual.keys - expected.keys.toSet()
    if (missing.isNotEmpty() || extra.isNotEmpty()) {
        return "at $at: expected the keys ${expected.keys.sorted()}, got ${actual.keys.sorted()}"
    }
    return expected.entries.firstNotNullOfOrNull { (key, value) ->
        compareFixtureValues(value, actual[key] ?: FixtureValue.Null, tolerance, "$at.$key")
    }
}

private fun differs(
    at: String,
    expected: FixtureValue,
    actual: FixtureValue,
): String = "at $at: expected ${describe(expected)}, got ${describe(actual)}"

/** A value as a message shows it. */
internal fun describe(value: FixtureValue): String =
    when (value) {
        is FixtureValue.Null -> {
            "null"
        }

        is FixtureValue.Bool -> {
            value.value.toString()
        }

        is FixtureValue.Number -> {
            value.value.toString()
        }

        is FixtureValue.Text -> {
            "\"${value.value}\""
        }

        is FixtureValue.Array -> {
            value.items.joinToString(", ", "[", "]") { describe(it) }
        }

        is FixtureValue.Object -> {
            value.entries.joinToString(
                ", ",
                "{",
                "}",
            ) { (key, item) -> "\"$key\": ${describe(item)}" }
        }
    }
