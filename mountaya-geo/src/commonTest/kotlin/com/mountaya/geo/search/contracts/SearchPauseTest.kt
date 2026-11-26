package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SearchPauseTest {
    /**
     * A pause is a length of time, zero or more, and one below zero is no length to wait: no `SearchPause` at all, as a
     * cap outside 1 to 50 is no `SearchLimit`, so no search is built over one (`geo.search.typing-pause`).
     */
    @Test
    fun aPauseIsALengthOfTimeOfZeroOrMoreAndOneBelowZeroIsNone() {
        assertEquals(Duration.ZERO, SearchPause.of(Duration.ZERO)?.duration)
        assertEquals(400.milliseconds, SearchPause.of(400.milliseconds)?.duration)
        assertNull(SearchPause.of((-1).milliseconds))
        assertNull(SearchPause.of((-5).milliseconds))
        assertNull(SearchPause.of(-Duration.INFINITE))
        assertEquals(SearchPause.of(150.milliseconds), SearchPause.of(150.milliseconds))
    }

    @Test
    fun theStandardPauseIsTheContractsTypingPause() {
        assertEquals(searchTypingPause, SearchPause.standard.duration)
        assertEquals(150.milliseconds, SearchPause.standard.duration)
        assertEquals(SearchPause.of(searchTypingPause), SearchPause.standard)
    }
}
