package com.mountaya.navigation

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationInstruction
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.geo.NavigationStep
import com.mountaya.geo.stepInstruction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The voice's own order on the host, over a speech engine the test starts when it chooses: what is said
 * before the platform's engine has started waits for it, a hush, a failed start or a close drops what
 * waits, and a failed start and each refusal of the audio focus are told to the app, or dropped where it
 * gave no `onError`. The platform's engine itself is the device test's.
 */
class SpeechNavigationVoiceTest {
    /**
     * A speech engine that starts when the test says, keeps every sentence it is handed, refuses the audio focus
     * while [refusesFocus] says, and keeps every failure the voice dropped.
     */
    private class ScriptedSpeech : SpeechNavigationVoice.Speech {
        private var started: ((Boolean) -> Unit)? = null
        val said = mutableListOf<String>()
        val utterances = mutableListOf<UUID>()
        val dropped = mutableListOf<Throwable>()
        var refusesFocus = false
        var stops = 0
            private set
        var shutdowns = 0
            private set

        /** Reports the engine started, or failed to where [ready] is false. */
        fun finishStarting(ready: Boolean = true) {
            started?.invoke(ready)
        }

        override fun start(started: (Boolean) -> Unit) {
            this.started = started
        }

        override fun speak(
            text: String,
            id: UUID,
        ): Boolean {
            said += text
            utterances += id
            return !refusesFocus
        }

        override fun stop() {
            stops += 1
        }

        override fun shutdown() {
            shutdowns += 1
        }

        override fun dropped(failure: Throwable) {
            dropped += failure
        }
    }

