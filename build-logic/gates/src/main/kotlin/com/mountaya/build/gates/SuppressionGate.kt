package com.mountaya.build.gates

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * SuppressionGate counts suppressions (SPEC section 7).
 *
 * A suppression, Kotlin's `@Suppress`, Android Lint's `@SuppressLint` or Java's `@SuppressWarnings`,
 * appears only as an exemption this gate counts, each with the sentence that justifies it. A
 * suppression with no sentence is a claim nobody made.
 */
object SuppressionGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "checkSuppressions"

    /** The annotations that suppress a warning, by the short name a source spells them with. */
    private val SUPPRESSIONS = setOf("Suppress", "SuppressLint", "SuppressWarnings")

    /**
     * Runs the gate over [trees]. An exemption is keyed by the file and what it suppresses, as
     * `<module>/<source set>/<path>: @<annotation>(<arguments>)`.
     */
    fun run(
        trees: List<SourceTree>,
        exemptions: Map<String, String>,
    ): GateResult {
        val exempt = Exemptions(exemptions)
        val violations = mutableListOf<String>()
        var checked = 0

        KotlinParser().use { parser ->
            for (source in trees.kotlinFiles()) {
                if (source.isGenerated) continue
                checked += 1
                val file = parser.parse(source.file)
                for (annotation in file.collectDescendantsOfType<KtAnnotationEntry>()) {
                    val name = annotation.shortName?.asString() ?: continue
                    if (name !in SUPPRESSIONS) continue
                    val arguments = annotation.valueArguments.joinToString(", ") { it.asElement().text }
                    val key = "${source.display}: @$name($arguments)"
                    if (exempt.covers(key)) continue
                    violations += "$key (line ${annotation.line()}): a suppression with no exemption. " +
                        "Add it to the suppression table with the sentence that justifies it."
                }
            }
        }

        violations += exempt.leftovers(NAME)
        return GateResult(NAME, checked, exempt.count, violations)
    }
}
