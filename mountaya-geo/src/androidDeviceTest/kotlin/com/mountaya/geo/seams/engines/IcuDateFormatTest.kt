package com.mountaya.geo

import java.util.Date
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Android writes an instant through `android.icu`, which every release from this SDK's floor carries, since its
 * `java.text` writes a short and a medium time on the device's 12- or 24-hour clock where the person set one, and
 * joins a date and a time with one space. The framework hands that setting to `java.text` as
 * `java.text.DateFormat.is24Hour`, true, false, or null where the person set none, as an emulator leaves it, so no
 * conformance case sees it: this test sets it as the framework does, holds what Geo builds to the locale's own
 * clock under each value, and shows `java.text` following it, the control that the setting reached the platform.
 */
class IcuDateFormatTest {
    /** 2026-03-14T00:00:00Z: 13:00 in Auckland, 20:00 the evening before in New York. */
    private val piDay = Date(1_773_446_400_000L)
    private val korean = gregorian(Locale.forLanguageTag("ko-KR"))
    private val german = gregorian(Locale.forLanguageTag("de-DE"))

    /** Runs [read] with the device's clock set to 24 hours, 12 hours or the locale's, as [is24Hour] says. */
    private fun <T> onDeviceClock(
        is24Hour: Boolean?,
        read: () -> T,
    ): T {
        val setting = java.text.DateFormat::class.java.getField("is24Hour")
        val before = setting.get(null)
        setting.set(null, is24Hour)
        try {
            return read()
        } finally {
            setting.set(null, before)
        }
    }

    @Test
    fun anInstantIsWrittenByIcu() {
        assertIs<android.icu.text.DateFormat>(platformDateFormat(korean, "Pacific/Auckland", null, TimeLength.Short))
        assertIs<android.icu.text.DateFormat>(platformDateFormat(korean, "Pacific/Auckland", DateLength.Long, null))
    }

    @Test
    fun aTimeKeepsTheLocalesClockWhateverClockTheDeviceIsSetTo() {
        for (is24Hour in listOf(true, false, null)) {
            onDeviceClock(is24Hour) {
                fun written(
                    locale: Locale,
                    dateLength: DateLength?,
                    timeLength: TimeLength,
                ): String = platformDateFormat(locale, "Pacific/Auckland", dateLength, timeLength).format(piDay)

                assertEquals("오후 1:00", written(korean, null, TimeLength.Short), "24 hours: $is24Hour")
                assertEquals("13:00", written(german, null, TimeLength.Short), "24 hours: $is24Hour")
                assertEquals("13:00:00", written(german, null, TimeLength.Medium), "24 hours: $is24Hour")
                assertEquals(
                    "2026. 3. 14. 오후 1:00",
                    written(korean, DateLength.Medium, TimeLength.Short),
                    "24 hours: $is24Hour",
                )
            }
        }
    }

    /** The control: the java.text Geo leaves writes the device's clock where one is set. */
    @Test
    fun javaTextWritesTheDevicesClockWhereOneIsSet() {
        fun written(locale: Locale): String =
            javaTextDateFormat(locale, "Pacific/Auckland", null, TimeLength.Short).format(piDay)

        val korean24 = onDeviceClock(true) { written(korean) }
        val german12 = onDeviceClock(false) { written(german) }

        assertTrue("13" in korean24 && "오후" !in korean24, korean24)
        assertTrue("13" !in german12 && "1:00" in german12, german12)
    }

    /** German joins a medium date and a time with a comma, where android's java.text writes one space. */
    @Test
    fun aDateAndATimeAreJoinedAsTheLocalesDataJoinsThem() {
        val icu = platformDateFormat(german, "America/New_York", DateLength.Medium, TimeLength.Short).format(piDay)
        val javaText = javaTextDateFormat(german, "America/New_York", DateLength.Medium, TimeLength.Short).format(piDay)

        assertEquals("13.03.2026, 20:00", icu)
        assertEquals("13.03.2026 20:00", javaText)
    }
}
