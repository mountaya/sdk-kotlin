package com.mountaya.build.readme

import com.mountaya.build.contract.ApiDeclarations

/**
 * SkillSnippets prints the Kotlin snippets of this SDK's developer skill, which the `ai` repository
 * holds, into the consumer build, which compiles them against the real modules as it compiles the
 * README's (SPEC section 10), and holds the floors the skill states to the build's own.
 *
 * The skill is read from the `ai` checkout at build time, never from a copy: `SKILL.md` first, then
 * every other document below the skill by path. Each fence is read as the README's are, a skip marker
 * honoured ([ReadmeSnippets.fences]), and sorted by its imports into the consumer's common code, or its
 * jvm or Android code where an import names what that target alone declares, as the committed ABI
 * dumps say, or a package that target alone has.
 */
object SkillSnippets {
    /** The skill's directory in the `ai` checkout. */
    const val SKILL = "skills/mountaya-sdk-kotlin"

    /** The document an agent reads first, before the others. */
    const val ENTRY = "SKILL.md"

    /** The packages Android alone compiles: the platform's, AndroidX's, MapLibre's and the HTTP client of `MapView`. */
    private val ANDROID_PACKAGES = listOf("android.", "androidx.", "org.maplibre.", "okhttp3.")

    /** The packages the JVM has and common code does not. */
    private val JVM_PACKAGES = listOf("java.", "javax.")

    /** The package every module of this SDK declares below. */
    private const val MOUNTAYA = "com.mountaya."

    /** The packages a fence importing from is composable code on Android. */
    private const val COMPOSE = "androidx.compose."

    /** The annotation a composable snippet's function carries. */
    private const val COMPOSABLE = "androidx.compose.runtime.Composable"

    /** Refusal is why the skill's fences cannot be read or compiled. */
    class Refusal(
        message: String,
    ) : IllegalArgumentException(message)

    /**
     * Snippet is one Kotlin fence of the skill: the [document] it is in, below the skill, and its [number] among them
     * all.
     */
    data class Snippet(
        /** The document it is in, below the skill. */
        val document: String,
        /** Its number among every fence of the skill. */
        val number: Int,
        /** The fence. */
        val fence: ReadmeSnippets.Fence,
    ) {
        /** Where the fence opens, as a message names it. */
        val place: String = "$document, line ${fence.line}"

        /** Its `import` lines, as written. */
        val imports: List<String> = fence.code.filter { it.startsWith("import ") }
    }

    /** Target is where a fence compiles: a source set of the consumer, and the object its functions sit in there. */
    enum class Target(
        /** The consumer's source set it compiles in. */
        val sourceSet: String,
        /** The object its functions sit in there. */
        val objectName: String,
        /** What it is, as a message names it. */
        val what: String,
    ) {
        /** The common code, which every target compiles. */
        COMMON("commonMain", "SkillSnippets", "common code"),

        /** The jvm's code. */
        JVM("jvmMain", "JvmSkillSnippets", "the jvm's code"),

        /** The Android target's code. */
        ANDROID("androidMain", "AndroidSkillSnippets", "Android's code"),
    }

    /**
     * Declarations is what each target declares, read off the committed ABI dumps: the klib's, which every target
     * shares, the jvm's and Android's, and the composables Android's declares.
     */
    class Declarations(
        /** What every target declares, read off the klib's dump. */
        val common: ApiDeclarations,
        /** What the jvm declares. */
        val jvm: ApiDeclarations,
        /** What Android declares. */
        val android: ApiDeclarations,
        /** The top-level functions Android's dumps declare that take a `Composer`, so are composables. */
        val composables: Set<String>,
    ) {
        /** The reading of the dumps. */
        companion object {
            private val COMPOSABLE_FUNCTION = Regex("""^\s+public static (?:final )?fun ([^\s(]+) \(([^)]*)\)""")
            private const val COMPOSER = "Landroidx/compose/runtime/Composer;"

            /** Returns what [klib], [jvm] and [android], each the text of one dump of that kind, declare. */
            fun read(
                klib: List<String>,
                jvm: List<String>,
                android: List<String>,
            ): Declarations {
                val composables =
                    android
                        .flatMap { it.lineSequence() }
                        .mapNotNull { COMPOSABLE_FUNCTION.find(it) }
                        .filter { COMPOSER in it.groupValues[2] }
                        .map { it.groupValues[1].substringBefore('-') }
                        .toSet()
                return Declarations(
                    ApiDeclarations.read(klib),
                    ApiDeclarations.read(jvm),
                    ApiDeclarations.read(android),
                    composables,
                )
            }
        }
    }

