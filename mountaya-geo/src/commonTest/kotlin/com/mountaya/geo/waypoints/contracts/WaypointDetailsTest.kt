package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class WaypointDetailsTest {
    private fun details(
        cutoffAt: Instant? = Instant.fromEpochSeconds(1_782_885_600),
        phone: String? = "+33 4 50",
        caption: String? = "The hut",
        format: RestSchema.WaypointPropertyFormat? = null,
    ) = WaypointDetails(
        amenities = listOf(RestSchema.WaypointAmenity.Water),
        description = "A bench by the fountain.",
        timing = WaypointDetails.Timing(cutoffAt, 900),
        contact = WaypointDetails.Contact(phone = phone, website = "https://refuge.example"),
        media =
            WaypointDetails.Media(
                photos = listOf(WaypointDetails.Photo("https://refuge.example/hut.jpg", caption)),
                links = listOf(WaypointDetails.Link("Topo", "https://refuge.example/topo")),
            ),
        properties = listOf(WaypointDetails.Property("beds", "Beds", "12", format)),
    )

    @Test
    fun twoDetailsAreEqualWhenEveryMemberIsAndHashAlike() {
        assertEquals(details(), details())
        assertEquals(details().hashCode(), details().hashCode())
        assertNotEquals(details(), details(phone = null))
        assertNotEquals(details(), details(caption = null))
        assertNotEquals(details(), details(format = RestSchema.WaypointPropertyFormat.Number))
        assertNotEquals(details(), details(cutoffAt = null))
    }

    /**
     * An `Instant` always names an instant, so a cut-off of no number cannot be built; two instants are
     * equal where they name the same one.
     */
    @Test
    fun aCutOffIsComparedAsTheInstantItNames() {
        assertEquals(
            details(cutoffAt = Instant.fromEpochMilliseconds(0)),
            details(cutoffAt = Instant.fromEpochSeconds(0)),
        )
        assertNotEquals(
            details(cutoffAt = Instant.fromEpochSeconds(0)),
            details(cutoffAt = Instant.fromEpochSeconds(1)),
        )
    }

    @Test
    fun aTextIsComparedCodeUnitForCodeUnit() {
        assertNotEquals(details(phone = "café"), details(phone = "café"))
        assertNotEquals(details(caption = "café"), details(caption = "café"))
        assertNotEquals(
            WaypointDetails.Link("Topo", "https://a.example/caf\u00E9"),
            WaypointDetails.Link("Topo", "https://a.example/cafe\u0301"),
        )
    }

    /** An address is the text the wire answers, as written, so one that is no web address is still stated. */
    @Test
    fun anAddressIsKeptAsWritten() {
        assertEquals("refuge dot example", WaypointDetails.Contact(website = "refuge dot example").website)
    }

    @Test
    fun detailsBuiltWithNothingHoldNothing() {
        val empty = WaypointDetails()
        assertNull(empty.amenities)
        assertNull(empty.description)
        assertNull(empty.timing)
        assertNull(empty.contact)
        assertNull(empty.media)
        assertNull(empty.properties)
        assertEquals(WaypointDetails.Timing(cutoffAt = null, stopDuration = null), WaypointDetails.Timing())
        assertNull(WaypointDetails.Media().photos)
        assertNull(WaypointDetails.Media().links)
    }
}
