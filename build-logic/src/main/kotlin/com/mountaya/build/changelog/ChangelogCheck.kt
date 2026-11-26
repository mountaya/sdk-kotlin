package com.mountaya.build.changelog

import java.time.LocalDate

/**
 * ChangelogCheck is the changelog of SPEC section 10, held to its shape and to the API it names. It is the one document
 * no generator stands behind and no compiler reads, so a name it gives that no module publishes any more reads as true
 * until a reader looks for it.
 *
 * The shape is Keep a Changelog's, as `sdk-swift` writes it: `## [Unreleased]` first, holding what
 * the next release carries, then each release as `## [MAJOR.MINOR.PATCH] - YYYY-MM-DD`, newest
 * first. A name is every backticked word that reads as one: a module, its coordinate, or a
 * declaration path, each of whose parts a module's ABI dump declares.
 */
object ChangelogCheck {
    /** The heading of the section that holds what the next release carries. */
    const val UNRELEASED = "## [Unreleased]"

    /** Words the changelog writes in backticks that no module declares: the targets a module is built for. */
    val NOT_DECLARATIONS: Set<String> = setOf("jvm", "android", "iosArm64", "iosSimulatorArm64")

    private val RELEASE = Regex("""^## \[(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)] - (\d{4}-\d{2}-\d{2})$""")
    private val QUOTED = Regex("`([^`\n]+)`")
    private val PATH = Regex("""^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)*(\(\))?$""")
    private val COORDINATE = Regex("""^([a-z][a-z0-9.]*):([a-z][a-z0-9-]*)$""")
    private val MODULE = Regex("""^mountaya-[a-z-]+$""")
    private val IDENTIFIER = Regex("""^[A-Za-z_][A-Za-z0-9_]*$""")
    private val JVM_CLASS = Regex("""^public (?:[a-z]+ )*class ([\w/$]+)""")
    private val JVM_MEMBER = Regex("""^\s+public (?:[a-z]+ )*(?:fun|field) ([^\s(]+)""")
    private val ACCESSOR = Regex("""^(?:get|set|is)([A-Z].*)$""")
    private const val COMPOSITION_LOCAL = "CompositionLocal;"

    /**
     * Returns every way [changelog] falls short: a section out of shape or out of order, and a name in backticks that
     * is not one of [modules], their coordinate under [group], or a path whose every part is in [published].
     */
    fun problems(
        changelog: String,
        group: String,
        modules: Set<String>,
        published: Set<String>,
    ): List<String> = shapeProblems(changelog) + nameProblems(changelog, group, modules, published)

    /**
     * Returns the names [dump] publishes: every declaration a klib dump signs, and every class and member a jvm or
     * android dump lists, a property under its getter's name and its own, a `CompositionLocal` under its own in
     * UpperCamelCase too, as the naming law spells it, and a member whose JVM name Kotlin mangles, since it takes or
     * answers a value class (`Glyph-NKG1LGE`), under the name it is declared with.
     */
    fun publishedNames(dump: String): Set<String> {
        val names = mutableSetOf<String>()
        for (line in dump.lines()) {
            val signature = line.substringAfter(" // ", "").substringBefore('|')
            if (line.isNotBlank() && !line.trimStart().startsWith("//") && '/' in signature) {
                signature.substringAfter('/').split('.').filterTo(names) { IDENTIFIER.matches(it) }
                continue
            }
            JVM_CLASS.find(line)?.let { match ->
                match.groupValues[1]
                    .substringAfterLast('/')
                    .split('$')
                    .filterTo(names) { IDENTIFIER.matches(it) }
            }
            JVM_MEMBER.find(line)?.let { match ->
                // A mangled name is the declared name, a hyphen, and what Kotlin appends: no declared
                // name holds a hyphen.
                val member = match.groupValues[1].substringBefore('$').substringBefore('-')
                if (IDENTIFIER.matches(member)) {
                    names += member
                    val property = ACCESSOR.matchEntire(member)?.groupValues?.get(1)
                    if (property != null) names += property.replaceFirstChar(Char::lowercaseChar)
                    // The naming law spells a `CompositionLocal` in UpperCamelCase (SPEC.md section 5), and
                    // its getter's name reads as a lowerCamelCase property's would: one answering a
                    // `CompositionLocal` is published under its name as the law spells it too.
                    if (property != null && line.trimEnd().endsWith(COMPOSITION_LOCAL)) names += property
                }
            }
        }
        return names
    }

    private fun shapeProblems(changelog: String): List<String> {
        val headings = changelog.lines().filter { it.startsWith("## ") }
        val problems = mutableListOf<String>()
        if (headings.firstOrNull() != UNRELEASED) {
            problems += "The first section is ${headings.firstOrNull() ?: "none"}, where it is $UNRELEASED."
        }
        var newer: List<Int>? = null
        for (heading in headings.drop(1)) {
            val release = RELEASE.matchEntire(heading)
            if (release == null) {
                problems += "`$heading` is no `## [MAJOR.MINOR.PATCH] - YYYY-MM-DD`."
                continue
            }
            if (runCatching { LocalDate.parse(release.groupValues[4]) }.isFailure) {
                problems += "`$heading` dates no day of the calendar."
            }
            val numbers = (1..3).map { release.groupValues[it].toInt() }
            if (newer != null && compare(numbers, newer) >= 0) {
                problems += "`$heading` is not older than the release above it, and the releases go newest first."
            }
            newer = numbers
        }
        return problems
    }

    private fun nameProblems(
        changelog: String,
        group: String,
        modules: Set<String>,
        published: Set<String>,
    ): List<String> {
        val unknown = mutableListOf<String>()
        for (token in QUOTED.findAll(changelog).map { it.groupValues[1] }) {
            val coordinate = COORDINATE.matchEntire(token)
            val known =
                when {
                    coordinate != null -> coordinate.groupValues[1] == group && coordinate.groupValues[2] in modules
                    MODULE.matches(token) -> token in modules
                    token == group || !PATH.matches(token) -> true
                    else -> token.removeSuffix("()").split('.').all { it in published || it in NOT_DECLARATIONS }
                }
            if (!known) unknown += token
        }
        return unknown.distinct().map {
            "`$it` is no module, no coordinate of one and no declaration a module publishes: rename it, " +
                "or, where it is no declaration, add it to ChangelogCheck.NOT_DECLARATIONS."
        }
    }

    private fun compare(
        a: List<Int>,
        b: List<Int>,
    ): Int = a.zip(b).map { (x, y) -> x.compareTo(y) }.firstOrNull { it != 0 } ?: 0
}