    private fun turn(name: String): NavigationInstruction =
        stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft, name = name))

    private val arrival = stepInstruction(NavigationStep(1, 1, RestSchema.ManeuverType.Goal))

    /** The host's words, a turn said with its name and an arrival not said at all, a failure told to [onError]. */
    private fun voice(
        speech: ScriptedSpeech,
        onError: ((Throwable) -> Unit)? = null,
    ): SpeechNavigationVoice =
        SpeechNavigationVoice(speech, onError) { instruction ->
            when (instruction.key) {
                NavigationInstructionKey.TurnLeft -> "Tournez à gauche sur ${instruction.name}"
                NavigationInstructionKey.GoalPlain -> ""
                else -> null
            }
        }

    @Test
    fun whatIsSaidBeforeTheEngineHasStartedWaitsAndIsSaidOnceItHasInOrder() {
        val speech = ScriptedSpeech()
        val voice = voice(speech)

        voice.say(turn("Sentier"))
        voice.say(arrival)
        voice.say(turn("Crête"))
        assertEquals(emptyList(), speech.said, "nothing reaches an engine that has not started")

        speech.finishStarting()
        voice.say(turn("Refuge"))

        assertEquals(
            listOf("Tournez à gauche sur Sentier", "Tournez à gauche sur Crête", "Tournez à gauche sur Refuge"),
            speech.said,
        )
        assertEquals(listOf(UUID(1L, 1L), UUID(1L, 2L), UUID(1L, 3L)), speech.utterances)
        voice.close()
    }

    @Test
    fun aHushDropsWhatWaitsForTheEngineAndWhatIsSaidAfterItWaitsAgain() {
        val speech = ScriptedSpeech()
        val voice = voice(speech)

        voice.say(turn("Sentier"))
        voice.hush()
        voice.say(turn("Crête"))
        speech.finishStarting()

        assertEquals(listOf("Tournez à gauche sur Crête"), speech.said)
        assertEquals(1, speech.stops)
        voice.close()
    }

    @Test
    fun anEngineThatFailsToStartSaysNothingThatWaitedAndNothingAfter() {
        val speech = ScriptedSpeech()
        val voice = voice(speech)

        voice.say(turn("Sentier"))
        speech.finishStarting(ready = false)
        voice.say(turn("Crête"))
        speech.finishStarting()

        assertEquals(emptyList(), speech.said)
        assertEquals(false, voice.isStarted)
        voice.close()
    }

    /**
     * A voice that will never speak says so, rather than dropping every sentence without a word: the failure
     * reaches the app's onError once, however often the platform reports it.
     */
    @Test
    fun anEngineThatFailsToStartIsToldToOnErrorOnce() {
        val speech = ScriptedSpeech()
        val failures = mutableListOf<Throwable>()
        val voice = voice(speech) { failures += it }

        assertNull(voice.isStarted)
        speech.finishStarting(ready = false)
        speech.finishStarting(ready = false)

        assertEquals(SpeechException.Kind.EngineUnavailable, assertIs<SpeechException>(failures.single()).kind)
        voice.close()
    }

    /**
     * A refused focus leaves the sentence said over what other apps play, as `sdk-swift` and `sdk-dart` say it, and
     * tells the app each refusal, the sentences that waited for the engine included, as each is handed on.
     */
    @Test
    fun eachRefusalOfTheAudioFocusIsToldOnceAndItsSentenceIsSaidAnyway() {
        val speech = ScriptedSpeech()
        val failures = mutableListOf<Throwable>()
        val voice = voice(speech) { failures += it }
        speech.refusesFocus = true

        voice.say(turn("Sentier"))
        voice.say(turn("Crête"))
        speech.finishStarting()
        voice.say(turn("Refuge"))
        speech.refusesFocus = false
        voice.say(turn("Col"))

        assertEquals(4, speech.said.size, "every sentence is said, the focus refused or not")
        assertEquals(
            List(3) { SpeechException.Kind.AudioRefused },
            failures.map { assertIs<SpeechException>(it).kind },
        )
        voice.close()
    }

    /** With no onError, a failure reaches the platform's log in a debuggable app, and is never thrown at the caller. */
    @Test
    fun aVoiceGivenNoOnErrorDropsEachFailureToThePlatformsLogAndThrowsNothing() {
        val failing = ScriptedSpeech()
        voice(failing).say(turn("Sentier"))
        failing.finishStarting(ready = false)
        assertEquals(SpeechException.Kind.EngineUnavailable, assertIs<SpeechException>(failing.dropped.single()).kind)

        val refusing = ScriptedSpeech()
        val voice = voice(refusing)
        refusing.finishStarting()
        refusing.refusesFocus = true
        voice.say(turn("Crête"))
        assertEquals(SpeechException.Kind.AudioRefused, assertIs<SpeechException>(refusing.dropped.single()).kind)
        assertEquals(listOf("Tournez à gauche sur Crête"), refusing.said)
        voice.close()
    }

    @Test
    fun anEngineThatStartsAndAVoiceClosedBeforeItsEngineFailsTellNothing() {
        val started = ScriptedSpeech()
        val failures = mutableListOf<Throwable>()
        val voice = voice(started) { failures += it }
        started.finishStarting()
        assertEquals(true, voice.isStarted)
        voice.close()

        val closing = ScriptedSpeech()
        voice(closing) { failures += it }.close()
        closing.finishStarting(ready = false)

        assertEquals(emptyList(), failures)
    }

    /**
     * `prepare` is how an app made its voice up front learns, before the first maneuver, whether it will speak: it
     * waits for the platform's report, and a failed start reaches onError before it returns, as `sdk-swift`'s and
     * `sdk-dart`'s `prepare` complete.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun prepareWaitsForTheEnginesReportAndAFailedStartIsToldBeforeItReturns() =
        runTest {
            val speech = ScriptedSpeech()
            val failures = mutableListOf<Throwable>()
            val voice = voice(speech) { failures += it }

            val preparing = async { voice.prepare() }
            runCurrent()
            assertFalse(preparing.isCompleted, "the engine has not reported yet")
            voice.say(turn("Sentier"))
            speech.finishStarting(ready = false)
            runCurrent()

            assertTrue(preparing.isCompleted)
            preparing.await()
            assertEquals(SpeechException.Kind.EngineUnavailable, assertIs<SpeechException>(failures.single()).kind)
            assertEquals(emptyList(), speech.said)
            voice.prepare()
            assertEquals(1, failures.size, "a second prepare tells nothing again")
            voice.close()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun prepareReturnsOnceTheEngineStartedAndAtOnceAfterAClose() =
        runTest {
            val started = ScriptedSpeech()
            val voice = voice(started)
            val preparing = async { voice.prepare() }
            started.finishStarting()
            preparing.await()
            voice.say(turn("Crête"))
            assertEquals(listOf("Tournez à gauche sur Crête"), started.said)
            voice.close()

            val closing = ScriptedSpeech()
            val closed = voice(closing)
            val waiting = async { closed.prepare() }
            runCurrent()
            closed.close()
            waiting.await()
            closed.prepare()
            assertEquals(1, closing.shutdowns)
        }

    @Test
    fun aCloseReleasesTheEngineOnceAndDropsWhatWaitsEvenWhereTheEngineStartsAfter() {
        val speech = ScriptedSpeech()
        val voice = voice(speech)

        voice.say(turn("Sentier"))
        voice.close()
        speech.finishStarting()
        voice.say(turn("Crête"))
        voice.hush()
        voice.close()

        assertEquals(emptyList(), speech.said)
        assertEquals(1, speech.shutdowns)
        assertEquals(0, speech.stops, "a closed voice hands its released engine nothing more")
    }
}
