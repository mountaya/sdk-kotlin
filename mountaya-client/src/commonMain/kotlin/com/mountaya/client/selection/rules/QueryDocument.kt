package com.mountaya.client

/**
 * QueryDocument prints the GraphQL document of a Data API operation for a selection.
 *
 * The bytes are contract, since the persisted-query digest is taken over them: two spaces to a
 * level and one field to a line, the operation named after its field with the first letter
 * upper-cased, and a single newline at the end.
 */
internal object QueryDocument {
    /**
     * Returns the document that selects [selection] from the operation [name], whose `input`
     * argument has the GraphQL type [inputType].
     */
    fun print(
        name: String,
        inputType: String,
        selection: FieldSelection,
    ): String =
        buildString {
            append("query ").append(name.replaceFirstChar(Char::uppercaseChar))
            append("(\$input: ").append(inputType).append(") {\n")
            append("  ").append(name).append("(input: \$input) {\n")
            write(selection, depth = 2)
            append("  }\n}\n")
        }

    private fun StringBuilder.write(
        selection: FieldSelection,
        depth: Int,
    ) {
        val indent = "  ".repeat(depth)
        for (field in selection.fields) {
            val beneath = field.beneath
            if (beneath == null) {
                append(indent).append(field.name).append('\n')
            } else {
                append(indent).append(field.name).append(" {\n")
                write(beneath, depth + 1)
                append(indent).append("}\n")
            }
        }
    }
}
