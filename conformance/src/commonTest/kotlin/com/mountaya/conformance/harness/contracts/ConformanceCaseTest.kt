package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class ConformanceCaseTest {
    @Test
    fun aCaseCarriesItsInputAndTheWholeFixture() {
        val fixture = FixtureValue.Object(listOf("constants" to FixtureValue.Object(emptyList())))
        val input = FixtureValue.Text("data")
        val case = ConformanceCase("data", input, fixture)

        assertEquals("data", case.name)
        assertSame(input, case.input)
        assertSame(fixture, case.fixture)
    }

    @Test
    fun aCaseWithNoInputSaysSo() {
        assertNull(ConformanceCase("empty", null, FixtureValue.Object(emptyList())).input)
    }

    @Test
    fun theFieldsAreTheInputReadAsAnObject() {
        val input = FixtureValue.Object(listOf("delayMs" to FixtureValue.Number(5.0)))

        assertSame(input, ConformanceCase("object", input, FixtureValue.Object(emptyList())).fields)
    }

    @Test
    fun anInputThatIsNotAnObjectHasNoFields() {
        val case = ConformanceCase("text", FixtureValue.Text("data"), FixtureValue.Object(emptyList()))

        assertEquals("the case's `input` is not an object", assertFailsWith<AssertionError> { case.fields }.message)
    }

    @Test
    fun theRootReadsWhatTheFixtureStatesBesideItsCases() {
        val constants = FixtureValue.Object(listOf("nowMs" to FixtureValue.Number(1.0)))
        val case = ConformanceCase("root", null, FixtureValue.Object(listOf("constants" to constants)))

        val absent = assertFailsWith<AssertionError> { case.root("defaults") }

        assertSame(constants, case.root("constants"))
        assertEquals("the fixture states no `defaults`", absent.message)
    }
}
