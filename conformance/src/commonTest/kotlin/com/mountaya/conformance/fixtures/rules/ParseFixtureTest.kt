package com.mountaya.conformance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ParseFixtureTest {
    @Test
    fun objectsKeepTheFileOrderEvenWhereSortingWouldMoveThem() {
        val parsed = assertIs<FixtureValue.Object>(parseFixture("""{"routes": {"duration": true, "distance": true}}"""))
        val routes = assertIs<FixtureValue.Object>(parsed["routes"])

        assertEquals(listOf("duration", "distance"), routes.keys)
    }

    @Test
    fun nullAnEmptyObjectAndAnAbsentKeyStayDistinct() {
        val parsed = assertIs<FixtureValue.Object>(parseFixture("""{"a": null, "b": {}}"""))

        assertIs<FixtureValue.Null>(parsed["a"])
        assertTrue(assertIs<FixtureValue.Object>(parsed["b"]).entries.isEmpty())
        assertEquals(null, parsed["c"])
    }

    @Test
    fun aWholeNumberAndItsDecimalSpellingAreOneNumberAndALargeIntegerIsExact() {
        val parsed = assertIs<FixtureValue.Array>(parseFixture("[16, 16.0, 1800000000000]"))

        assertEquals(
            listOf(16.0, 16.0, 1_800_000_000_000.0),
            parsed.items.map { assertIs<FixtureValue.Number>(it).value },
        )
    }

    @Test
    fun aNumberJsonCannotCarryIsDecodedWhereverItIsWritten() {
        val parsed =
            assertIs<FixtureValue.Object>(
                parseFixture(
                    """
                    {"input": {"zoom": {"${'$'}number": "NaN"}},
                     "expect": [{"${'$'}number": "Infinity"}, {"${'$'}number": "-Infinity"}]}
                    """,
                ),
            )
        val zoom = assertIs<FixtureValue.Number>(assertIs<FixtureValue.Object>(parsed["input"])["zoom"]).value
        val expect =
            assertIs<FixtureValue.Array>(parsed["expect"]).items.map { assertIs<FixtureValue.Number>(it).value }

        assertTrue(zoom.isNaN())
        assertEquals(listOf(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), expect)
    }

    @Test
    fun anObjectThatIsNotExactlyOneOfTheThreeSpellingsStaysAnObject() {
        val parsed =
            assertIs<FixtureValue.Array>(
                parseFixture(
                    """
                    [{"${'$'}number": "nan"}, {"${'$'}number": 1},
                     {"${'$'}number": "NaN", "unit": "m"}, {"number": "NaN"}]
                    """,
                ),
            )

        for (item in parsed.items) assertIs<FixtureValue.Object>(item)
    }

    @Test
    fun textAndBooleansKeepTheirKind() {
        val parsed = assertIs<FixtureValue.Array>(parseFixture("""["true", true, "١٬٥٣١"]"""))

        assertEquals("true", assertIs<FixtureValue.Text>(parsed.items[0]).value)
        assertEquals(true, assertIs<FixtureValue.Bool>(parsed.items[1]).value)
        assertEquals("١٬٥٣١", assertIs<FixtureValue.Text>(parsed.items[2]).value)
    }
}
