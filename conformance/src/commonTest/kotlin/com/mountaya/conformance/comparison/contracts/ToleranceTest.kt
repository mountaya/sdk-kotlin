package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ToleranceTest {
    private fun fixture(tolerance: FixtureValue?): FixtureValue.Object =
        FixtureValue.Object(listOfNotNull(tolerance?.let { "tolerance" to it }))

    private fun stated(
        kind: String,
        value: Double,
    ) = FixtureValue.Object(listOf("kind" to FixtureValue.Text(kind), "value" to FixtureValue.Number(value)))

    private val every =
        listOf(
            Tolerance.Exact,
            Tolerance.Absolute(1.0),
            Tolerance.Relative(1.0),
            Tolerance.Angular(1.0),
        )

    @Test
    fun noStatedToleranceMeansExact() {
        val tolerance = Tolerance.of(fixture(null))

        assertIs<Tolerance.Exact>(tolerance)
        assertTrue(tolerance.accepts(0.1, 0.1))
        assertFalse(tolerance.accepts(0.1, 0.1 + 1e-15))
    }

    @Test
    fun relativeScalesWithTheExpectationAndAnExpectedZeroNeedsAnExactZero() {
        val tolerance = Tolerance.of(fixture(stated("relative", 1e-9)))

        assertIs<Tolerance.Relative>(tolerance)
        assertTrue(tolerance.accepts(6_371_000.0, 6_371_000.001))
        assertFalse(tolerance.accepts(6_371_000.0, 6_371_000.01))
        assertTrue(tolerance.accepts(0.0, 0.0))
        assertTrue(tolerance.accepts(0.0, -0.0))
        assertFalse(tolerance.accepts(0.0, 1e-300))
    }

    @Test
    fun absoluteIsAFixedDistance() {
        val tolerance = Tolerance.of(fixture(stated("absolute", 0.5)))

        assertIs<Tolerance.Absolute>(tolerance)
        assertTrue(tolerance.accepts(10.0, 10.5))
        assertFalse(tolerance.accepts(10.0, 10.6))
    }

    @Test
    fun angularTakesTheShortWayRoundTheCircle() {
        val tolerance = Tolerance.of(fixture(stated("angular", 0.1)))

        assertIs<Tolerance.Angular>(tolerance)
        assertTrue(tolerance.accepts(359.95, 0.03))
        assertTrue(tolerance.accepts(0.03, 359.95))
        assertTrue(tolerance.accepts(-179.95, 180.0))
        assertTrue(tolerance.accepts(90.0, 450.05))
        assertTrue(tolerance.accepts(10.0, 10.1))
        assertFalse(tolerance.accepts(10.0, 10.2))
        assertFalse(tolerance.accepts(359.9, 0.1))
        assertFalse(tolerance.accepts(0.0, 180.0))
    }

    @Test
    fun aNumberThatIsNotFiniteAgreesWithItselfAloneUnderEveryKind() {
        for (tolerance in every) {
            val kind = tolerance::class.simpleName
            assertTrue(tolerance.accepts(Double.NaN, Double.NaN), "$kind: NaN and NaN")
            assertTrue(tolerance.accepts(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), "$kind: +inf")
            assertTrue(tolerance.accepts(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), "$kind: -inf")
            assertFalse(tolerance.accepts(Double.NaN, 0.0), "$kind: NaN expected, 0 given")
            assertFalse(tolerance.accepts(0.0, Double.NaN), "$kind: 0 expected, NaN given")
            assertFalse(tolerance.accepts(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), "$kind: two signs")
            assertFalse(tolerance.accepts(Double.POSITIVE_INFINITY, Double.NaN), "$kind: +inf and NaN")
            assertFalse(tolerance.accepts(Double.POSITIVE_INFINITY, Double.MAX_VALUE), "$kind: +inf and a finite")
            assertFalse(tolerance.accepts(Double.MAX_VALUE, Double.POSITIVE_INFINITY), "$kind: a finite and +inf")
        }
    }

    @Test
    fun anUnknownKindIsRefused() {
        assertFailsWith<AssertionError> { Tolerance.of(fixture(stated("percent", 1.0))) }
    }

    @Test
    fun aToleranceWithNoNumericValueIsRefused() {
        val stated = FixtureValue.Object(listOf("kind" to FixtureValue.Text("absolute")))

        assertFailsWith<AssertionError> { Tolerance.of(fixture(stated)) }
    }
}
