package com.mountaya.client

import kotlin.time.Duration

/**
 * DataOperation is one Data API operation as the contract declares it: its name, the type of its
 * input, the tree of every field its result can carry, and the most the API spends on it.
 *
 * Every printed method of `data` carries its own; a caller writes one, from the contract, to call
 * an operation this build prints no method for, `Mountaya.query`. Two are equal where they state
 * the same.
 */
public class DataOperation(
    /** The operation's field on the Data API's query type, such as `directions`. */
    public val name: String,
    /** The GraphQL type of its `input` argument, such as `DirectionsInput!`. */
    public val inputType: String,
    /**
     * The operation's field tree: every field of its result, in the schema's order, which is also
     * the full selection.
     */
    public val selection: FieldSelection,
    /**
     * The most the API spends on the operation before it answers a 504 itself, its
     * `x-timeout-ms`, from which an attempt's budget is derived, or null where the caller states
     * none: the client's budget then holds alone.
     */
    public val timeout: Duration? = null,
) {
    /** The GraphQL document of the full selection, which is what a call that narrows nothing sends. */
    public val document: String = QueryDocument.print(name, inputType, selection)

    /**
     * Returns the GraphQL document a call sends: the full [document] where [selecting] is null, and
     * otherwise the document of the fields it names, in the field tree's order.
     *
     * It throws [SelectionException] for a selection naming a field outside the field tree, rather
     * than sending a request the server would refuse.
     */
    public fun document(selecting: FieldSelection?): String {
        if (selecting == null) return document
        return QueryDocument.print(name, inputType, SelectionNarrowing.narrow(selection, selecting))
    }

    override fun equals(other: Any?): Boolean =
        other is DataOperation && other.name == name && other.inputType == inputType &&
            other.selection == selection && other.timeout == timeout

    override fun hashCode(): Int =
        ((name.hashCode() * 31 + inputType.hashCode()) * 31 + selection.hashCode()) * 31 + timeout.hashCode()

    override fun toString(): String = "DataOperation($name)"
}
