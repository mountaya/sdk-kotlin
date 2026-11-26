package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WaypointPropertyValueTest {
    private val english = UnitPreferences("en")

    private fun field(
        value: String,
        format: String?,
    ): WaypointDetails.Property =
        WaypointDetails.Property("field", "Field", value, format?.let { RestSchema.WaypointPropertyFormat(it) })

    @Test
    fun aDurationADistanceAndANumberAreWrittenByTheFormatters() {
        assertEquals("1h", waypointPropertyValue(field("3600", "duration"), english))
        assertEquals("3.6 km", waypointPropertyValue(field("3600", "distance"), english))
        assertEquals("3,6\u202Fkm", waypointPropertyValue(field("3600", "distance"), UnitPreferences("fr")))
        assertEquals(
            "2.2 mi",
            waypointPropertyValue(
                field("3600", "distance"),
                UnitPreferences("en", length = RestSchema.UnitLength.Imperial),
            ),
        )
        assertEquals("3,600", waypointPropertyValue(field("3600", "number"), english))
        assertEquals("1,234.568", waypointPropertyValue(field("1234.56789", "number"), english))
        assertEquals("16", waypointPropertyValue(field(" 0x10 ", "number"), english))
    }

    @Test
    fun aValueItsFormatCannotReadAnyOtherFormatAndNoFormatReadAsWritten() {
        assertEquals("abc", waypointPropertyValue(field("abc", "number"), english))
        assertEquals("about an hour", waypointPropertyValue(field("about an hour", "duration"), english))
        assertEquals("Infinity", waypointPropertyValue(field("Infinity", "distance"), english))
        assertEquals("3600", waypointPropertyValue(field("3600", "text"), english))
        assertEquals("3600", waypointPropertyValue(field("3600", null), english))
        assertEquals("3600", waypointPropertyValue(field("3600", "url"), english))
    }

    @Test
    fun anInstantIsWrittenOnlyThroughTheCallersFormatterAndOneThatIsNoneAsWritten() {
        val date = field("2026-08-29T10:30:00Z", "date")

        assertEquals("2026-08-29T10:30:00Z", waypointPropertyValue(date, english))
        assertEquals("29 August, 10:30", waypointPropertyValue(date, english) { "29 August, 10:30" })
        assertEquals("soon", waypointPropertyValue(field("soon", "date"), english) { "never" })
        assertNotNull(rfc3339Instant("2026-08-29T10:30:00.250Z"), "the instant is read")
        assertNull(rfc3339Instant("29/08"))
    }

    /**
     * A date is read by the client's anchor, the one grammar every timestamp the wire carries is read by, as
     * `sdk-swift` and `sdk-dart` read it, never by `Instant.parse`, which takes forms the anchor does not: a letter of
     * the wrong case and an offset to the second name no instant, and read as written.
     */
    @Test
    fun aDateIsReadByTheClientsAnchorAndAFormItRefusesReadsAsWritten() {
        val formatted: (kotlin.time.Instant) -> String = { "formatted" }

        assertEquals("formatted", waypointPropertyValue(field("2026-08-29T12:30:00+02:00", "date"), english, formatted))
        for (value in listOf("2026-08-29t10:30:00z", "2026-08-29T10:30:00z", "2026-08-29T12:30:00+02:00:00")) {
            assertEquals(value, waypointPropertyValue(field(value, "date"), english, formatted), value)
            assertNull(rfc3339Instant(value), value)
        }
    }
}
