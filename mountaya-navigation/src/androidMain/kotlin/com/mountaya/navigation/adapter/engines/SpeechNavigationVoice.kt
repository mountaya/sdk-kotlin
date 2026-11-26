package com.mountaya.navigation

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.mountaya.geo.NavigationInstruction
import kotlinx.coroutines.CompletableDeferred
import java.util.Locale
import java.util.UUID

/**
 * SpeechNavigationVoice says each maneuver through the platform's text-to-speech, as navigation
 * guidance, in the host's words and in the language it names, taking the audio focus while it speaks,
 * so other apps lower or pause what they play.
 *
 * Its `words` turn an instruction, a copy key and its arguments, into the sentence said, from the
 * host's own strings: an instruction they answer null for, or an empty sentence, is not said. Its
 * `language` is a BCP 47 tag naming the voice, `fr-FR`; where the device holds no voice for it, the
 * sentences are said in the voice of its `fallbackLanguage`, where one is given, and where the device
 * holds none for that either, in the engine's own default voice. Always the host's, never a platform's
 * default locale.
 *
 * The platform's speech engine starts in a service of its own as the voice is made, and the voice is named
 * once the platform reports it has started. A sentence handed to it before then waits for it, in order, and
 * is said once it has, so the first maneuver of a route, said as the route lands, is said however soon the
 * route lands; a hush drops what waits with what is being said. [prepare] waits for that report, so an app
 * that makes its voice up front learns before the first maneuver whether it will speak. Where the engine
 * fails to start, nothing is
 * said, now or later, and the failure is told to the `onError` the voice was made with, once, as a
 * [SpeechException] of kind `SpeechException.Kind.EngineUnavailable`, on the thread the platform reports
 * the start on, the main thread for a voice made there. Where the platform refuses the audio focus a
 * sentence asks for, the sentence is said anyway, over what other apps play, and each refusal is told as a
 * [SpeechException] of kind `SpeechException.Kind.AudioRefused`, on the thread that handed the sentence on.
 * With no `onError`, each failure is dropped, never thrown, and a debuggable app logs it once with `Log.w`.
 * An app's manifest needs nothing for the engine to start: `mountaya-navigation`'s own declares
 * the query Android 11 and later ask of an app before it may bind the platform's speech service, which
 * the build merges into the app's. It holds that service until [close], which the app calls where it is
 * done navigating: an engine closes nothing it was given.
 */
