package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class WaypointCardFactsTest {
    private val phone = WaypointCardFacts.Line("Phone", "+39", "tel:+39")

    /** A card offering water under the name given, reached by phone, with one link. */
    private fun cardNamingWater(name: String): WaypointCardFacts =
        WaypointCardFacts(
            headline = null,
            services = listOf(WaypointCardFacts.Service(RestSchema.WaypointAmenity("water"), name)),
            timing = emptyList(),
            contact = listOf(phone),
            fields = emptyList(),
            links = listOf(WaypointCardFacts.MediaLink("Topo", " https://a.example ", "https://a.example")),
            photos = emptyList(),
            isEmpty = false,
        )

    @Test
    fun aLineLeavesTheAppOnlyWhereItSaysSoAndTwoCardsCompareByWhatTheyState() {
        assertFalse(phone.isExternal)
        assertNotEquals(WaypointCardFacts.Line("Phone", "+39", "tel:+39", isExternal = true), phone)
        val card = cardNamingWater("Water")

        assertEquals(cardNamingWater("Water"), card)
        assertNotEquals(cardNamingWater("Eau"), card)
        assertNotEquals(
            WaypointCardFacts.MediaLink("Notes", "ask", "ask"),
            WaypointCardFacts.MediaLink("Notes", "ask", null),
        )
    }
}
