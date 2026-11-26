package com.mountaya.conformance

/**
 * Holds every constant the case's fixture declares to [rules], the value the SDK's own rule states
 * for it, each read through the public surface, within the fixture's tolerance. A constant the
 * fixture declares and [rules] does not name fails, so a constant the contract adds is caught the
 * day it lands rather than read past.
 *
 * [values] holds the constants that are no single number, a ramp, a table or a word, each as the
 * value the rule states, compared with the fixture's as a case's answer is, within its tolerance.
 * [inputs] names the constants the cases read as their input rather than a rule states, such as the
 * instant every date case renders: known, read by the runner, and held to nothing. [sets] holds the
 * constants the contract writes as the array of a set's members, whose order says nothing
 * (`sdk/SPEC.md` section 10): each member once, and the same members as the rule states.
 */
internal fun holdDeclaredConstants(
    case: ConformanceCase,
    rules: Map<String, Double>,
    values: Map<String, FixtureValue> = emptyMap(),
    inputs: Set<String> = emptySet(),
    sets: Map<String, Set<String>> = emptyMap(),
) {
    val tolerance = Tolerance.of(case.fixture)
    for ((name, declared) in case.root("constants").entries) {
        if (name in inputs) continue
        val set = sets[name]
        if (set != null) {
            val members =
                (declared as? FixtureValue.Array)?.items?.map {
                    (it as? FixtureValue.Text)?.value ?: throw AssertionError("a member of `$name` is not a text")
                } ?: throw AssertionError("the constant `$name` is not the array of a set's members")
            if (members.size != members.toSet().size || members.toSet() != set) {
                throw AssertionError("the fixture declares $name as $members, and the rule states ${set.sorted()}")
            }
            continue
        }
        val stated = values[name]
        if (stated != null) {
            compareFixtureValues(declared, stated, tolerance)?.let { problem ->
                throw AssertionError("the fixture declares $name, and the rule states another value: $problem")
            }
            continue
        }
        val value =
            (declared as? FixtureValue.Number)?.value ?: throw AssertionError("the constant `$name` is not a number")
        val rule = rules[name] ?: throw AssertionError("the fixture declares `$name`, which no rule here states")
        if (!tolerance.accepts(value, rule)) {
            throw AssertionError("the fixture declares $name as $value, and the rule states $rule")
        }
    }
    val undeclared = rules.keys + values.keys + inputs + sets.keys - case.root("constants").keys.toSet()
    if (undeclared.isNotEmpty()) {
        throw AssertionError(
            "the fixture no longer declares ${undeclared.sorted().joinToString(", ")}",
        )
    }
}

/**
 * Holds that the case's fixture declares no constant, for an entry whose rule states none: a constant the
 * contract adds fails the entry the day it lands rather than being read past.
 */
internal fun holdNoConstants(case: ConformanceCase) {
    val declared =
        case.fixture
            .objectOrNull("constants")
            ?.keys
            .orEmpty()
    if (declared.isNotEmpty()) {
        throw AssertionError("the fixture declares ${declared.sorted().joinToString(", ")}, which no rule here states")
    }
}
