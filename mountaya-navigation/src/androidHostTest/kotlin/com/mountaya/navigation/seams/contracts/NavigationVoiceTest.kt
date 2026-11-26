package com.mountaya.navigation

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.geo.NavigationStep
import com.mountaya.geo.stepInstruction
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationVoiceTest {
    @Test
    fun aVoiceIsHandedAnInstructionAsAKeyAndItsArgumentsNeverASentence() {
        val voice = RecordingVoice()
        val instruction =
            stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft, name = "Rue"))

        voice.say(instruction)
        voice.hush()

        assertEquals(listOf(NavigationInstructionKey.TurnLeft), voice.said.map { it.key })
        assertEquals("Rue", voice.said.first().name)
        assertEquals(1, voice.hushed)
    }
}
