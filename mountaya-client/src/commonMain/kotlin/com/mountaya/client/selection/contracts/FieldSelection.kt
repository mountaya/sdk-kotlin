package com.mountaya.client

/**
 * FieldSelection is a set of fields of a Data API result, each selected whole or with the fields
 * selected beneath it.
 *
 * It is built with [FieldSelection], `field(name)` selecting a field whole:
 *
 * ```
 * val selection = FieldSelection {
 *     field("routes") {
 *         field("distance")
 *         field("analysis") { field("elevations") }
 *     }
 * }
 * ```
 *
 * The order a selection is written in decides nothing: the document is printed in the order of
 * the operation's field tree, so two selections of the same fields send the same bytes.
 */
public class FieldSelection internal constructor(
    /** The fields selected, in the order they were given. */
    internal val fields: List<Field>,
) {
    /** Field is one selected field, and what is selected beneath it, or null where it is selected whole. */
    internal class Field(
        val name: String,
        val beneath: FieldSelection?,
    ) {
        override fun equals(other: Any?): Boolean = other is Field && other.name == name && other.beneath == beneath

        override fun hashCode(): Int = name.hashCode() * 31 + (beneath?.hashCode() ?: 0)
    }

    /**
     * Builder collects the fields of one level of a selection. A field named twice takes the
     * selection given last.
     */
    public class Builder internal constructor() {
        private val fields = mutableListOf<Field>()

        /**
         * Selects the field [name] whole: a field with no fields beneath it, or an object with every field
         * beneath it, its whole subtree.
         */
        public fun field(name: String) {
            add(Field(name, null))
        }

        /** Selects the field [name] with the fields [beneath] selects under it. */
        public fun field(
            name: String,
            beneath: Builder.() -> Unit,
        ) {
            add(Field(name, Builder().apply(beneath).build()))
        }

        internal fun build(): FieldSelection = FieldSelection(fields.toList())

        private fun add(field: Field) {
            fields.removeAll { it.name == field.name }
            fields += field
        }
    }

    override fun equals(other: Any?): Boolean = other is FieldSelection && other.fields == fields

    override fun hashCode(): Int = fields.hashCode()

    override fun toString(): String =
        fields.joinToString(", ", "{", "}") { field -> field.beneath?.let { "${field.name} $it" } ?: field.name }
}

/** FieldSelection builds the selection [fields] states. */
public fun FieldSelection(fields: FieldSelection.Builder.() -> Unit): FieldSelection =
    FieldSelection.Builder().apply(fields).build()
