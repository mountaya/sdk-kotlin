package com.mountaya.build.readme

/**
 * DocSnippets prints the Kotlin fences of any Markdown documents it is handed, a documentation site's pages among
 * them, into the consumer build, which compiles them against the real modules as it compiles the README's and the
 * skill's (SPEC section 10). It names no document of its own: `mise run snippets` hands each one in by its path, and
 * nothing it prints is committed.
 *
 * Each fence is read as the README's are, a skip marker honoured ([ReadmeSnippets.fences]), sorted by its imports as
 * the skill's are ([SkillSnippets.targetOf]), and printed into a file of its own, with its own imports, in an object
 * of its own ([SkillSnippets.source]): the statements a consumer pastes into a function as a function's body, and a
 * fence of declarations, a screen's composable and its activity, as the object's members, which read one another in
 * any order as a file's do. Two fences never clash over a name they both import or declare, and the compiler's
 * message names the file of the fence it is about, which [Printed] maps back to the document's line. A fence its
 * imports leave in common code that reads `System`, or names a `java.` or `javax.` declaration in full, is the jvm's,
 * since common code has neither: a server's `System.getenv("MOUNTAYA_SECRET_KEY")`; and one that reads `BuildConfig`,
 * the class an Android app's build generates, is Android's, where the consumer declares a stand-in for it.
 */
object DocSnippets {
    /** The package every printed fence declares, the one the consumer's stand-ins for an app's own classes are in. */
    const val PACKAGE = "com.example.consumer.snippets"

    /** The index of the printed fences, beside their source sets: a line a fence, its fields apart by tabs. */
    const val INDEX = "fences.tsv"

    /** The tally of a run, beside the source sets, which the check prints once the fences compiled. */
    const val TALLY = "tally.txt"

    /**
     * What an unindented line of a fence of declarations opens with: an annotation, or a function, a class, an object
     * or an interface behind its modifiers, or a property behind one of its own, `private` or `const`, which a
     * statement never carries.
     */
    private val DECLARATION =
        Regex(
            """^(?:@\w|(?:(?:private|internal|public|protected|suspend|inline|data|sealed|abstract|open|enum|""" +
                """annotation|value|operator|infix|tailrec)\s+)*(?:fun|class|object|interface)\b|""" +
                """(?:(?:private|internal|public|const|lateinit)\s+)+(?:val|var)\b)""",
        )

    /** What an unindented line that opens nothing starts with: a comment, or the end of a block or a list. */
    private val CLOSINGS = listOf("//", "/*", "*", "}", ")", "]")

    /** What a line of code names that only the jvm declares: `System`, or a declaration of the JDK named in full. */
    private val JVM_NAME = Regex("""(?<![\w.])(?:System\.|java\.|javax\.)""")

    /** What a line of code names that only an Android app declares: the `BuildConfig` its build generates. */
    private val ANDROID_NAME = Regex("""(?<![\w.])BuildConfig\.""")

    /** Refusal is why the documents' fences cannot be read or compiled. */
    class Refusal(
        message: String,
    ) : IllegalArgumentException(message)

    /**
     * Printed is one fence as printed: the [snippet], the [target] it compiles on, the printed file's [source], and the
     * line of that file its body starts on, [bodyLine], which is the document's line [documentLine].
     */
    data class Printed(
        /** The fence, numbered among every fence of the documents. */
        val snippet: SkillSnippets.Snippet,
        /** Where it compiles. */
        val target: SkillSnippets.Target,
        /** The printed file. */
        val source: String,
        /** The line of the printed file its body's first line is on, counted from 1. */
        val bodyLine: Int,
        /** The line of the document its body's first line is on, counted from 1. */
        val documentLine: Int,
        /** How many lines its body holds. */
        val bodyLength: Int,
    ) {
        /** The object the printed file declares, after the fence's number. */
        val objectName: String = objectNameOf(snippet)

        /** The line of the index this fence is: the printed file's [path], then where its body is in each. */
        fun indexLine(path: String): String =
            listOf(path, bodyLine, snippet.document, snippet.fence.line, documentLine, bodyLength).joinToString("\t")
    }

    /**
     * Returns every Kotlin fence of [documents], each a document's name as a message shows it paired with its
     * Markdown, in the order given and numbered from 1 across them all.
     */
    fun snippets(documents: List<Pair<String, String>>): List<SkillSnippets.Snippet> {
        val snippets = mutableListOf<SkillSnippets.Snippet>()
        for ((document, markdown) in documents) {
            val fences =
                try {
                    ReadmeSnippets.fences(markdown)
                } catch (refusal: ReadmeSnippets.Refusal) {
                    throw Refusal("$document: ${refusal.message}")
                }
            for (fence in fences) snippets += SkillSnippets.Snippet(document, snippets.size + 1, fence)
        }
        return snippets
    }

