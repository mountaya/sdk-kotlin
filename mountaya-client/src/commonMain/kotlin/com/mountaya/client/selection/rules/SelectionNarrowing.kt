package com.mountaya.client

/**
 * SelectionNarrowing holds a caller's field selection to an operation's field tree.
 *
 * A selection is checked against the tree rather than against the result type, so a field
 * outside it fails where the caller wrote it instead of as a server error.
 */
internal object SelectionNarrowing {
    /**
     * Returns the part of [tree] that [selection] names, in the tree's order, or throws
     * [SelectionException] naming the first field the tree does not allow. An object selected whole is
     * selected with its whole subtree; a selection naming no field, a block naming none beneath an
     * object, and a block beneath a field that has no fields are refused.
     */
    fun narrow(
        tree: FieldSelection,
        selection: FieldSelection,
    ): FieldSelection = narrow(tree, selection, emptyList())

    private fun narrow(
        tree: FieldSelection,
        selection: FieldSelection,
        path: List<String>,
    ): FieldSelection {
        val selected = selection.fields
        if (selected.isEmpty()) {
            throw SelectionException(
                path,
                if (path.isEmpty()) {
                    "the selection names no field; leave it out to select every field"
                } else {
                    "this field is an object: name the fields to select beneath it"
                },
            )
        }
        val allowed = tree.fields
        for (field in selected) {
            if (allowed.none { it.name == field.name }) {
                val names = allowed.joinToString(", ") { "`${it.name}`" }
                throw SelectionException(path + field.name, "no such field; the fields here are $names")
            }
        }

        val narrowed =
            allowed.mapNotNull { field ->
                val chosen = selected.firstOrNull { it.name == field.name } ?: return@mapNotNull null
                val fieldTree = field.beneath
                val chosenBeneath = chosen.beneath
                when {
                    fieldTree != null && chosenBeneath != null -> {
                        FieldSelection.Field(field.name, narrow(fieldTree, chosenBeneath, path + field.name))
                    }

                    // A field selected whole is selected with every field beneath it, the object's whole subtree.
                    fieldTree != null -> {
                        field
                    }

                    chosenBeneath != null -> {
                        throw SelectionException(
                            path + field.name,
                            "this field has no fields beneath it: select it without a block",
                        )
                    }

                    else -> {
                        field
                    }
                }
            }
        return FieldSelection(narrowed)
    }
}
