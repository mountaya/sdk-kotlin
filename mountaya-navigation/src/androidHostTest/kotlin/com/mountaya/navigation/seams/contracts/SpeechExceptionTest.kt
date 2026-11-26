package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SpeechExceptionTest {
    @Test
    fun aFailureCarriesItsKindAndWhatThePlatformThrew() {
        val thrown = IllegalStateException("bound to nothing")
        val failure = SpeechException(SpeechException.Kind.EngineUnavailable, thrown)

        assertEquals(SpeechException.Kind.EngineUnavailable, failure.kind)
        assertSame(thrown, failure.cause)
        assertNull(SpeechException(SpeechException.Kind.AudioRefused).cause)
    }

    /** The kinds are one set under one spelling in every SDK, as a log names each. */
    @Test
    fun eachKindIsNamedAsTheOtherSdksNameIt() {
        assertEquals("engineUnavailable", SpeechException.Kind.EngineUnavailable.toString())
        assertEquals("audioRefused", SpeechException.Kind.AudioRefused.value)
    }

    /** A message ends on what to do, for a developer reading a log. */
    @Test
    fun itsMessageSaysWhatToDo() {
        val unavailable = SpeechException(SpeechException.Kind.EngineUnavailable).message.orEmpty()
        assertTrue("settings" in unavailable, unavailable)
        assertTrue("TTS_SERVICE" in unavailable, unavailable)
        assertTrue(unavailable.endsWith("NavigationVoice of the app's own."), unavailable)

        val refused = SpeechException(SpeechException.Kind.AudioRefused).message.orEmpty()
        assertTrue("said anyway" in refused, refused)
        assertTrue(refused.endsWith("asks for it again."), refused)
    }
}
