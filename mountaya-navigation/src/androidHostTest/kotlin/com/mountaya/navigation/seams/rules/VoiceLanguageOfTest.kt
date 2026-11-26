package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VoiceLanguageOfTest {
    private val held = setOf("fr-FR", "en-GB")

    @Test
    fun theLanguageIsSpokenWhereTheDeviceHoldsAVoiceForIt() {
        assertEquals("fr-FR", voiceLanguageOf("fr-FR", "en-GB") { it in held })
        assertEquals("fr-FR", voiceLanguageOf("fr-FR", null) { it in held })
    }

    @Test
    fun theFallbackIsSpokenWhereTheDeviceHoldsNoVoiceForTheLanguage() {
        assertEquals("en-GB", voiceLanguageOf("rm-CH", "en-GB") { it in held })
    }

    /** Where the device holds neither, the speech engine's own default voice speaks. */
    @Test
    fun neitherHeldIsTheEnginesOwnVoice() {
        assertNull(voiceLanguageOf("rm-CH", "gsw-CH") { it in held })
        assertNull(voiceLanguageOf("rm-CH", null) { it in held })
    }

    @Test
    fun aFallbackThatIsTheLanguageIsTriedOnce() {
        val tried = mutableListOf<String>()
        assertNull(
            voiceLanguageOf("rm-CH", "rm-CH") { tag ->
                tried += tag
                false
            },
        )
        assertEquals(listOf("rm-CH"), tried)
    }
}
