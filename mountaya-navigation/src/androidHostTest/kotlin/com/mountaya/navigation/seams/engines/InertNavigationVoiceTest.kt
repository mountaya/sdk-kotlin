package com.mountaya.navigation

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationStep
import com.mountaya.geo.stepInstruction
import kotlin.test.Test

class InertNavigationVoiceTest {
    @Test
    fun anInertVoiceTakesEveryInstructionAndEveryHushInAnyOrderAndSaysNothing() {
        val voice: NavigationVoice = InertNavigationVoice()
        val step = NavigationStep(0, 0, RestSchema.ManeuverType.Goal)

        voice.hush()
        voice.say(stepInstruction(step))
        voice.hush()
        voice.say(stepInstruction(step))
    }
}