    /**
     * Returns where [snippet] compiles: where its imports say ([SkillSnippets.targetOf]), else Android's code where its
     * code reads an app's `BuildConfig`, the jvm's where it reads `System` or names a `java.` or `javax.` declaration
     * in full, and common code otherwise.
     */
    fun targetOf(
        snippet: SkillSnippets.Snippet,
        declarations: SkillSnippets.Declarations,
    ): SkillSnippets.Target {
        val target =
            try {
                SkillSnippets.targetOf(snippet, declarations)
            } catch (refusal: SkillSnippets.Refusal) {
                throw Refusal(refusal.message.orEmpty())
            }
        if (target != SkillSnippets.Target.COMMON) return target
        val code = SkillSnippets.bodyOf(snippet).filterNot { it.trimStart().startsWith("//") }
        return when {
            code.any { ANDROID_NAME.containsMatchIn(it) } -> SkillSnippets.Target.ANDROID
            code.any { JVM_NAME.containsMatchIn(it) } -> SkillSnippets.Target.JVM
            else -> target
        }
    }

    /**
     * Answers whether [snippet] is a fence of declarations, as a file a consumer pastes them into holds them: each of
     * its unindented lines, its comments and the lines that close a block aside, opens a function, a class, an object,
     * an interface, an annotation or a property behind a modifier. Any other fence is the statements of a function's
     * body.
     */
    fun isDeclarations(snippet: SkillSnippets.Snippet): Boolean {
        val openings =
            SkillSnippets
                .bodyOf(snippet)
                .filter { it.isNotBlank() && !it.first().isWhitespace() }
                .filterNot { line -> CLOSINGS.any { line.startsWith(it) } }
        return openings.isNotEmpty() && openings.all { DECLARATION.containsMatchIn(it) }
    }

    /**
     * Prints [snippet] for [target] into a file of its own, in [PACKAGE]: a fence of declarations ([isDeclarations])
     * as the members of the file's object, any other as a function's body, a composable's where [composable], and
     * answers it with where its body landed.
     */
    fun print(
        snippet: SkillSnippets.Snippet,
        target: SkillSnippets.Target,
        composable: Boolean,
    ): Printed {
        val objectName = objectNameOf(snippet)
        val declarations = isDeclarations(snippet)
        val source =
            SkillSnippets.source(
                header =
                    listOf(
                        "Printed by `mise run snippets` from ${snippet.document}, " +
                            "the fence at line ${snippet.fence.line},",
                        "for the compiler alone: never committed, and written again by every run.",
                    ),
                packageName = PACKAGE,
                objectName = objectName,
                about =
                    listOf(
                        "$objectName is the Kotlin fence at line ${snippet.fence.line} of ${snippet.document},",
                        "compiled in ${target.what}, written as a consumer pastes it.",
                    ),
                snippets = listOf(snippet),
                composable = if (composable && !declarations) setOf(snippet) else emptySet(),
                members = if (declarations) setOf(snippet) else emptySet(),
            )
        val opening = if (declarations) "// ${snippet.place}." else "fun snippet${snippet.number}() {"
        val bodyLine = source.lines().indexOfFirst { it.trimEnd().endsWith(opening) } + 2
        val code = snippet.fence.code
        val first = code.indexOfFirst { it.isNotEmpty() && !it.startsWith("import ") }
        val documentLine = snippet.fence.line + 1 + maxOf(first, 0)
        return Printed(snippet, target, source, bodyLine, documentLine, SkillSnippets.bodyOf(snippet).size)
    }

    /**
     * Returns the tally of a run: how many of [snippets] each target compiles, as [sorted] places them, in how many
     * [documents], then each fence skipped, by its place and its reason.
     */
    fun tally(
        snippets: List<SkillSnippets.Snippet>,
        sorted: Map<SkillSnippets.Snippet, SkillSnippets.Target>,
        documents: Int,
    ): List<String> {
        val skipped = snippets.filter { it.fence.skipped != null }
        val counts =
            SkillSnippets.Target.entries.joinToString(", ") { target ->
                "${sorted.values.count { it == target }} in ${target.sourceSet}"
            }
        val head =
            "Kotlin fences compiled: ${sorted.size} ($counts); skipped: ${skipped.size}; documents read: $documents."
        return listOf(head) + skipped.map { "Skipped: ${it.document}:${it.fence.line}: ${it.fence.skipped}." }
    }

    /** The object [snippet]'s printed file declares, after its number, so no two fences declare one name. */
    private fun objectNameOf(snippet: SkillSnippets.Snippet): String = "Snippet${snippet.number}"
}
