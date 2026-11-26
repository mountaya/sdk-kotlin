package com.mountaya.build.contract

/**
 * ApiDeclarations is what the modules' committed ABI dumps declare, each declaration by its path after its package, the
 * spelling the registry's `kotlin` column records (SPEC.md sections 5 and 9): a type by its own name, a nested one
 * through the types it is nested in (`ApiException.IdempotentAnswer`), a top-level function or property by its name,
 * and a member through its type, a companion's as its type's, since a consumer calls one as `MapPaint.standard`.
 * Nothing is read by its package, so a declaration answers wherever its module files it.
 *
 * A klib dump is read by the signature each declaration carries, which spells it as Kotlin does. A
 * jvm or android dump spells it as the JVM does: a nested class `Outer$Inner` is read as `Outer.Inner`,
 * a name Kotlin mangles because it takes or answers a value class without what Kotlin appended
 * (`convertLength-4iIbNF4`), and a top-level property, which a file facade carries as its getter,
 * under its own name too (`getLivelyPalette` as `livelyPalette`). A declaration one target alone has,
 * the jvm's `constructWebhookEvent` or Android's `MapView`, is in that target's dump alone, so every
 * dump is read.
 */
class ApiDeclarations private constructor(
    /** Every declaration's path after its package, `Companion` left out: `MapPaint.standard`. */
    val paths: Set<String>,
) {
    /** Whether nothing was read: no dump, or dumps that declare nothing. */
    val isEmpty: Boolean get() = paths.isEmpty()

    /** Answers whether a dump declares [path], spelled exactly as its path after its package. */
    fun declares(path: String): Boolean = path in paths

    /** The reading of the dumps. */
    companion object {
        /** Returns what [dumps] declare, each the text of one ABI dump, klib or jvm. */
        fun read(dumps: Collection<String>): ApiDeclarations {
            val paths = mutableSetOf<String>()
            for (dump in dumps) {
                if (dump.startsWith(KLIB_HEADER)) readKlib(dump, paths) else readJvm(dump, paths)
            }
            return ApiDeclarations(paths)
        }

        /** The first line of a klib dump. */
        private const val KLIB_HEADER = "// Klib ABI Dump"

        /** The name Kotlin gives a companion object that names none of its own, which a consumer never writes. */
        private const val COMPANION = "Companion"

        private val IDENTIFIER = Regex("""^[A-Za-z_][A-Za-z0-9_]*$""")
        private val JVM_CLASS = Regex("""^public (?:[a-z]+ )*class ([\w/$]+)""")
        private val JVM_MEMBER = Regex("""^\s+public (?:[a-z]+ )*(?:fun|field) ([^\s(]+)""")
        private val JVM_GETTER = Regex("""^get([A-Z].*)$""")

        /** What a class Kotlin declares holds and a file facade never does: a constructor, a companion, an object's instance. */
        private val NOT_A_FACADE = listOf("<init>", " field Companion ", " field INSTANCE ")

        /** [names] as a path, `Companion` left out: `MapPaint.Companion.standard` is `MapPaint.standard`. */
        private fun pathOf(names: List<String>): String? =
            names.filter { it != COMPANION }.takeIf { it.isNotEmpty() }?.joinToString(".")

        /**
         * Reads a klib dump: each declaration line ends on `// <package>/<path>|<signature>`, and the
         * keyword before its name says it declares something. An accessor, `<get-x>`, and a
         * constructor, `<init>`, name nothing of their own.
         */
        private fun readKlib(
            dump: String,
            into: MutableSet<String>,
        ) {
            for (line in dump.lineSequence()) {
                val declaration = line.substringBefore(" // ", "").trim()
                if (declaration.isEmpty() || !declares(declaration)) continue
                val signature = line.substringAfter(" // ").substringBefore('|')
                if ('/' !in signature) continue
                val names = signature.substringAfter('/').split('.')
                if (names.any { !IDENTIFIER.matches(it) }) continue
                pathOf(names)?.let(into::add)
            }
        }

        /**
         * Whether a klib dump's [declaration] declares a type, a function, a property or an enum entry,
         * read off its first keyword: a constructor, and a line of anything else, declares nothing.
         */
        private fun declares(declaration: String): Boolean {
            for (word in declaration.split(' ')) {
                when (word) {
                    "class", "interface", "object", "fun", "val", "var", "entry" -> return true
                    "constructor" -> return false
                }
            }
            return false
        }

        /**
         * Reads a jvm or android dump: a class line names the class through the classes it is nested
         * in, `Outer$Inner`, and each member line below it a member, until the class closes. A file
         * facade holds top-level declarations and is no type: a top-level class named `<File>Kt`, or one
         * a file renames with `@file:JvmName`, `SecretKeyCredential`, which is a final class holding static
         * members alone, no constructor and no companion, as no class Kotlin declares is.
         */
        private fun readJvm(
            dump: String,
            into: MutableSet<String>,
        ) {
            var owner: List<String>? = null
            var header = ""
            val members = mutableListOf<String>()

            fun close() {
                val names = owner ?: return
                val isFacade =
                    names.size == 1 &&
                        (
                            names.single().endsWith("Kt") ||
                                (
                                    " final class " in " $header" && members.isNotEmpty() &&
                                        members.all { " static " in it } &&
                                        members.none { line -> NOT_A_FACADE.any { it in line } }
                                )
                        )
                if (!isFacade) pathOf(names)?.let(into::add)
                for (line in members) {
                    val member = JVM_MEMBER.find(line) ?: continue
                    // A mangled name is the declared one, a hyphen and what Kotlin appended, and a synthetic
                    // one the declared one, a dollar and its role: no declared name holds either.
                    val name = member.groupValues[1].substringBefore('$').substringBefore('-')
                    if (!IDENTIFIER.matches(name)) continue
                    if (isFacade) {
                        into += name
                        val property = JVM_GETTER.matchEntire(name)?.groupValues?.get(1)
                        if (property != null) into += property.replaceFirstChar(Char::lowercaseChar)
                    } else {
                        pathOf(names + name)?.let(into::add)
                    }
                }
            }

            for (line in dump.lineSequence()) {
                val found = JVM_CLASS.find(line)
                if (found != null) {
                    close()
                    val names = found.groupValues[1].substringAfterLast('/').split('$')
                    owner = names.takeIf { all -> all.all { IDENTIFIER.matches(it) } }
                    header = line
                    members.clear()
                    continue
                }
                if (line.startsWith("}")) {
                    close()
                    owner = null
                    members.clear()
                    continue
                }
                if (owner != null) members += line
            }
            close()
        }
    }
}