    /** Returns [paths], the skill's documents below its directory, in the order a reader meets them: [ENTRY] first. */
    fun documentOrder(paths: Collection<String>): List<String> =
        paths.sortedWith(compareBy<String> { it != ENTRY }.thenBy { it })

    /**
     * Returns every Kotlin fence of [documents], each document's path below the skill mapped to its Markdown, read in
     * [documentOrder] and numbered from 1 across them all.
     */
    fun snippets(documents: Map<String, String>): List<Snippet> {
        val snippets = mutableListOf<Snippet>()
        for (document in documentOrder(documents.keys)) {
            val fences =
                try {
                    ReadmeSnippets.fences(documents.getValue(document))
                } catch (refusal: ReadmeSnippets.Refusal) {
                    throw Refusal("$document: ${refusal.message}")
                }
            for (fence in fences) snippets += Snippet(document, snippets.size + 1, fence)
        }
        return snippets
    }

    /**
     * Returns what the skill's [documents] state of the build's [floors], each keyed by its catalog key and valued by
     * the catalog's figure, that the catalog does not: a floor stated nowhere, and every statement of one that names
     * another figure, each read in the forms [Floors] reads.
     */
    fun floorProblems(
        documents: Map<String, String>,
        floors: Map<String, String>,
    ): List<String> {
        val problems = mutableListOf<String>()
        for (floor in Floors.all) {
            val value =
                floors[floor.key]
                    ?: throw Refusal("The build hands no `${floor.key}`, the floor of ${floor.what}.")
            var stated = false
            for (document in documentOrder(documents.keys)) {
                val text = documents.getValue(document)
                for (statement in Floors.statements(floor, text)) {
                    stated = true
                    if (statement.figure == value) continue
                    val line = Floors.lineOf(text, statement.offset)
                    problems += "$document, line $line: `${statement.words}` states ${floor.what} " +
                        "as ${statement.figure}, and the version catalog's `${floor.key}` is $value."
                }
            }
            if (!stated) {
                problems += "The Kotlin skill states nowhere ${floor.what}: the version catalog's `${floor.key}` " +
                    "is $value, which it states as `${floor.example.format(value)}`."
            }
        }
        return problems
    }

    /**
     * Returns where [snippet] compiles: Android's code where an import names a package Android alone has or a
     * declaration Android's dumps alone declare, the jvm's where one names a declaration the jvm's dumps alone declare
     * or a package the JVM has and common code does not, and common code otherwise. A snippet that needs both the jvm
     * alone and Android compiles nowhere, and is refused.
     */
    fun targetOf(
        snippet: Snippet,
        declarations: Declarations,
    ): Target {
        var android: String? = null
        var jvmAlone: String? = null
        var jvm: String? = null
        for (line in snippet.imports) {
            val name = imported(line)
            when {
                ANDROID_PACKAGES.any { name.startsWith(it) } -> {
                    android = android ?: name
                }

                name.startsWith(MOUNTAYA) -> {
                    when (declaredIn(name, declarations)) {
                        Target.ANDROID -> android = android ?: name
                        Target.JVM -> jvmAlone = jvmAlone ?: name
                        Target.COMMON, null -> Unit
                    }
                }

                JVM_PACKAGES.any { name.startsWith(it) } -> {
                    jvm = jvm ?: name
                }
            }
        }
        if (android != null && jvmAlone != null) {
            throw Refusal(
                "${snippet.place}: it imports `$jvmAlone`, which the jvm alone declares, and `$android`, which " +
                    "is Android's, so no target compiles it. Split it, or mark it `${ReadmeSnippets.SKIP_MARKER} " +
                    "<reason> -->`.",
            )
        }
        return when {
            android != null -> Target.ANDROID
            jvmAlone != null || jvm != null -> Target.JVM
            else -> Target.COMMON
        }
    }

    /** Answers whether [snippet], Android's code, is a composable's body: it imports Compose or a composable. */
    fun isComposable(
        snippet: Snippet,
        declarations: Declarations,
    ): Boolean =
        snippet.imports.map(::imported).any { name ->
            name.startsWith(COMPOSE) ||
                (name.startsWith(MOUNTAYA) && name.substringAfterLast('.') in declarations.composables)
        }

    /** The name an `import` line imports, without its alias. */
    private fun imported(line: String): String =
        line
            .removePrefix("import ")
            .substringBefore(" as ")
            .trim()

