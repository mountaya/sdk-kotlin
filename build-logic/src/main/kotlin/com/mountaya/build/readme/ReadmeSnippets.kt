package com.mountaya.build.readme

/**
 * ReadmeSnippets prints the Kotlin snippets of a Markdown document as one Kotlin file, which the
 * consumer build compiles against the real modules (SPEC section 10).
 *
 * Every fence tagged `kotlin` is printed, each into a function of its own, since a snippet is
 * written as the statements a consumer pastes; its `import` lines are gathered at the top of the
 * file. A fence that cannot be compiled opts out with a marker on the line above it, which names
 * its reason, `<!-- kotlin:skip a build script, which the consumer build's own declares -->`, and
 * the printed file lists every fence that opted out, so an opt-out is read in review rather than
 * taken on trust.
 */
object ReadmeSnippets {
    /** The marker that opts the fence below it out of the compile. */
    const val SKIP_MARKER = "<!-- kotlin:skip"

    /** The run of backticks a fence opens with, at the least. */
    private const val FENCE = "```"

    /** Fence is one Kotlin fence of the document. */
    data class Fence(
        /** The line of its opening, counted from 1. */
        val line: Int,
        /** Its lines, as written. */
        val code: List<String>,
        /** The reason its marker names, or null where it is compiled. */
        val skipped: String? = null,
    )

    /** Refusal is why a document's fences cannot be read. */
    class Refusal(
        message: String,
    ) : IllegalArgumentException(message)

    /**
     * Reads every fenced block tagged `kotlin` in [markdown], in order, wherever it stands: inside a
     * list item or a tag of the document's own markup, `{% code-block %}`, as at the top level.
     *
     * A fence opens on a line of three backticks or more, indented or not, and the first word after them
     * names its language, so `kotlin {% title="Map" %}` is a Kotlin fence; it closes on a line of at least
     * as many backticks and nothing else, and its lines are read without the indentation of its opening.
     * It refuses a fence that is never closed, and a skip marker that names no reason or stands above no
     * Kotlin fence: the marker is the line directly above the fence's opening, at any indentation.
     */
    fun fences(markdown: String): List<Fence> {
        val lines = markdown.split("\n").map { it.trimEnd(' ', '\t', '\r') }
        val fences = mutableListOf<Fence>()
        var index = 0
        var marker: Pair<Int, String>? = null
        while (index < lines.size) {
            val line = lines[index]
            val text = line.trimStart(' ', '\t')
            if (text.startsWith(SKIP_MARKER)) {
                if (!text.endsWith("-->")) throw Refusal("line ${index + 1}: a skip marker ends on its own line")
                val reason = text.removePrefix(SKIP_MARKER).removeSuffix("-->").trim(' ')
                if (reason.isEmpty()) throw Refusal("line ${index + 1}: a skip marker names the reason it skips")
                marker = index + 1 to reason
                index += 1
                continue
            }
            if (!text.startsWith(FENCE)) {
                val pending = marker
                if (pending != null && line.isNotEmpty()) {
                    throw Refusal("line ${pending.first}: a skip marker stands above no fence")
                }
                index += 1
                continue
            }
            val indentation = line.length - text.length
            val ticks = text.takeWhile { it == '`' }.length
            val language =
                text
                    .drop(ticks)
                    .trim(' ', '\t')
                    .substringBefore(' ')
                    .substringBefore('\t')
            val opening = index
            val code = mutableListOf<String>()
            index += 1
            while (index < lines.size && !closes(lines[index], ticks)) {
                code += lines[index].dropIndentation(indentation)
                index += 1
            }
            if (index >= lines.size) throw Refusal("line ${opening + 1}: a fence that is never closed")
            index += 1
            val pending = marker
            if (language == "kotlin") {
                fences += Fence(opening + 1, code, pending?.second)
            } else if (pending != null) {
                throw Refusal("line ${pending.first}: a skip marker stands above no Kotlin fence")
            }
            marker = null
        }
        marker?.let { throw Refusal("line ${it.first}: a skip marker stands above no fence") }
        return fences
    }

    /** Whether [line] closes a fence opened with [ticks] backticks: as many or more, and nothing else. */
    private fun closes(
        line: String,
        ticks: Int,
    ): Boolean {
        val text = line.trim(' ', '\t')
        return text.length >= ticks && text.all { it == '`' }
    }

    /** The line without the first [indentation] characters of white space it starts with, as many as it has. */
    private fun String.dropIndentation(indentation: Int): String {
        val blank = takeWhile { it == ' ' || it == '\t' }.length
        return drop(minOf(blank, indentation))
    }

    /**
     * Prints the Kotlin file [fences] compile as, in [packageName], named after [document], its
     * header naming the command that prints it afresh, [rewrite].
     */
    fun file(
        fences: List<Fence>,
        document: String,
        rewrite: String,
        packageName: String,
    ): String {
        val compiled = fences.filter { it.skipped == null }
        val imports = LinkedHashSet<String>()
        for (fence in compiled) {
            for (line in fence.code) if (line.startsWith("import ")) imports += line
        }
        val text = StringBuilder()
        text.append("// Printed from $document by the build. Never edit it: edit the document, then run\n")
        text.append("//   $rewrite\n")
        text.append("// and the diff is the review.\n")
        text.append("//\n")
        text.append("// ${compiled.size} snippets compiled, ${fences.size - compiled.size} skipped.\n")
        for (fence in fences) fence.skipped?.let { text.append("// Skipped: $it.\n") }
        text.append("\npackage $packageName\n\n")
        for (line in imports) text.append(line).append('\n')
        if (imports.isNotEmpty()) text.append('\n')
        text.append("/**\n")
        text.append(" * ReadmeSnippets is every Kotlin snippet of $document, each in a function of its own, written\n")
        text.append(" * as a consumer pastes it.\n")
        text.append(" */\n")
        text.append("internal object ReadmeSnippets {\n")
        for ((position, fence) in compiled.withIndex()) {
            if (position > 0) text.append('\n')
            text.append("    suspend fun snippet${position + 1}() {\n")
            val body = fence.code.filterNot { it.startsWith("import ") }.dropWhile { it.isEmpty() }
            for (line in body) text.append(if (line.isEmpty()) "\n" else "        $line\n")
            text.append("    }\n")
        }
        text.append("}\n")
        return text.toString()
    }
}
