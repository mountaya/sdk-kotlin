package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class FixtureValueTest {
    @Test
    fun anObjectKeepsItsKeysInTheOrderTheyWereWritten() {
        val value = FixtureValue.Object(listOf("zoom" to FixtureValue.Number(6.0), "center" to FixtureValue.Null))

        assertEquals(listOf("zoom", "center"), value.keys)
    }

    @Test
    fun anAbsentKeyIsNotNull() {
        val value = FixtureValue.Object(listOf("present" to FixtureValue.Null))

        assertSame(FixtureValue.Null, value["present"])
        assertNull(value["absent"])
    }

    private val input =
        FixtureValue.Object(
            listOf(
                "delayMs" to FixtureValue.Number(500.0),
                "abort" to FixtureValue.Text("during"),
                "idempotent" to FixtureValue.Bool(true),
                "errors" to FixtureValue.Array(listOf(FixtureValue.Null)),
                "constants" to FixtureValue.Object(emptyList()),
                "status" to FixtureValue.Null,
            ),
        )

    @Test
    fun eachFieldReaderAnswersTheValueOfItsOwnType() {
        assertEquals(500.0, input.number("delayMs"))
        assertEquals("during", input.text("abort"))
        assertEquals(true, input.bool("idempotent"))
        assertEquals(listOf<FixtureValue>(FixtureValue.Null), input.items("errors"))
        assertEquals(emptyList(), input.objectOrNull("constants")?.entries)
    }

    @Test
    fun anOptionalReaderAnswersNullForAnAbsentKeyAndForNull() {
        assertNull(input.numberOrNull("status"))
        assertNull(input.numberOrNull("absent"))
        assertNull(input.textOrNull("absent"))
        assertNull(input.objectOrNull("status"))
    }

    @Test
    fun aRequiredReaderNamesTheKeyItDidNotFind() {
        assertEquals("`absent` is absent", assertFailsWith<AssertionError> { input.number("absent") }.message)
        assertEquals("`status` is absent", assertFailsWith<AssertionError> { input.text("status") }.message)
    }

    @Test
    fun aReaderRefusesAValueOfAnotherType() {
        assertEquals("`abort` is not a number", assertFailsWith<AssertionError> { input.number("abort") }.message)
        assertEquals(
            "`delayMs` is not a string",
            assertFailsWith<AssertionError> { input.textOrNull("delayMs") }.message,
        )
        assertEquals("`abort` is not a boolean", assertFailsWith<AssertionError> { input.bool("abort") }.message)
        assertEquals("`abort` is not an array", assertFailsWith<AssertionError> { input.items("abort") }.message)
        assertEquals(
            "`abort` is not an object",
            assertFailsWith<AssertionError> { input.objectOrNull("abort") }.message,
        )
    }
}
