package com.mountaya.elements

import androidx.compose.ui.input.key.Key
import com.mountaya.client.InternalMountayaApi
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.ElementsScope
import com.mountaya.geo.ElementsScopeStatement
import com.mountaya.geo.TimeSliderReadout
import com.mountaya.geo.scopeOf
import com.mountaya.geo.timeSliderReadout
import com.mountaya.geo.timeSliderWindow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * A layers panel's time section on the host JVM, Compose left out: where a stop stands on the track and the stop under a
 * press, an arrow key's step, the caption, and the thumb's readout in the scope's words.
 */
@OptIn(InternalMountayaApi::class)
class TimeSliderTest {
    private val french = scopeOf(ElementsScope.standard, ElementsScopeStatement(language = RestSchema.Language.Fr))

    /** A moment written as its epoch milliseconds, so the readout's own joining is what a case reads. */
    private val moment: (Instant) -> String = { "@${it.toEpochMilliseconds()}" }

    @Test
    fun aStopStandsAcrossTheTrackAThumbInFromEitherEndAndAPressReadsTheStopUnderIt() {
        val width = 456f
        val radius = 18f

        assertEquals(radius, timeSliderX(0, width, radius, 215))
        assertEquals(width - radius, timeSliderX(215, width, radius, 215))
        for (stop in 0..215) {
            assertEquals(
                stop,
                timeSliderStopAt(timeSliderX(stop, width, radius, 215), width, radius, 215),
            )
        }
        assertEquals(0, timeSliderStopAt(-40f, width, radius, 215))
        assertEquals(215, timeSliderStopAt(width + 40f, width, radius, 215))
        assertEquals(0, timeSliderStopAt(10f, 20f, radius, 215), "a track narrower than its thumb holds the first stop")
    }

    @Test
    fun anArrowKeyMovesTheThumbAStopHeldToTheTrackAndNoOtherKeyMovesIt() {
        assertEquals(-1, timeSliderKeyStep(Key.DirectionLeft))
        assertEquals(-1, timeSliderKeyStep(Key.DirectionDown))
        assertEquals(1, timeSliderKeyStep(Key.DirectionRight))
        assertEquals(1, timeSliderKeyStep(Key.DirectionUp))
        assertNull(timeSliderKeyStep(Key.Enter))
        assertEquals(72, timeSliderStepped(71, 1, 215))
        assertEquals(0, timeSliderStepped(0, -1, 215))
        assertEquals(215, timeSliderStepped(215, 1, 215))
    }

    @Test
    fun theCaptionSaysWhatTheSliderDrivesTheKeysOnlyWhereAKeyboardIsAtHandAndTheZone() {
        assertEquals(
            "Choose the time for time-aware layers. Use ← → to adjust by the hour. Times shown in your local time zone.",
            timeCaption(ElementsScope.standard, hasKeyboard = true),
        )
        assertEquals(
            "Choose the time for time-aware layers. Times shown in your local time zone.",
            timeCaption(ElementsScope.standard, hasKeyboard = false),
        )
        assertEquals(
            "Choisissez l'heure des calques temporels. Heures dans votre fuseau horaire local.",
            timeCaption(french, hasKeyboard = false),
        )
    }

    @Test
    fun theReadoutIsTheMomentThenItsOffsetCountedInTheScopesWords() {
        val window = timeSliderWindow(Instant.fromEpochMilliseconds(1773480413900))

        fun read(
            value: Int,
            scope: ElementsScope = ElementsScope.standard,
        ): String = timeReadoutText(timeSliderReadout(window, value), scope, moment)

        assertEquals("@1773480413900 · Now", read(71))
        assertEquals("@1773484013900 · in 1 hour", read(72))
        assertEquals("@1773397613900 · 23 hours ago", read(48))
        assertEquals("@1773566813900 · in 1 day", read(95))
        assertEquals("@1773307613900 · 2 days ago", read(23))
        assertEquals("@1773998813900 · dans 6 jours", read(215, french))
        assertEquals("@1773476813900 · il y a 1 heure", read(70, french))
        assertEquals("@1773480413900 · Maintenant", read(71, french))
    }

    @Test
    fun aStopOfNoInstantReadsItsOffsetAloneAndThePanelsOwnStringsStandOverItsWords() {
        val none = timeSliderWindow(Instant.fromEpochMilliseconds(8_640_000_000_000_000))
        val own =
            ElementsScope.standard.withStrings(
                mapOf(
                    "layers.timeHoursAhead" to "+{count} h",
                    "layers.timeNow" to "Live",
                ),
            )

        assertEquals("in 9 hours", timeReadoutText(timeSliderReadout(none, 80), ElementsScope.standard, moment))
        assertEquals("Now", timeReadoutText(timeSliderReadout(none, 71), ElementsScope.standard, moment))
        assertEquals("+1,234 h", timeReadoutText(TimeSliderReadout(null, "layers.timeHoursAhead", 1234), own, moment))
        assertEquals("Live", timeReadoutText(TimeSliderReadout(null, "layers.timeNow", null), own, moment))
    }
}
