package com.mountaya.build.readme

/**
 * Floors are the floors a consumer's build meets, which the version catalog holds and nothing else (SPEC section 2),
 * and the forms a document states each in, so the documents that state one are held to the catalog by one reading: the
 * developer skill wherever its prose states one ([SkillSnippets.floorProblems]), and the README on its floors table's
 * rows alone, its one prose copy of each ([ReadmeFloors]).
 */
object Floors {
    /**
     * Floor is a floor the build declares, keyed in the version catalog by [key], and the [forms] a document states it
     * in, each with the figure it states captured and its words apart by any space, a line's end included, so a
     * sentence reflowed across lines still states it; [example] is how a problem says it, the figure in place of its
     * `%s`.
     */
    data class Floor(
        /** The floor's key in the version catalog. */
        val key: String,
        /** What the floor is, as a problem names it. */
        val what: String,
        /** The forms a document states it in. */
        val forms: List<Regex>,
        /** How a problem says it. */
        val example: String,
    )

    /**
     * Statement is one statement of [floor]: the [figure] it states, its [words] with each run of spaces one, and its
     * [offset].
     */
    data class Statement(
        /** The floor stated. */
        val floor: Floor,
        /** The figure it states. */
        val figure: String,
        /** Its words, each run of spaces one. */
        val words: String,
        /** Where it starts in the document. */
        val offset: Int,
    )

    /** Every floor, in the order a document's problems name them. */
    val all: List<Floor> =
        listOf(
            Floor(
                "kotlin-floor",
                "the Kotlin a consumer compiles with",
                listOf(Regex("""\bKotlin\s+(\d+(?:\.\d+)+)\s+or\s+newer\b""")),
                "Kotlin %s or newer",
            ),
            Floor(
                "jvm-target",
                "the Java a consumer's jvm and Android code targets",
                listOf(Regex("""\bJava\s+(\d+)\s+or\s+newer\b""")),
                "Java %s or newer",
            ),
            Floor(
                "android-minSdk",
                "the API level the Android modules run from",
                listOf(Regex("""\bfrom\s+API\s+level\s+(\d+)\b"""), Regex("""\bminSdk\s*=?\s*(\d+)\b""")),
                "from API level %s",
            ),
            Floor(
                "android-compileSdk",
                "the API level an app compiles against",
                listOf(Regex("""\bagainst\s+API\s+level\s+(\d+)\b"""), Regex("""\bcompileSdk\s*=?\s*(\d+)\b""")),
                "against API level %s",
            ),
        )

    /** The catalog keys of every floor, each of which the build hands the gates that hold a document to it. */
    val keys: List<String> = all.map { it.key }

    /** A run of spaces in a statement, which a problem quotes as one. */
    private val SPACES = Regex("""\s+""")

    /** Returns every statement of [floor] in [text], in the order they stand. */
    fun statements(
        floor: Floor,
        text: String,
    ): List<Statement> =
        floor.forms
            .flatMap { it.findAll(text) }
            .sortedBy { it.range.first }
            .map { Statement(floor, it.groupValues[1], it.value.replace(SPACES, " "), it.range.first) }

    /** Returns the line of [text] that [offset] stands on, counted from 1. */
    fun lineOf(
        text: String,
        offset: Int,
    ): Int = text.take(offset).count { it == '\n' } + 1
}
