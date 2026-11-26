package com.mountaya.navigation

/**
 * SpeechException is a failure of the platform's speech, which `SpeechNavigationVoice` tells its `onError`:
 * the platform's text-to-speech engine did not start, so the voice says nothing, now or later; or the
 * platform refused the audio focus a sentence asked for, and the sentence is said anyway, over what other
 * apps play.
 *
 * Its message says what to do, for a developer reading a log; a caller reads [kind]. The kinds are the ones
 * `sdk-dart`'s `SpeechException` and `sdk-swift`'s `SpeechError` carry.
 */
public class SpeechException(
    /** Which failure it is. */
    public val kind: Kind,
    /** What the platform threw, where it threw anything. */
    cause: Throwable? = null,
) : Exception(messageOf(kind), cause) {
    /**
     * Kind is every failure of the platform's speech this build knows.
     *
     * It is open, so a `when` over it ends in `else`: the set grows where the speech meets another.
     */
    @JvmInline
    public value class Kind internal constructor(
        /** The failure, as a log names it. */
        public val value: String,
    ) {
        override fun toString(): String = value

        /** The failures this build knows. */
        public companion object {
            /**
             * The platform's text-to-speech engine did not start, or the device holds none the app can reach, so the
             * voice says nothing, now or later. It is told once per voice.
             */
            public val EngineUnavailable: Kind = Kind("engineUnavailable")

            /**
             * The platform refused the audio focus a sentence asked for, so it is said over what other apps play. It
             * is told once per refusal, and the next sentence asks again.
             */
            public val AudioRefused: Kind = Kind("audioRefused")
        }
    }
}

/** The message a failure of [kind] is logged with, ending on what to do. */
private fun messageOf(kind: SpeechException.Kind): String =
    when (kind) {
        SpeechException.Kind.EngineUnavailable -> {
            "The platform's text-to-speech engine did not start, so nothing is said; install or enable a speech " +
                "engine in the system's settings, keep the TTS_SERVICE query mountaya-navigation's manifest " +
                "declares, or speak through a NavigationVoice of the app's own."
        }

        SpeechException.Kind.AudioRefused -> {
            "The platform refused the audio focus for a sentence, which is said anyway, over what other apps play; " +
                "another app holds the focus, a call's among them, and the next sentence asks for it again."
        }

        else -> {
            "The platform's speech failed ($kind)."
        }
    }
