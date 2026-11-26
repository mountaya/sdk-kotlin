package com.mountaya.build.gates

import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

/**
 * DeterminismGate holds determinism to be a boundary too (SPEC section 7).
 *
 * A rule that reads the clock, a random source, the default locale, the system time zone or a
 * dispatcher cannot be held to a fixture and cannot be reproduced by a second language. Each of
 * those reads is refused outside `seams/`, the one topic in which the environment is read. So
 * is writing to a log stream, which is a read of the environment in the other direction.
 */
object DeterminismGate {
    /** The task name the root build registers this gate under. */
    const val NAME = "checkDeterminism"

    private const val SEAMS = "seams"

    /** A name that is the read itself, wherever it appears. */
    private val REFUSED_NAMES =
        mapOf(
            "Random" to "a random source is a seam",
            "Dispatchers" to "a dispatcher is injected, never named",
            "GlobalScope" to "a scope is the caller's",
            "SharedPreferences" to "stored state is a seam",
            "NSUserDefaults" to "stored state is a seam",
            "println" to "a library writes to no log stream",
        )

    /** A receiver and a member that together are the read. */
    private val REFUSED_MEMBERS =
        mapOf(
            ("Clock" to "System") to "the clock is a seam",
            ("Instant" to "now") to "the clock is a seam",
            ("System" to "currentTimeMillis") to "the clock is a seam",
            ("System" to "nanoTime") to "the clock is a seam",
            ("Uuid" to "random") to "a random source is a seam",
            ("UUID" to "randomUUID") to "a random source is a seam",
            ("Locale" to "getDefault") to "a locale is an argument",
            ("NSLocale" to "currentLocale") to "a locale is an argument",
            ("TimeZone" to "getDefault") to "a time zone is an argument",
            ("TimeZone" to "currentSystemDefault") to "a time zone is an argument",
            ("ZoneId" to "systemDefault") to "a time zone is an argument",
            ("NSTimeZone" to "localTimeZone") to "a time zone is an argument",
            ("NSTimeZone" to "systemTimeZone") to "a time zone is an argument",
            ("Log" to "d") to "a library writes to no log stream",
            ("Log" to "e") to "a library writes to no log stream",
            ("Log" to "i") to "a library writes to no log stream",
            ("Log" to "w") to "a library writes to no log stream",
        )

    /** Runs the gate over the main source sets of [trees], outside `seams/`. */
    fun run(trees: List<SourceTree>): GateResult {
        val violations = mutableListOf<String>()
        var checked = 0

        KotlinParser().use { parser ->
            for (source in trees.filterNot { it.isTest }.kotlinFiles()) {
                if (source.isGenerated || source.topic == SEAMS) continue
                checked += 1
                val file = parser.parse(source.file)

                val references =
                    file.collectDescendantsOfType<KtNameReferenceExpression>().map {
                        it.getReferencedName() to it.line()
                    }
                val typeNames =
                    file.collectDescendantsOfType<KtUserType>().mapNotNull { type ->
                        type.referencedName?.let { name -> name to type.line() }
                    }
                val names = references + typeNames
                for ((name, line) in names) {
                    val because = REFUSED_NAMES[name] ?: continue
                    violations += "${source.display}:$line: `$name` outside `$SEAMS/`: $because."
                }

                for (expression in file.collectDescendantsOfType<KtDotQualifiedExpression>()) {
                    val receiver = expression.receiverExpression.text.substringAfterLast('.')
                    val selector = expression.selectorExpression ?: continue
                    val member = if (selector is KtCallExpression) selector.calleeExpression?.text else selector.text
                    val because = REFUSED_MEMBERS[receiver to member] ?: continue
                    violations +=
                        "${source.display}:${expression.line()}: `$receiver.$member` outside `$SEAMS/`: $because."
                }
            }
        }
        return GateResult(NAME, checked, 0, violations.distinct())
    }
}
