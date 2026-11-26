package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CompareFixtureValuesTest {
    private fun compare(
        expected: String,
        actual: String,
        tolerance: Tolerance = Tolerance.Exact,
    ) = compareFixtureValues(parseFixture(expected), parseFixture(actual), tolerance)

    @Test
    fun equalValuesMatch() {
        assertNull(compare("""{"a": [1, "two", true, null]}""", """{"a": [1, "two", true, null]}"""))
    }

    @Test
    fun anObjectsKeyOrderIsNotCompared() {
        assertNull(compare("""{"a": 1, "b": 2}""", """{"b": 2, "a": 1}"""))
    }

    @Test
    fun theFirstDifferenceIsNamedByItsPath() {
        assertEquals(
            "at $.routes[1].distance: expected 1035.0, got 1036.0",
            compare(
                """{"routes": [{"distance": 1}, {"distance": 1035}]}""",
                """{"routes": [{"distance": 1}, {"distance": 1036}]}""",
            ),
        )
    }

    @Test
    fun nullIsNotAnAbsentKey() {
        assertEquals("at $: expected the keys [a], got []", compare("""{"a": null}""", "{}"))
    }

    @Test
    fun anArrayIsCompared() {
        assertEquals("at $: expected 2 items, got 1", compare("[1, 2]", "[1]"))
    }

    @Test
    fun numbersCompareWithinTheTolerance() {
        assertNull(compare("[1000000]", "[1000000.0005]", Tolerance.Relative(1e-9)))
        assertEquals(
            "at $[0]: expected 1000000.0, got 1000000.01",
            compare("[1000000]", "[1000000.01]", Tolerance.Relative(1e-9)),
        )
    }

    @Test
    fun aNumberThatIsNotFiniteMatchesItselfAloneWhereverItSits() {
        val nan = """{"zoom": {"${'$'}number": "NaN"}}"""
        val infinity = """{"zoom": {"${'$'}number": "Infinity"}}"""

        assertNull(compare(nan, nan, Tolerance.Relative(1e-9)))
        assertNull(compare(infinity, infinity, Tolerance.Absolute(1e-9)))
        assertEquals("at $.zoom: expected NaN, got 0.0", compare(nan, """{"zoom": 0}""", Tolerance.Absolute(1.0)))
        assertEquals("at $.zoom: expected Infinity, got NaN", compare(infinity, nan))
    }

    @Test
    fun anAngularToleranceComparesTheShortWayRound() {
        assertNull(compare("""{"azimuth": 359.95}""", """{"azimuth": 0.03}""", Tolerance.Angular(0.1)))
    }

    @Test
    fun aNumberIsNotItsText() {
        assertEquals("at $: expected 1.0, got \"1\"", compare("1", "\"1\""))
    }

    /** A set's members match its expected items in any order, each item matched once. */
    @Test
    fun theMembersOfASetMatchTheExpectedItemsInAnyOrderEachOnce() {
        fun members(
            expected: String,
            vararg actual: String,
        ) = compareFixtureMembers(parseFixture(expected), actual.map { parseFixture(it) }, Tolerance.Exact)

        assertNull(members("""["a", "b", "c"]""", "\"c\"", "\"a\"", "\"b\""))
        assertNull(members("""["a", "a"]""", "\"a\"", "\"a\""))
        assertEquals("at $: expected 2 members, got 1", members("""["a", "b"]""", "\"a\""))
        assertEquals(
            "at $[1]: expected \"a\" among the members, got \"b\"",
            members("""["a", "a"]""", "\"a\"", "\"b\""),
        )
        assertEquals("at $: expected {}, got the members of a set", members("{}"))
    }
}
