package com.mountaya.navigation

import com.mountaya.geo.NavigationInstruction

/** RecordingVoice keeps every instruction a navigation hands it, and how often it was hushed. */
class RecordingVoice : NavigationVoice {
    val said = mutableListOf<NavigationInstruction>()
    var hushed = 0
        private set

    override fun say(instruction: NavigationInstruction) {
        said += instruction
    }

    override fun hush() {
        hushed += 1
    }
}
