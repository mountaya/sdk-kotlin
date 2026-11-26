package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class WaypointSummaryTest {
    private val words = WaypointSummaryWords("Services", { "open $it" }, { "cut-off $it" }, { "stop $it" })
    private val english = UnitPreferences("en")
    private val nine = Instant.parse("2026-07-14T09:00:00Z")
    private val spellsNine: (Instant) -> String = { if (it == nine) "09:00" else "?" }

    private fun timing(
        cutoff: Instant? = null,
        stop: Int? = null,
    ) = RestSchema.WaypointTiming {
        cutoff_at = cutoff
        stop_duration = stop
    }

    @Test
    fun theTwoClausesAreJoinedByAMiddleDot() {
        val place = restWaypoint(amenities = listOf("water", "medical-post"), timing = timing(nine, 900))

        assertEquals("Services · stop 15m", waypointSummary(place, words, english))
        assertEquals("Services · cut-off 09:00", waypointSummary(place, words, english, spellsNine))
    }

    @Test
    fun theOpeningHoursWinThenTheCutoffTheCallerCanSpellThenTheStop() {
        val contact = RestSchema.WaypointContact { opening_hours = "8:00 to 18:00" }

        assertEquals(
            "open 8:00 to 18:00",
            waypointSummary(restWaypoint(contact = contact, timing = timing(nine, 900)), words, english, spellsNine),
        )
        assertEquals("cut-off 09:00", waypointSummary(restWaypoint(timing = timing(nine)), words, english, spellsNine))
        assertEquals("stop 15m", waypointSummary(restWaypoint(timing = timing(stop = 900)), words, english))
        val emptyHours = RestSchema.WaypointContact { opening_hours = "" }
        assertEquals(
            "stop 15m",
            waypointSummary(restWaypoint(contact = emptyHours, timing = timing(stop = 900)), words, english),
        )
    }

    @Test
    fun aPlaceWithNothingToSayHasNoSummary() {
        assertNull(waypointSummary(restWaypoint(), words, english))
        assertNull(waypointSummary(restWaypoint(timing = timing(nine)), words, english))
        assertNull(waypointSummary(restWaypoint(timing = timing(stop = 20)), words, english))
        assertNull(
            waypointSummary(
                restWaypoint(
                    contact =
                        RestSchema.WaypointContact {
                            phone = "+33450123456"
                        },
                ),
                words,
                english,
            ),
        )
    }

    @Test
    fun theStopIsStatedInTheReadersLanguage() {
        assertEquals(
            "stop 1h 30min",
            waypointSummary(restWaypoint(timing = timing(stop = 5_400)), words, UnitPreferences("fr")),
        )
    }
}
