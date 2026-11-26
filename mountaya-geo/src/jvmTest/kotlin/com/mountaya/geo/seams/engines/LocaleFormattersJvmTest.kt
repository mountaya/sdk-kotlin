package com.mountaya.geo

import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * The jvm's formatters, whose cache android's is, text for text: each thread keeps one formatter per
 * locale and shape, reuses it with no copy, drops the one it used least recently past 64, and never hands
 * one to another thread. The work is counted by identity, a formatter reused being the very same one.
 */
class LocaleFormattersJvmTest {
    /** [read] on a thread of its own, which keeps formatters of its own. */
    private fun <T> onAnotherThread(read: () -> T): T {
        val pool = Executors.newSingleThreadExecutor()
        try {
            return pool.submit(Callable { read() }).get()
        } finally {
            pool.shutdown()
        }
    }

    @Test
    fun aThreadReusesItsFormatterWithNoCopy() {
        assertSame(numberFormatOf("fr-FR", 1, true, 0), numberFormatOf("fr-FR", 1, true, 0))
        val date = dateFormatOf("fr-FR", "Europe/Paris", DateLength.Long, null)
        assertSame(date, dateFormatOf("fr-FR", "Europe/Paris", DateLength.Long, null))
        assertNotSame(numberFormatOf("fr-FR", 1, true, 0), numberFormatOf("fr-FR", 2, true, 0), "another shape")
        assertNotSame(date, dateFormatOf("fr-FR", "Europe/Paris", DateLength.Long, TimeLength.Short), "a time beside")
    }

    @Test
    fun noFormatterIsHandedToAnotherThread() {
        val mine = numberFormatOf("de-DE", 0, true, 0)
        val theirs = onAnotherThread { numberFormatOf("de-DE", 0, true, 0) }

        assertNotSame(mine, theirs)
        assertSame(mine, numberFormatOf("de-DE", 0, true, 0))
    }

    /**
     * Past the bound the formatter used least recently goes, and one in use stays, where a cache emptied
     * when full would build every locale again.
     */
    @Test
    fun pastTheBoundTheFormatterUsedLeastRecentlyIsDroppedAndOneInUseStays() {
        onAnotherThread {
            val kept = numberFormatOf("es-ES", 0, true, 0)
            val dropped = numberFormatOf("it-IT", 0, true, 0)
            for (digits in 1..63) {
                numberFormatOf("en-US", digits % 20, digits % 2 == 0, digits / 20)
                assertSame(kept, numberFormatOf("es-ES", 0, true, 0), "used again after $digits others")
            }
            assertSame(kept, numberFormatOf("es-ES", 0, true, 0))
            assertNotSame(dropped, numberFormatOf("it-IT", 0, true, 0), "the one used least recently was dropped")
        }
    }

    @Test
    fun threadsFormattingAtOnceEachWriteTheirOwnNumbersAndDates() {
        val pool = Executors.newFixedThreadPool(8)
        try {
            val written =
                (0 until 64)
                    .map { task ->
                        pool.submit(
                            Callable {
                                (0 until 200).map { index ->
                                    val value = (task * 1_000 + index).toDouble() + 0.5
                                    localeNumberText(value, "en-US", 1, grouping = true) to
                                        localeDateText(index * 86_400_000L, "en-US", "UTC", DateLength.Short, null)
                                }
                            },
                        )
                    }.map { it.get() }

            for ((task, pairs) in written.withIndex()) {
                for ((index, pair) in pairs.withIndex()) {
                    val value = (task * 1_000 + index).toDouble() + 0.5
                    val expected = String.format(Locale.US, "%,.1f", value)
                    assertEquals(expected, pair.first)
                    val date = localeDateText(index * 86_400_000L, "en-US", "UTC", DateLength.Short, null)
                    assertEquals(date, pair.second)
                }
            }
        } finally {
            pool.shutdown()
        }
    }
}
