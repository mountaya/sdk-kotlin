package com.mountaya.conformance

/**
 * ConformanceOutcome is what a runner answers a case with: a value, the members of a set, or a refusal, and
 * nothing else.
 *
 * There is no skip. A scope covers an entry whole, on every target, and an entry runs whole on a
 * target or not at all there (SPEC.md section 9), so no case is ever answered with less than a
 * value or a refusal.
 */
public sealed interface ConformanceOutcome {
    /** Value is an answer in the shape of the case's `expect`. */
    public class Value(
        /** The answer. */
        public val value: FixtureValue,
    ) : ConformanceOutcome

    /**
     * Members is an answer a fixture compares as a set: the array of [members] in whatever order the SDK lists
     * them, each matched by one of the case's `expect` items and the two of one size, as a fixture states where
     * the order a renderer lists its values in is its own.
     */
    public class Members(
        /** The answer's members, in the order the SDK lists them. */
        public val members: List<FixtureValue>,
    ) : ConformanceOutcome

    /** Refused is a refusal, named by its class as the contract spells it, never by its message. */
    public class Refused(
        /** The class, such as `ConfigurationError`. */
        public val name: String,
    ) : ConformanceOutcome
}
