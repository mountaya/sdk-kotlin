package com.mountaya.build.trail

import java.io.File

/**
 * TrailCheck is the scenario gate of SPEC.md section 7: Trail, `examples/trail`, held to the contract's
 * `sdk/trail.yaml`, so the three native SDKs' copies stay one app.
 *
 * An app marks the code that does a step with a comment naming it, `// trail: trail.map` in Kotlin and `<!-- trail:
 * trail.map -->` in a manifest, the session server's code under `examples/trail` included. The gate fails on a step of the
 * scenario no marker names and on a marker naming no step; on a value shaped as a key or a session token anywhere under
 * `examples/`, in either shape the contract's `sdk/SPEC.md` gives one: a credential's prefix, `pk_`, `sk_` or `sess_`, and
 * 32 lowercase hex characters, the shape its section 1 documents for every credential, and `sess_`, an unpadded base64url
 * payload, a `.` and a signature, the form a session token's own reader takes in its studio section; on a copy of the demo
 * itinerary that differs from the contract's by a byte; and on a missing copy of the examples' licence. What is never
 * committed is not read for a key: the key file, `.env`, whose `.example` is, and what a build writes, `build/`, `.gradle/`
 * and `.kotlin/`.
 */
object TrailCheck {
    /** The gate's name, which every message opens with. */
    const val NAME = "checkTrail"

    /** Trail's directory below `examples/`. */
    const val TRAIL = "trail"

    /** Trail's copy of the demo itinerary, below `examples/`. */
    const val ITINERARY = "trail/src/main/assets/itinerary.json"

    /** The examples' licence, below `examples/`. */
    const val LICENCE = "LICENSE"

    /** The file a developer keeps the settings in, which is never committed and never read for a key. */
    const val KEY_FILE = ".env"

    /** The directories a build writes and an editor keeps, which hold nothing committed and are never read. */
    val UNREAD: Set<String> = setOf("build", ".gradle", ".kotlin", ".idea")

    /** The files a marker is looked for in: Kotlin, Gradle's Kotlin scripts and Android's XML. */
    private val MARKED = setOf("kt", "kts", "xml")

    private val MARKER = Regex("""(?://|<!--)\s*trail:\s*([^\s>]+)""")

    /** A credential in the shape `sdk/SPEC.md` section 1 documents for each: its prefix and 32 lowercase hex characters. */
    private val HEX_SHAPED = Regex("""(?<![A-Za-z0-9])(pk|sk|sess)_[0-9a-f]{32}""")

    /** A session token in its own form, as `sdk/SPEC.md` reads one: `sess_`, a payload, a `.` and a signature. */
    private val SESSION_TOKEN = Regex("""(?<![A-Za-z0-9])sess_[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+""")

    /** Verdict is what a run found: the problems, and the one line it prints either way. */
    data class Verdict(
        /** Every problem it found, one sentence each. */
        val problems: List<String>,
        /** The line it prints either way. */
        val summary: String,
    )

    /**
     * Holds [examples], the repository's `examples/` directory, to [steps], the step ids `sdk/trail.yaml` lists, and to
     * [itinerary], the contract's demo itinerary: each null where the contract checkout is absent, which leaves the
     * markers and the copy unchecked and the rest checked.
     */
    fun check(
        examples: File,
        steps: List<String>?,
        itinerary: ByteArray?,
    ): Verdict {
        val problems = mutableListOf<String>()
        val files = filesUnder(examples)
        val markers = mutableListOf<Pair<String, String>>()
        val trail = File(examples, TRAIL)
        for (file in files) {
            val shown = "examples/${file.relativeTo(examples).invariantSeparatorsPath}"
            val lines = file.readText().lines()
            for ((index, line) in lines.withIndex()) {
                // A line is told once for each kind it holds, a token in both shapes once.
                val prefixes = HEX_SHAPED.findAll(line).map { it.groupValues[1] }.toSet()
                if ("pk" in prefixes || "sk" in prefixes) {
                    problems += "$shown:${index + 1} holds a value shaped as a key, `pk_` or `sk_` and 32 lowercase " +
                        "hex characters: Trail reads its key from .env or the environment, and never from a committed file."
                }
                if ("sess" in prefixes || SESSION_TOKEN.containsMatchIn(line)) {
                    problems +=
                        "$shown:${index + 1} holds a value shaped as a session token, `sess_` and 32 lowercase hex " +
                        "characters or a payload, a `.` and a signature: a token is minted at runtime, and never committed."
                }
                if (file.startsWith(trail) && file.extension in MARKED) {
                    for (marker in MARKER.findAll(line)) markers += marker.groupValues[1] to "$shown:${index + 1}"
                }
            }
        }
        if (steps != null) {
            for (step in steps) {
                if (markers.none { it.first == step }) {
                    problems += "`$step` is a step of sdk/trail.yaml that no `trail: $step` marker under " +
                        "examples/$TRAIL names: mark the code that does it."
                }
            }
            for ((marked, place) in markers) {
                if (marked !in steps) problems += "$place marks `$marked`, which names no step of sdk/trail.yaml."
            }
        }
        val copy = File(examples, ITINERARY)
        if (itinerary != null) {
            if (!copy.isFile) {
                problems +=
                    "examples/$ITINERARY is missing: Trail ships a byte-for-byte copy of sdk/trail/itinerary.json."
            } else if (!copy.readBytes().contentEquals(itinerary)) {
                problems +=
                    "examples/$ITINERARY differs from the contract's sdk/trail/itinerary.json: copy it again, " +
                    "byte for byte."
            }
        }
        if (!File(examples, LICENCE).isFile) {
            problems +=
                "examples/$LICENCE is missing: the examples are MIT licensed, as sdk-typescript's are, and carry " +
                "the licence beside them."
        }
        val held = if (steps == null) "no step list read" else "${steps.size} steps marked by ${markers.size} markers"
        val copied = if (itinerary == null) "no itinerary to compare" else "the itinerary compared byte for byte"
        return Verdict(problems, "$NAME: $held, ${files.size} files read for a key, $copied")
    }

    /**
     * Returns every file under [examples] a commit would hold, in a stable order: what a build writes and the key file
     * left out.
     */
    fun filesUnder(examples: File): List<File> =
        examples
            .walkTopDown()
            .onEnter { it == examples || it.name !in UNREAD }
            .filter { it.isFile && it.name != KEY_FILE }
            .sortedBy { it.invariantSeparatorsPath }
            .toList()
}
