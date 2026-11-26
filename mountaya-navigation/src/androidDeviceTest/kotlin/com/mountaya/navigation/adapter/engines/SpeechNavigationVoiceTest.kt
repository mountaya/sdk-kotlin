package com.mountaya.navigation

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.NavigationInstructionKey
import com.mountaya.geo.NavigationStep
import com.mountaya.geo.stepInstruction
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The voice over the platform's text-to-speech, on the device it speaks on: its engine starts, it says
 * the host's words for each instruction, says nothing the host has no words for, and takes the audio focus
 * while it speaks, letting the app that held it lower what it plays, then gives it back.
 */
class SpeechNavigationVoiceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * The query itself, read off the manifest the build merged into this test's own APK, as it merges the
     * library's into an app's: an app targeting API 30 or later, as this APK does, sees a speech engine only
     * through it, where the device does not let every app see its engine, as this emulator's system engine is.
     */
    @Test
    fun theLibrarysManifestDeclaresTheSpeechServiceQueryAnAppMergesIn() {
        assertTrue(
            "android.intent.action.TTS_SERVICE" in queriedActions(),
            "the merged manifest queries the speech service",
        )
    }

    /**
     * From Android 11 an app targeting API 30 or later, as this test's own APK does, sees the platform's
     * speech engine only through the TTS_SERVICE query the library's manifest declares, merged into the
     * app's: without it the engine never starts, every sentence is dropped, and the failure is told. On a
     * device whose speech engine every app may see, a system package marked so, it starts either way, which
     * is why the query is read off the manifest too.
     */
    @Test
    fun theVoiceStartsThePlatformsSpeechEngineOnAndroid11AndLater() {
        assertTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R, "the emulator runs API ${Build.VERSION.SDK_INT}")
        val targetSdk = context.applicationInfo.targetSdkVersion
        assertTrue(targetSdk >= Build.VERSION_CODES.R, "the test APK targets API $targetSdk")
        val failures = CopyOnWriteArrayList<Throwable>()
        val voice = SpeechNavigationVoice(context, "en-US", onError = { failures += it }) { "Continue" }
        try {
            val deadline = SystemClock.uptimeMillis() + START_TIMEOUT_MILLISECONDS
            while (voice.isStarted == null && SystemClock.uptimeMillis() < deadline) Thread.sleep(POLL_MILLISECONDS)

            assertEquals(
                true,
                voice.isStarted,
                "the platform's speech engine started: the device holds one, and the app sees it through the manifest's query",
            )
            assertEquals(emptyList(), failures.toList())
        } finally {
            voice.close()
        }
    }

    @Test
    fun eachInstructionIsWordedByTheHostAndOneItHasNoWordsForIsNotSaid() {
        val asked = mutableListOf<NavigationInstructionKey>()
        val voice =
            SpeechNavigationVoice(context, "fr-FR") { instruction ->
                asked += instruction.key
                if (instruction.key ==
                    NavigationInstructionKey.TurnLeft
                ) {
                    "Tournez à gauche sur ${instruction.name}"
                } else {
                    null
                }
            }

        voice.say(stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft, name = "Sentier")))
        voice.say(stepInstruction(NavigationStep(1, 1, RestSchema.ManeuverType.Goal)))
        voice.hush()
        voice.hush()
        voice.close()

        assertEquals(listOf(NavigationInstructionKey.TurnLeft, NavigationInstructionKey.GoalPlain), asked)
    }

    @Test
    fun aLanguageTheDeviceHoldsNoVoiceForIsSpokenInTheEnginesOwn() {
        val voice = SpeechNavigationVoice(context, "not a tag") { "Continuez" }

        voice.say(stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.Continue)))
        voice.hush()
        voice.close()
    }

    @Test
    fun aLanguageTheDeviceHoldsNoVoiceForIsSpokenInTheFallbackLanguage() {
        val voice = SpeechNavigationVoice(context, "not a tag", fallbackLanguage = "en-US") { "Continue" }

        voice.say(stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.Continue)))
        voice.hush()
        voice.close()
    }

    /**
     * Another player holds the focus, as a music app does: the voice takes it for the moment, letting that
     * player lower what it plays, said as it is said before the engine has started, and gives it back once
     * the sentence has ended.
     */
    @Test
    fun aSentenceTakesTheAudioFocusForTheMomentAndGivesItBackOnceSaid() {
        val audio = assertNotNull(context.getSystemService(AudioManager::class.java), "the device has an audio manager")
        val changes = LinkedBlockingQueue<Int>()
        val music =
            AudioFocusRequest
                .Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                // Told of a loss it may duck under, rather than ducked by the platform without a word.
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener({ change -> changes.add(change) }, Handler(Looper.getMainLooper()))
                .build()
        assertEquals(AudioManager.AUDIOFOCUS_REQUEST_GRANTED, audio.requestAudioFocus(music))
        val voice = SpeechNavigationVoice(context, "en-US") { "Turn left onto ${it.name}" }
        try {
            voice.say(
                stepInstruction(NavigationStep(0, 1, RestSchema.ManeuverType.TurnLeft, name = "Path")),
            )

            assertEquals(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK, changes.poll(30, TimeUnit.SECONDS))
            assertEquals(AudioManager.AUDIOFOCUS_GAIN, changes.poll(30, TimeUnit.SECONDS))
        } finally {
            voice.close()
            audio.abandonAudioFocusRequest(music)
        }
    }

    /**
     * The action of every `<intent>` the `<queries>` of this APK's own manifest names, read off the compiled
     * manifest of the APK the context's package is: each asset path is tried in turn, since the platform's own
     * resources hold a manifest of their own, until one names the package.
     */
    private fun queriedActions(): Set<String> {
        for (cookie in 1..MAX_ASSET_COOKIE) {
            val parser =
                try {
                    context.assets.openXmlResourceParser(cookie, "AndroidManifest.xml")
                } catch (missing: IOException) {
                    continue
                }
            try {
                var named: String? = null
                var inQueries = false
                var inIntent = false
                val actions = mutableSetOf<String>()
                while (true) {
                    when (parser.next()) {
                        XmlPullParser.END_DOCUMENT -> {
                            break
                        }

                        XmlPullParser.START_TAG -> {
                            val action = parser.getAttributeValue(ANDROID_NAMESPACE, "name")
                            when (parser.name) {
                                "manifest" -> named = parser.getAttributeValue(null, "package")
                                "queries" -> inQueries = true
                                "intent" -> inIntent = inQueries
                                "action" -> if (inIntent && action != null) actions += action
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            when (parser.name) {
                                "queries" -> inQueries = false
                                "intent" -> inIntent = false
                            }
                        }
                    }
                }
                if (named == context.packageName) return actions
            } finally {
                parser.close()
            }
        }
        error("No asset path holds the manifest of ${context.packageName}.")
    }

    private companion object {
        /** How long the platform's speech engine is given to start, its service bound on the device's main thread. */
        const val START_TIMEOUT_MILLISECONDS = 30_000L

        /** How often the test reads whether it has. */
        const val POLL_MILLISECONDS = 50L

        /** How many asset paths are tried for the APK's own manifest, the platform's resources and overlays among them. */
        const val MAX_ASSET_COOKIE = 32

        /** The namespace a manifest's `android:` attributes are compiled under. */
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
