package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FixturePositionTest {
    @Test
    fun anArrayOfNumbersIsReadAsShortAsItIsWritten() {
        assertEquals(listOf(6.8652, 45.9237), fixturePosition(parseFixture("[6.8652, 45.9237]"), "point"))
        assertEquals(listOf(6.9), fixturePosition(parseFixture("[6.9]"), "point"))
        assertEquals(emptyList(), fixturePosition(parseFixture("[]"), "point"))
        val unplaced = fixturePosition(parseFixture("""[{"${'$'}number": "NaN"}, 45]"""), "point")
        assertTrue(unplaced[0].isNaN())
    }

    @Test
    fun anythingElseFailsNamingWhatWasRead() {
        val notAnArray = assertFailsWith<AssertionError> { fixturePosition(parseFixture("6.9"), "center") }
        assertEquals("`center` is not an array of numbers", notAnArray.message)
        val notNumbers = assertFailsWith<AssertionError> { fixturePosition(parseFixture("""[6.9, "45"]"""), "edges") }
        assertEquals("`edges` holds a value that is not a number", notNumbers.message)
        assertFailsWith<AssertionError> { fixturePosition(null, "line") }
    }
}
