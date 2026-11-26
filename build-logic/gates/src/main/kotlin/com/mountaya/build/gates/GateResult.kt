package com.mountaya.build.gates

/**
 * GateResult is what a gate found.
 *
 * `checked` and `exempt` are printed on every run, pass or fail, so a reviewer sees the
 * exemptions move.
 */
data class GateResult(
    /** The gate's task name. */
    val gate: String,
    /** How many things the gate checked. */
    val checked: Int,
    /** How many exemptions it used. */
    val exempt: Int,
    /** Every violation it found, one sentence each. */
    val violations: List<String>,
    /** The line a run prints, where a gate counts something other than what it checked and exempted. */
    val line: String? = null,
) {
    /** The one line a run prints. */
    val summary: String = line ?: "$gate: $checked checked, $exempt exempt"

    /** Returns the report a failing run shows: the summary, then one violation to a line. */
    fun report(): String = (listOf(summary) + violations.map { "  $it" }).joinToString("\n")
}

/**
 * Exemptions, keyed by what is exempt and valued by the sentence that justifies it.
 *
 * An exemption with no sentence is a claim nobody made, and one nothing uses is a leftover, so
 * both fail the gate that reads the table.
 */
class Exemptions(
    private val table: Map<String, String>,
) {
    private val used = mutableSetOf<String>()

    /** Answers whether [key] is exempt. Asking marks the exemption as used. */
    fun covers(key: String): Boolean {
        val sentence = table[key] ?: return false
        used += key
        return sentence.isNotBlank()
    }

    /** How many exemptions were used. */
    val count: Int get() = used.size

    /** Returns one violation per exemption that is blank or that nothing used. */
    fun leftovers(gate: String): List<String> =
        table.entries.mapNotNull { (key, sentence) ->
            when {
                sentence.isBlank() -> "$gate exemption \"$key\" carries no sentence saying why."
                key !in used -> "$gate exemption \"$key\" exempts nothing. Remove it."
                else -> null
            }
        }
}
