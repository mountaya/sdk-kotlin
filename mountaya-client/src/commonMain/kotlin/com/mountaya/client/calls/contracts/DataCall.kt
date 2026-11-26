package com.mountaya.client

/**
 * DataCall is one Data API call, written out by a printed method and handed to the
 * [OperationSender].
 *
 * The selection is already checked against the operation's field tree and printed, so a field
 * outside the tree failed at the call before this existed.
 *
 * It is `InternalMountayaApi`, as the [OperationSender] that takes it is: a sender of another module implements
 * that seam whole, and no consumer reads it (SPEC.md section 7).
 */
@InternalMountayaApi
public class DataCall(
    /** What the contract declares about the operation. */
    public val facts: OperationFacts,
    /** The operation, with its field tree and its full document. */
    public val operation: DataOperation,
    /** The document this call sends: the full one where the caller narrowed nothing. */
    public val document: String,
    /**
     * The operation's one GraphQL variable, `input`, already written by the client's `Json` straight
     * to the text sent, never built as a tree of JSON first.
     */
    public val input: String,
    /** What the call overrides of its client's configuration. */
    public val options: RequestOptions,
) {
    override fun equals(other: Any?): Boolean =
        other is DataCall && other.facts === facts && other.operation == operation && other.document == document &&
            other.input == input && other.options == options

    override fun hashCode(): Int = (operation.hashCode() * 31 + document.hashCode()) * 31 + input.hashCode()

    override fun toString(): String = "DataCall(${operation.name}, input=$input)"
}
