package com.mountaya.geo

import com.mountaya.conformance.ConformanceCase
import com.mountaya.conformance.FixtureValue

/**
 * Holds every constant the case's fixture declares to [stated], the value the rule states for it,
 * compared exactly, as a fixture's constants are the rule's own figures and words. A constant the fixture
 * declares and [stated] does not name fails, and so does one [stated] names that the fixture no longer
 * declares, so a constant the contract adds or drops is caught the day it lands.
 *
 * The conformance module's own holder reads only the public surface, and the rules of the entries this
 * module runs are internal, so its runners hold their constants here.
 */
internal fun holdConstantsExactly(
    case: ConformanceCase,
    stated: Map<String, FixtureValue>,
) {
    val declared = case.root("constants")
    for ((name, value) in declared.entries) {
        val rule = stated[name] ?: throw AssertionError("the fixture declares `$name`, which no rule here states")
        sameValue(value, rule, "`$name`")
    }
    val undeclared = stated.keys - declared.keys.toSet()
    if (undeclared.isNotEmpty()) {
        throw AssertionError("the fixture no longer declares ${undeclared.sorted().joinToString(", ")}")
    }
}

/** Fails where [actual] is not [expected]: the same text, number, truth, items in order, or keys whatever their order. */
private fun sameValue(
    expected: FixtureValue,
    actual: FixtureValue,
    at: String,
) {
    val same =
        when (expected) {
            is FixtureValue.Null -> {
                actual is FixtureValue.Null
            }

            is FixtureValue.Bool -> {
                actual is FixtureValue.Bool && actual.value == expected.value
            }

            is FixtureValue.Number -> {
                actual is FixtureValue.Number && actual.value == expected.value
            }

            is FixtureValue.Text -> {
                actual is FixtureValue.Text && actual.value == expected.value
            }

            is FixtureValue.Array -> {
                if (actual !is FixtureValue.Array || actual.items.size != expected.items.size) {
                    false
                } else {
                    expected.items.indices.forEach { sameValue(expected.items[it], actual.items[it], "$at[$it]") }
                    true
                }
            }

            is FixtureValue.Object -> {
                if (actual !is FixtureValue.Object || actual.keys.toSet() != expected.keys.toSet()) {
                    false
                } else {
                    expected.entries.forEach { (key, value) ->
                        sameValue(value, actual[key] ?: FixtureValue.Null, "$at.$key")
                    }
                    true
                }
            }
        }
    if (!same) throw AssertionError("the fixture declares $at, and the rule states another value")
}