public class SpeechNavigationVoice internal constructor(
    private val speech: Speech,
    /** Told the engine failed to start and each refusal of the audio focus, or null to drop each. */
    private val onError: ((Throwable) -> Unit)? = null,
    private val words: (instruction: NavigationInstruction) -> String?,
) : NavigationVoice,
    AutoCloseable {
    /**
     * Creates a voice over the platform's text-to-speech, starting its speech engine at once.
     *
     * @param context Any context of the app's; the voice holds the application's own.
     * @param language The BCP 47 tag of the language the sentences are in.
     * @param fallbackLanguage The BCP 47 tag of the voice a sentence is said in where the device holds none
     * for [language], or null to fall back on the engine's own default voice at once.
     * @param onError Told a [SpeechException]: once where the platform's speech engine fails to start, and each
     * time the platform refuses a sentence the audio focus; or null to drop each.
     * @param words The host's sentence for each instruction, or null for one it does not say.
     */
    public constructor(
        context: Context,
        language: String,
        fallbackLanguage: String? = null,
        onError: ((Throwable) -> Unit)? = null,
        words: (instruction: NavigationInstruction) -> String?,
    ) : this(PlatformSpeech(context.applicationContext, language, fallbackLanguage), onError, words)

    /**
     * Speech is the platform's text-to-speech as a voice drives it: started once, handed each sentence
     * after the one before it, stopped, and released. It tells the function [start] is given, once,
     * whether its engine started, on whatever thread the platform reports it.
     */
    internal interface Speech {
        /** Starts the engine, and tells [started] once whether it did. */
        fun start(started: (Boolean) -> Unit)

        /**
         * Says [text] after anything said before it, under the utterance [id], and answers false where the
         * platform refused the audio focus it asked for, which it says anyway.
         */
        fun speak(
            text: String,
            id: UUID,
        ): Boolean

        /** Stops what is being said, and drops what waits to be. */
        fun stop()

        /** Releases the engine. */
        fun shutdown()

        /** Logs [failure], which no `onError` was given for, where the app is debuggable. */
        fun dropped(failure: Throwable)
    }

    /** Guards what waits and whether the engine started, which the platform reports on a thread of its own. */
    private val lock = Any()

    /** Whether the speech engine started: null while it starts. */
    private var ready: Boolean? = null

    private var closed = false

    /** Completed once the platform reports whether the engine started, a failure told first, or as the voice closes. */
    private val settled = CompletableDeferred<Unit>()

    /** The sentences said before the engine started, in the order they were said. */
    private val waiting = ArrayDeque<String>()

    /** Counts the sentences handed to the engine, each under an utterance of its own count. */
    private var spoken = 0L

    init {
        speech.start(::started)
    }

    /** Whether the platform's speech engine started, or null while it starts: what a device test waits on. */
    internal val isStarted: Boolean?
        get() = synchronized(lock) { ready }

    /**
     * Waits until the platform reports whether the speech engine started, which it was asked to as the voice was
     * made, so an app learns ahead of the first maneuver whether the voice will speak: where the engine failed to
     * start, the voice's `onError` has been told a [SpeechException] of kind `SpeechException.Kind.EngineUnavailable`
     * by the time this returns. It starts nothing of its own, a sentence said while it waits waits as it would
     * have, and it returns at once where the engine has already reported or the voice is closed. Cancelling the
     * coroutine that waits leaves the engine starting.
     */
    public suspend fun prepare() {
        settled.await()
    }

    /** Says the host's sentence for [instruction], after anything said before it. */
    override fun say(instruction: NavigationInstruction) {
        val text = words(instruction)?.takeIf { it.isNotEmpty() } ?: return
        val refused =
            synchronized(lock) {
                if (closed) return
                when (ready) {
                    null -> {
                        waiting.addLast(text)
                        false
                    }

                    true -> {
                        !speak(text)
                    }

                    // An engine that failed to start says nothing, now or later.
                    false -> {
                        false
                    }
                }
            }
        // Told outside the lock, so a handler that says or hushes takes it afresh.
        if (refused) tell(SpeechException(SpeechException.Kind.AudioRefused))
    }

    /** Stops what is being said, and drops what waits to be, the sentences waiting for the engine included. */
    override fun hush() {
        synchronized(lock) {
            waiting.clear()
            if (!closed) speech.stop()
        }
    }

    /** Releases the platform's speech engine. Nothing is said after, a sentence that waited included. */
    override fun close() {
        synchronized(lock) {
            if (closed) return
            closed = true
            waiting.clear()
            speech.shutdown()
        }
        settled.complete(Unit)
    }

    override fun toString(): String = "SpeechNavigationVoice"

    /**
     * Says what waited, in order, once the engine has started, telling each refusal of the audio focus, or drops it
     * where the engine failed to and tells `onError` so; a voice closed first tells nothing.
     */
    private fun started(isReady: Boolean) {
        var refusals = 0
        synchronized(lock) {
            if (closed || ready != null) return
            ready = isReady
            if (isReady) {
                while (waiting.isNotEmpty()) if (!speak(waiting.removeFirst())) refusals += 1
            } else {
                waiting.clear()
            }
        }
        // Told outside the lock, so a handler that says or hushes takes it afresh, and before `prepare` returns.
        if (!isReady) tell(SpeechException(SpeechException.Kind.EngineUnavailable))
        settled.complete(Unit)
        repeat(refusals) { tell(SpeechException(SpeechException.Kind.AudioRefused)) }
    }

    /** Hands [text] to the engine under the next utterance, and answers false where its audio focus was refused. */
    private fun speak(text: String): Boolean {
        spoken += 1
        return speech.speak(text, UUID(1L, spoken))
    }

    /** Tells [failure] to `onError`, or drops it where there is none, logged where the app is debuggable. */
    private fun tell(failure: SpeechException) {
        val told = onError
        if (told != null) told(failure) else speech.dropped(failure)
    }

    /**
     * PlatformSpeech is the platform's text-to-speech, speaking as navigation guidance. It takes the audio
     * focus as it is handed a sentence, for a moment and letting other apps lower what they play, or pause
     * it, and lets it go half a second after the last sentence ends, so a sentence that follows another
     * keeps it, and at once where it is stopped; another app that takes the focus from it, for good or for
     * a while, stops what it says. It names the voice once the engine has started, before anything that
     * waited is said, as `voiceLanguageOf` chooses it: [language]'s, else [fallbackLanguage]'s, and the
     * engine's own where the device holds neither.
     *
     * It is driven from the thread its voice is, and the platform tells it the engine's start, each
     * sentence's end and a loss of the focus on threads of the platform's choosing.
     */
    private class PlatformSpeech(
        private val context: Context,
        private val language: String,
        private val fallbackLanguage: String?,
    ) : Speech {
        private val audio: AudioManager? = context.getSystemService(AudioManager::class.java)
        private val main = Handler(Looper.getMainLooper())

        /** What the speech plays as, and what the focus is asked for: navigation guidance, in words. */
        private val guidance: AudioAttributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

        /** Guards the engine, the sentences under way and the focus, which the platform reports on threads of its choosing. */
        private val lock = Any()

        /** The platform's engine, from [start] to [shutdown]. */
        private var engine: TextToSpeech? = null

        /** The utterance of each sentence handed to the engine that has not ended yet. */
        private val underway = HashSet<String>()

        /** Whether the focus is held, and the request it was granted to from API 26. */
        private var focused = false
        private var request: AudioFocusRequest? = null

        /** Another app takes the focus, for good or for a while: what is said stops, as a hush stops it. */
        private val changes =
            AudioManager.OnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop()
            }

        /** Lets the focus go where nothing is said, once the last sentence has ended. */
        private val letGo =
            Runnable {
                synchronized(lock) {
                    if (underway.isEmpty()) abandon()
                }
            }

        /** Hears each sentence end: said, failed or stopped. */
        private val progress =
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {}

                override fun onDone(utteranceId: String) {
                    ended(utteranceId)
                }

                @Deprecated(
                    "The platform calls it where it calls no other onError, and a sentence that failed has ended.",
                )
                override fun onError(utteranceId: String) {
                    ended(utteranceId)
                }

                override fun onError(
                    utteranceId: String,
                    errorCode: Int,
                ) {
                    ended(utteranceId)
                }

                override fun onStop(
                    utteranceId: String,
                    interrupted: Boolean,
                ) {
                    ended(utteranceId)
                }
            }

        override fun start(started: (Boolean) -> Unit) {
            // Held while the engine is made, so a start the platform reports on another thread reads it made.
            synchronized(lock) {
                engine = TextToSpeech(context) { status -> initialized(status, started) }
            }
        }

        override fun speak(
            text: String,
            id: UUID,
        ): Boolean {
            val utterance = id.toString()
            var focused = true
            val engine =
                synchronized(lock) {
                    val engine = engine ?: return true
                    main.removeCallbacks(letGo)
                    focused = take()
                    underway += utterance
                    engine
                }
            // A sentence the engine refuses has ended before it began.
            if (engine.speak(text, TextToSpeech.QUEUE_ADD, null, utterance) != TextToSpeech.SUCCESS) ended(utterance)
            return focused
        }

        override fun stop() {
            val engine = synchronized(lock) { engine }
            engine?.stop()
            quiet()
        }

        override fun shutdown() {
            val engine =
                synchronized(lock) {
                    val engine = engine
                    this.engine = null
                    engine
                }
            engine?.shutdown()
            quiet()
        }

        override fun dropped(failure: Throwable) {
            logDroppedFailure(context, "SpeechNavigationVoice", failure)
        }

        /**
         * Where the engine started, hears each sentence end and names the voice, then tells [started] whether
         * it did. A failure the platform reports as the engine is made finds no engine held, and is told as
         * one.
         */
        private fun initialized(
            status: Int,
            started: (Boolean) -> Unit,
        ) {
            val ready =
                synchronized(lock) {
                    val engine = engine
                    if (status != TextToSpeech.SUCCESS || engine == null) return@synchronized false
                    engine.setOnUtteranceProgressListener(progress)
                    engine.setAudioAttributes(guidance)
                    val spoken =
                        voiceLanguageOf(language, fallbackLanguage) { tag ->
                            engine.isLanguageAvailable(Locale.forLanguageTag(tag)) >= TextToSpeech.LANG_AVAILABLE
                        }
                    if (spoken != null) engine.language = Locale.forLanguageTag(spoken)
                    true
                }
            // Told outside the lock, since the voice takes a lock of its own and hands sentences on under it.
            // A failure reported as the engine is made is told within [start]'s hold, while the voice that
            // asked is still being made and no other thread can reach it.
            started(ready)
        }

        /** Forgets the sentence [utterance] names, and lets the focus go half a second after the last one ends. */
        private fun ended(utterance: String) {
            synchronized(lock) {
                if (!underway.remove(utterance) || underway.isNotEmpty()) return
                main.postDelayed(letGo, FOCUS_RELEASE_MILLISECONDS)
            }
        }

        /** Forgets every sentence under way and lets the focus go at once. */
        private fun quiet() {
            synchronized(lock) {
                underway.clear()
                main.removeCallbacks(letGo)
                abandon()
            }
        }

        /**
         * Takes the focus where it is not held, and answers false where the platform refused it, which leaves the
         * sentence said over other apps' audio; a focus held already, and a device with no audio service, refuse
         * nothing.
         */
        private fun take(): Boolean {
            val audio = audio ?: return true
            if (focused) return true
            focused =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val asked =
                        AudioFocusRequest
                            .Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                            .setAudioAttributes(guidance)
                            .setOnAudioFocusChangeListener(changes)
                            .build()
                    request = asked
                    audio.requestAudioFocus(asked) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                } else {
                    takeBefore26(audio)
                }
            return focused
        }

        /** Lets the focus go where it is held. */
        private fun abandon() {
            val audio = audio ?: return
            if (!focused) return
            focused = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                request?.let(audio::abandonAudioFocusRequest)
                request = null
            } else {
                abandonBefore26(audio)
            }
        }

        /** Takes the focus as the platform's floor below API 26 asks for it, the request of API 26 not there yet. */
        @Suppress("DEPRECATION")
        private fun takeBefore26(audio: AudioManager): Boolean =
            audio.requestAudioFocus(
                changes,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            ) ==
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        /** Lets the focus go as the platform's floor below API 26 asks for it. */
        @Suppress("DEPRECATION")
        private fun abandonBefore26(audio: AudioManager) {
            audio.abandonAudioFocus(changes)
        }
    }
}

/** How long after the last sentence ends the audio focus is let go, so a sentence that follows keeps it. */
private const val FOCUS_RELEASE_MILLISECONDS = 500L
