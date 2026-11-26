package com.mountaya.client

/**
 * Pagination is how an operation that pages by cursor sorts its pages.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, prints its own from beside the core, and
 * no consumer reads it (SPEC.md section 7).
 */
@InternalMountayaApi
public class Pagination(
    /** Every order its `sort` parameter takes, in the contract's order, and empty where it takes no `sort`. */
    public val sort: List<String>,
    /** The order it sorts by where the caller names none, and null where it takes no `sort`. */
    public val defaultSort: String?,
) {
    override fun equals(other: Any?): Boolean =
        other is Pagination && other.sort == sort && other.defaultSort == defaultSort

    override fun hashCode(): Int = sort.hashCode() * 31 + (defaultSort?.hashCode() ?: 0)

    override fun toString(): String = "Pagination(sort=$sort, defaultSort=$defaultSort)"
}
