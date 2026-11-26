package com.mountaya.conformance

/**
 * fixturePosition returns the array of numbers [value] writes, a position such as `[longitude, latitude]` or the edges
 * of a box, kept as short as the fixture writes it, so a rule meets a position that names no place as the fixture
 * states it. Fails with a sentence naming [what] when the value is anything else.
 */
public fun fixturePosition(
    value: FixtureValue?,
    what: String,
): List<Double> {
    val items = (value as? FixtureValue.Array)?.items ?: throw AssertionError("`$what` is not an array of numbers")
    return items.map { item ->
        (item as? FixtureValue.Number)?.value
            ?: throw AssertionError("`$what` holds a value that is not a number")
    }
}
