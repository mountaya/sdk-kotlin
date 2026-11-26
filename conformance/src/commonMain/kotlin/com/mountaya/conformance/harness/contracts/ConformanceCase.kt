package com.mountaya.conformance

/** ConformanceCase is one case of a fixture, as a runner receives it. */
public class ConformanceCase(
    /** The case's name, as the fixture writes it. */
    public val name: String,
    /** The case's `input`, or null when the case has none. */
    public val input: FixtureValue?,
    /** The whole fixture, for the `constants`, `defaults` or `tolerance` it states at its root. */
    public val fixture: FixtureValue.Object,
) {
    /** The case's `input` as an object, failing with a sentence when it is anything else. */
    public val fields: FixtureValue.Object
        get() = input as? FixtureValue.Object ?: throw AssertionError("the case's `input` is not an object")

    /** Returns the object the fixture states at its root under [key], failing with a sentence when it states none. */
    public fun root(key: String): FixtureValue.Object =
        fixture.objectOrNull(key) ?: throw AssertionError("the fixture states no `$key`")
}