    /**
     * The target whose dumps alone declare [name], an import of this SDK's, read as a path after its
     * package: from its first part that leads with a capital, a type, or its last part, a top-level
     * function or property. Null where no dump declares it, which the compiler then says.
     */
    private fun declaredIn(
        name: String,
        declarations: Declarations,
    ): Target? {
        val parts = name.split('.')
        if (parts.last() == "*") return Target.COMMON
        val type = parts.indexOfFirst { it.firstOrNull()?.isUpperCase() == true }
        val path = (if (type >= 0) parts.drop(type) else listOf(parts.last())).joinToString(".")
        return when {
            declarations.common.declares(path) -> Target.COMMON
            declarations.jvm.declares(path) -> Target.JVM
            declarations.android.declares(path) -> Target.ANDROID
            else -> null
        }
    }

    /**
     * Prints the Kotlin file of [target]: the [snippets] sorted there, each in a function of its own named
     * after its number, a composable one in Android's code ([composable]) as a composable's body and every
     * other as a suspending function's, their imports gathered at the top, in [packageName]. Its header
     * says [tally], what every target compiled and what was skipped.
     */
    fun file(
        target: Target,
        snippets: List<Snippet>,
        composable: Set<Snippet>,
        tally: List<String>,
        packageName: String,
    ): String =
        source(
            header =
                listOf(
                    "Printed from the Kotlin skill, $SKILL of the ai checkout, by the build, for the compiler",
                    "alone: never committed, and written again by every run.",
                    "",
                ) + tally,
            packageName = packageName,
            objectName = target.objectName,
            about =
                listOf(
                    "${target.objectName} is every snippet of the Kotlin skill that compiles in",
                    "${target.what}, each in a function of its own named after its place among the",
                    "skill's snippets, written as a consumer pastes it.",
                ),
            snippets = snippets,
            composable = composable,
        )

    /**
     * Prints [snippets] as one Kotlin file: the comment lines of [header], then [packageName] and the snippets' imports
     * gathered, then the object [objectName], whose KDoc's lines are [about], holding each snippet in a function of its
     * own named after its number and led by a comment naming its place, a composable one ([composable]) as a
     * composable's body and every other as a suspending function's, or, for one of [members], a fence of declarations,
     * as the object's own members, where they read one another in any order, as a file's declarations do. Each line
     * of a snippet's body is printed on a line of its own, in its order, so a line of the printed file is a line of
     * the document.
     */
    fun source(
        header: List<String>,
        packageName: String,
        objectName: String,
        about: List<String>,
        snippets: List<Snippet>,
        composable: Set<Snippet>,
        members: Set<Snippet> = emptySet(),
    ): String {
        val imports = LinkedHashSet<String>()
        if (snippets.any { it in composable }) imports += "import $COMPOSABLE"
        for (snippet in snippets) imports += snippet.imports
        val text = StringBuilder()
        for (line in header) text.append(if (line.isEmpty()) "//\n" else "// $line\n")
        text.append("\npackage $packageName\n\n")
        for (line in imports) text.append(line).append('\n')
        if (imports.isNotEmpty()) text.append('\n')
        text.append("/**\n")
        for (line in about) text.append(" * $line\n")
        text.append(" */\n")
        text.append("internal object $objectName {\n")
        for ((position, snippet) in snippets.withIndex()) {
            if (position > 0) text.append('\n')
            text.append("    // ${snippet.place}.\n")
            if (snippet in members) {
                for (line in bodyOf(snippet)) text.append(if (line.isEmpty()) "\n" else "    $line\n")
                continue
            }
            if (snippet in composable) {
                text.append("    @Composable\n")
                text.append("    fun snippet${snippet.number}() {\n")
            } else {
                text.append("    suspend fun snippet${snippet.number}() {\n")
            }
            for (line in bodyOf(snippet)) text.append(if (line.isEmpty()) "\n" else "        $line\n")
            text.append("    }\n")
        }
        text.append("}\n")
        return text.toString()
    }

    /** The lines of [snippet] its function's body holds: all but its imports, from the first that is not blank. */
    fun bodyOf(snippet: Snippet): List<String> =
        snippet.fence.code
            .filterNot { it.startsWith("import ") }
            .dropWhile { it.isEmpty() }

    /**
     * Returns the lines every printed file's header carries: how many of [snippets] each target compiles, as [sorted]
     * places them, and the reason of each one skipped.
     */
    fun tally(
        snippets: List<Snippet>,
        sorted: Map<Snippet, Target>,
        documents: Int,
    ): List<String> {
        val skipped = snippets.filter { it.fence.skipped != null }
        val counts =
            Target.entries.joinToString(
                ", ",
            ) { target -> "${sorted.values.count { it == target }} in ${target.sourceSet}" }
        return listOf("${snippets.size} snippets in $documents documents: $counts, ${skipped.size} skipped.") +
            skipped.map { "Skipped: ${it.place}: ${it.fence.skipped}." }
    }
}
