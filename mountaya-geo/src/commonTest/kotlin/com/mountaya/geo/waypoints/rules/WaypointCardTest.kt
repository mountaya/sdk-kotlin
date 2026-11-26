package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class WaypointCardTest {
    private val english = UnitPreferences("en")
    private val standard = ElementsScope.standard

    @Test
    fun everyContactLineInItsOrderAPhoneAndAnEmailWithTheirScheme() {
        val card =
            waypointCard(
                WaypointDetails(
                    contact =
                        WaypointDetails.Contact(
                            address = "Val Veny",
                            bookingUrl = "https://refuge.example/book",
                            email = "refuge@example.com",
                            openingHours = "June to September",
                            phone = "+39 0165",
                            website = "https://refuge.example",
                        ),
                ),
                standard,
                english,
            )

        assertEquals(
            listOf("Opening hours", "Address", "Phone", "Email", "Website", "Booking page"),
            card.contact.map { it.label },
        )
        assertEquals(
            listOf(
                null,
                null,
                "tel:+39 0165",
                "mailto:refuge@example.com",
                "https://refuge.example",
                "https://refuge.example/book",
            ),
            card.contact.map { it.href },
        )
        assertEquals(listOf(false, false, false, false, true, true), card.contact.map { it.isExternal })
    }

    @Test
    fun aWebsiteOrBookingPageThatIsNoWebAddressIsStatedAsText() {
        val card =
            waypointCard(
                WaypointDetails(
                    contact =
                        WaypointDetails.Contact(
                            bookingUrl = "javascript:alert(1)",
                            website = "refuge dot example",
                        ),
                ),
                standard,
                english,
            )

        assertEquals(listOf("refuge dot example", "javascript:alert(1)"), card.contact.map { it.value })
        assertTrue(card.contact.all { it.href == null && !it.isExternal })
    }

    @Test
    fun aCutoffOnlyThroughTheCallersFormatterThenTheStopOneOfNoMinuteNone() {
        val details =
            WaypointDetails(
                timing = WaypointDetails.Timing(cutoffAt = Instant.fromEpochSeconds(0), stopDuration = 1_200),
            )

        assertEquals(
            listOf(WaypointCardFacts.Line("Cut-off time", "10:30"), WaypointCardFacts.Line("Planned stop", "20m")),
            waypointCard(details, standard, english) { "10:30" }.timing,
        )
        assertEquals(listOf("Planned stop"), waypointCard(details, standard, english).timing.map { it.label })
        val brief = WaypointDetails(timing = WaypointDetails.Timing(stopDuration = 20))
        assertTrue(waypointCard(brief, standard, english).timing.isEmpty())
    }

    @Test
    fun servicesByNameOneWithNoneByItsValueAndFieldsByTheirFormat() {
        val card =
            waypointCard(
                WaypointDetails(
                    amenities = listOf(RestSchema.WaypointAmenity("water"), RestSchema.WaypointAmenity("sauna")),
                    properties =
                        listOf(
                            WaypointDetails.Property(
                                "page",
                                "Page",
                                " https://a.example ",
                                RestSchema.WaypointPropertyFormat.Url,
                            ),
                        ),
                ),
                standard,
                english,
            )

        assertEquals(listOf("Water", "sauna"), card.services.map { it.name })
        assertEquals(listOf(WaypointField("page", "Page", " https://a.example ", "https://a.example")), card.fields)
    }

    @Test
    fun linksAnchoredOnlyAtAWebAddressPhotosLeftOutAtAnyOtherAndTheHeadline() {
        val card =
            waypointCard(
                WaypointDetails(
                    amenities = listOf(RestSchema.WaypointAmenity("water")),
                    timing = WaypointDetails.Timing(stopDuration = 900),
                    media =
                        WaypointDetails.Media(
                            photos =
                                listOf(
                                    WaypointDetails.Photo(" https://a.example/hut.jpg"),
                                    WaypointDetails.Photo("data:x"),
                                ),
                            links =
                                listOf(
                                    WaypointDetails.Link("Topo", " https://a.example/topo "),
                                    WaypointDetails.Link("Notes", "ask"),
                                ),
                        ),
                ),
                standard,
                english,
            )

        assertEquals(listOf("https://a.example/topo", null), card.links.map { it.href })
        assertEquals(listOf("https://a.example/hut.jpg"), card.photos.map { it.url })
        assertEquals("Services \u00B7 15m stop", card.headline)
        assertFalse(card.isEmpty)
        val described = waypointCard(WaypointDetails(description = "  Closed in winter. "), standard, english)
        assertEquals("Closed in winter.", described.headline)
        assertTrue(described.isEmpty)
    }

    @Test
    fun aPlaceThatRecordsNothingStatesNothing() {
        val card = waypointCard(null, standard, english)

        assertTrue(card.services.isEmpty())
        assertTrue(card.timing.isEmpty())
        assertTrue(card.contact.isEmpty())
        assertTrue(card.fields.isEmpty())
        assertNull(card.headline)
        assertTrue(card.isEmpty)
    }
}
