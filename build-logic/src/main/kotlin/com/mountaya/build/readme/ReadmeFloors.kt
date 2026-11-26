package com.mountaya.build.readme

/**
 * ReadmeFloors holds the README's floors table to the version catalog (SPEC section 2). The table, the one [HEADER]
 * opens, is the README's one prose copy of each floor a consumer's build meets: each floor is stated on one of its rows,
 * in a form [Floors] reads, at the catalog's figure, and on no line outside it, so a floor's number is written in the
 * catalog and copied once, where the gate reads it.
 */
object ReadmeFloors {
    /** The header row that opens the floors table. */
    const val HEADER = "| Floor | Version |"

    /**
     * Returns what [markdown], the README shown as [document], states of the build's [floors], each keyed by its
     * catalog key and valued by the catalog's figure, that the catalog does not: no floors table or more than one, a
     * floor the table states nowhere or more than once, a statement naming another figure, and a statement outside the
     * table.
     */
    fun problems(
        document: String,
        markdown: String,
        floors: Map<String, String>,
    ): List<String> {
        val values =
            Floors.all.associateWith { floor ->
                floors[floor.key]
                    ?: throw IllegalArgumentException("The build hands no `${floor.key}`, the floor of ${floor.what}.")
            }
        val lines = markdown.split("\n")
        val headers = lines.indices.filter { lines[it].trim() == HEADER }.map { it + 1 }
        if (headers.isEmpty()) {
            val rows = values.entries.joinToString(", ") { (floor, value) -> "`${floor.example.format(value)}`" }
            return listOf(
                "$document holds no floors table, the `$HEADER` table that states each floor a consumer's build " +
                    "meets once, as the version catalog holds it: $rows.",
            )
        }
        if (headers.size > 1) {
            val at = headers.joinToString(" and ")
            return listOf(
                "$document holds ${headers.size} floors tables, at lines $at: one table states each floor once.",
            )
        }
        // Lines are counted from 1: the table runs from its header to the last row before a line that is no row.
        val header = headers.single()
        var last = header
        while (last < lines.size && lines[last].trimStart().startsWith("|")) last += 1
        val table = header..last
        val problems = mutableListOf<String>()
        for ((floor, value) in values) {
            val statements = Floors.statements(floor, markdown).map { Floors.lineOf(markdown, it.offset) to it }
            val (rows, outside) = statements.partition { (line, _) -> line in table }
            for ((line, statement) in outside) {
                problems +=
                    "$document, line $line: `${statement.words}` states ${floor.what} outside the floors table at " +
                    "line $header, its one copy in $document: state it on the table's row alone."
            }
            if (rows.isEmpty()) {
                problems +=
                    "$document's floors table states nowhere ${floor.what}: the version catalog's `${floor.key}` " +
                    "is $value, which a row states as `${floor.example.format(value)}`."
            }
            if (rows.size > 1) {
                problems +=
                    "$document's floors table states ${floor.what} ${rows.size} times, at lines " +
                    "${rows.joinToString(" and ") { (line, _) -> "$line" }}: one row states it."
            }
            for ((line, statement) in rows.filter { (_, statement) -> statement.figure != value }) {
                problems +=
                    "$document, line $line: `${statement.words}` states ${floor.what} as ${statement.figure}, and " +
                    "the version catalog's `${floor.key}` is $value."
            }
        }
        return problems
    }
}
