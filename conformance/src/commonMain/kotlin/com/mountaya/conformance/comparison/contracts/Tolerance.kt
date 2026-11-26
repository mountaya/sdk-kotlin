package com.mountaya.conformance

import kotlin.math.abs
import kotlin.math.min

/** The degrees in a circle, which an angular tolerance measures the short way round. */
private const val FULL_TURN = 360.0

/**
 * Tolerance is how far a number may be from a fixture's expectation and still match it, in one
 * of the three kinds `sdk/SPEC.md` section 10 defines.
 *
 * A fixture states it at its root. Where it states none, numbers compare exactly. A number that
 * is not finite agrees with itself alone under any tolerance, so `NaN` agrees with `NaN`, which
 * `==` on two `Double` values denies, and an infinity agrees with the same infinity and nothing
 * else.
 */
internal sealed class Tolerance {
    /** Whether [actual] matches [expected]. */
    fun accepts(
        expected: Double,
        actual: Double,
    ): Boolean =
        if (expected.isFinite() && actual.isFinite()) {
            within(expected, actual)
        } else {
            (expected.isNaN() && actual.isNaN()) || expected == actual
        }

    /** Whether two finite numbers are within this tolerance of each other. */
    protected abstract fun within(
        expected: Double,
        actual: Double,
    ): Boolean

    /** Absolute is `abs(actual - expected) <= value`. */
    class Absolute(
        val value: Double,
    ) : Tolerance() {
        override fun within(
            expected: Double,
            actual: Double,
        ): Boolean = abs(actual - expected) <= value
    }

    /**
     * Relative is `abs(actual - expected) <= value * abs(expected)`, so an expected zero accepts
     * only zero.
     */
    class Relative(
        val value: Double,
    ) : Tolerance() {
        override fun within(
            expected: Double,
            actual: Double,
        ): Boolean = abs(actual - expected) <= value * abs(expected)
    }

    /**
     * Angular takes the difference the short way round a circle of 360 degrees:
     * `min(d, 360 - d) <= value`, with `d = abs(actual - expected) mod 360`.
     */
    class Angular(
        val value: Double,
    ) : Tolerance() {
        override fun within(
            expected: Double,
            actual: Double,
        ): Boolean {
            val difference = abs(actual - expected) % FULL_TURN
            return min(difference, FULL_TURN - difference) <= value
        }
    }

    /** Exact is what a fixture with no tolerance asks for. */
    object Exact : Tolerance() {
        override fun within(
            expected: Double,
            actual: Double,
        ): Boolean = actual == expected
    }

    companion object {
        /** Reads the tolerance [fixture] states at its root, failing on a kind the harness does not know. */
        fun of(fixture: FixtureValue.Object): Tolerance {
            val stated = fixture["tolerance"] as? FixtureValue.Object ?: return Exact
            val kind = (stated["kind"] as? FixtureValue.Text)?.value
            val value =
                (stated["value"] as? FixtureValue.Number)?.value
                    ?: throw AssertionError("a tolerance with no numeric value")
            return when (kind) {
                "absolute" -> Absolute(value)
                "relative" -> Relative(value)
                "angular" -> Angular(value)
                else -> throw AssertionError("a tolerance of kind \"$kind\", which the harness does not know")
            }
        }
    }
}
