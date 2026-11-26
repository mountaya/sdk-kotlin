package com.mountaya.build.trail

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrailCheckTest {
    private val steps = listOf("trail.client", "trail.no-key", "trail.session")

    /** The contract's demo itinerary, as the tests hand it. */
    private val itinerary = "{\"id\": \"demo\"}\n".encodeToByteArray()

    /** A value shaped as a key, built here so that no source holds one: a prefix and 32 lowercase hex characters. */
    private val key = "pk_" + "0123456789abcdef".repeat(2)

    /** A value shaped as a session token: `sess_`, a base64url payload, a `.` and a signature. */
    private val token = "sess_" + "eyJzdXJmYWNlIjoiYXBpIn0" + "." + "c2lnbmVk"

    /** An `examples/` directory whose Trail holds to [steps] and [itinerary]. */
    private fun examples(): File {
        val root = Files.createTempDirectory("examples").toFile()
        write(root, "LICENSE", "MIT License\n")
        write(root, "trail/src/main/kotlin/com/example/trail/Trail.kt", "// trail: trail.client\nfun trail() = Unit\n")
        write(root, "trail/src/main/AndroidManifest.xml", "<!-- trail: trail.no-key -->\n<manifest />\n")
        write(root, "trail/session/src/main/kotlin/Main.kt", "    // trail: trail.session\nfun main() = Unit\n")
        File(root, "trail/src/main/assets").mkdirs()
        File(root, "trail/src/main/assets/itinerary.json").writeBytes(itinerary)
        write(root, "trail/.env.example", "MOUNTAYA_PUBLISHABLE_KEY=\n")
        return root
    }

    private fun write(
        root: File,
        path: String,
        text: String,
    ) {
        File(root, path).apply {
            parentFile.mkdirs()
            writeText(text)
        }
    }

    @Test
    fun aTrailThatHoldsToTheScenarioPassesSayingWhatItRead() {
        val verdict = TrailCheck.check(examples(), steps, itinerary)

        assertEquals(emptyList(), verdict.problems)
        assertEquals(
            "checkTrail: 3 steps marked by 3 markers, 6 files read for a key, the itinerary compared byte for byte",
            verdict.summary,
        )
    }

    @Test
    fun aStepNoMarkerNamesFails() {
        val root = examples()
        write(root, "trail/session/src/main/kotlin/Main.kt", "fun main() = Unit\n")

        assertEquals(
            listOf(
                "`trail.session` is a step of sdk/trail.yaml that no `trail: trail.session` marker under " +
                    "examples/trail names: mark the code that does it.",
            ),
            TrailCheck.check(root, steps, itinerary).problems,
        )
    }

    @Test
    fun aMarkerNamingNoStepFailsWhereItStands() {
        val root = examples()
        write(root, "trail/src/main/kotlin/com/example/trail/Map.kt", "package a\n\n// trail: trail.maps\n")

        assertEquals(
            listOf(
                "examples/trail/src/main/kotlin/com/example/trail/Map.kt:3 marks `trail.maps`, which names no step " +
                    "of sdk/trail.yaml.",
            ),
            TrailCheck.check(root, steps, itinerary).problems,
        )
    }

    @Test
    fun aValueShapedAsAKeyOrASessionTokenFailsWhereItStandsWithoutRepeatingIt() {
        val root = examples()
        write(root, "trail/src/main/res/values/keys.xml", "<string>${key.replace("pk_", "sk_")}</string>\n")
        write(root, "trail/README.md", "One line.\nexport MOUNTAYA_PUBLISHABLE_KEY=$key\n$token\n")

        val problems = TrailCheck.check(root, steps, itinerary).problems

        assertEquals(
            listOf(
                "examples/trail/README.md:2 holds a value shaped as a key, `pk_` or `sk_` and 32 lowercase hex " +
                    "characters: Trail reads its key from .env or the environment, and never from a committed file.",
                "examples/trail/README.md:3 holds a value shaped as a session token, `sess_` and 32 lowercase hex " +
                    "characters or a payload, a `.` and a signature: a token is minted at runtime, and never committed.",
                "examples/trail/src/main/res/values/keys.xml:1 holds a value shaped as a key, `pk_` or `sk_` and 32 " +
                    "lowercase hex characters: Trail reads its key from .env or the environment, and never from a " +
                    "committed file.",
            ),
            problems,
        )
        assertFalse(problems.any { key.removePrefix("pk_") in it }, "a problem never repeats the value it found")
    }

    /**
     * The contract documents every credential's shape as its prefix and 32 lowercase hex characters (`sdk/SPEC.md`
     * section 1), a session token's among them, beside the token's own form, a payload, a `.` and a signature.
     */
    @Test
    fun aSessionTokenInTheShapeOfAKeyFailsWhereItStandsWithoutRepeatingIt() {
        val root = examples()
        val token = "sess_" + "0123456789abcdef".repeat(2)
        // A token in both shapes at once, its hex followed by a signature, is told once.
        val both = "$token.c2lnbmVk"
        write(
            root,
            "trail/src/main/kotlin/com/example/trail/Token.kt",
            "package a\n\nval a = \"$token\"\nval b = \"$both\"\n",
        )

        val problems = TrailCheck.check(root, steps, itinerary).problems

        assertEquals(
            listOf(3, 4).map { line ->
                "examples/trail/src/main/kotlin/com/example/trail/Token.kt:$line holds a value shaped as a session " +
                    "token, `sess_` and 32 lowercase hex characters or a payload, a `.` and a signature: a token is " +
                    "minted at runtime, and never committed."
            },
            problems,
        )
        assertFalse(problems.any { token.removePrefix("sess_") in it }, "a problem never repeats the value it found")
    }

    /** What is never committed is never read: the key file a developer keeps the key in, and what a build writes. */
    @Test
    fun theKeyFileAndWhatABuildWritesAreNotReadAndAPlaceholderIsNoKey() {
        val root = examples()
        write(root, "trail/.env", "MOUNTAYA_PUBLISHABLE_KEY=$key\n")
        write(root, "trail/build/generated/res/values.xml", "<string>$key</string>\n")
        write(root, "trail/session/build/tmp/token.txt", "$token\n")
        write(root, "trail/.gradle/state.txt", "$key\n")
        write(root, "trail/src/main/kotlin/com/example/trail/Measured.kt", "val placeholder = \"pk_size_gate\"\n")

        val verdict = TrailCheck.check(root, steps, itinerary)

        assertEquals(emptyList(), verdict.problems)
        assertTrue(", 7 files read for a key," in verdict.summary, verdict.summary)
    }

    @Test
    fun anItineraryThatDiffersByAByteOrIsMissingFails() {
        val root = examples()
        val copy = File(root, TrailCheck.ITINERARY)
        copy.writeBytes(itinerary.copyOf().also { it[it.size - 1] = ' '.code.toByte() })

        assertEquals(
            listOf(
                "examples/trail/src/main/assets/itinerary.json differs from the contract's sdk/trail/itinerary.json: " +
                    "copy it again, byte for byte.",
            ),
            TrailCheck.check(root, steps, itinerary).problems,
        )

        copy.delete()
        assertEquals(
            listOf(
                "examples/trail/src/main/assets/itinerary.json is missing: Trail ships a byte-for-byte copy of " +
                    "sdk/trail/itinerary.json.",
            ),
            TrailCheck.check(root, steps, itinerary).problems,
        )
    }

    @Test
    fun aMissingLicenceCopyFails() {
        val root = examples()
        File(root, TrailCheck.LICENCE).delete()

        assertEquals(
            listOf(
                "examples/LICENSE is missing: the examples are MIT licensed, as sdk-typescript's are, and carry the " +
                    "licence beside them.",
            ),
            TrailCheck.check(root, steps, itinerary).problems,
        )
    }

    /**
     * Where the contract checkout is absent, the markers and the copy have nothing to be held to, and the rest is held.
     */
    @Test
    fun withNoScenarioReadTheMarkersAndTheCopyAreLeftAndTheKeysAndTheLicenceHeld() {
        val root = examples()
        write(root, "trail/src/main/kotlin/com/example/trail/Map.kt", "// trail: trail.maps\n")
        File(root, TrailCheck.ITINERARY).delete()
        File(root, TrailCheck.LICENCE).delete()
        write(root, "trail/notes.txt", "$key\n")

        val verdict = TrailCheck.check(root, null, null)

        assertEquals(2, verdict.problems.size, verdict.problems.joinToString("\n"))
        assertTrue(verdict.problems.first().startsWith("examples/trail/notes.txt:1 holds a value shaped as a key"))
        assertTrue(verdict.problems.last().startsWith("examples/LICENSE is missing"))
        assertEquals(
            "checkTrail: no step list read, 6 files read for a key, no itinerary to compare",
            verdict.summary,
        )
    }
}
