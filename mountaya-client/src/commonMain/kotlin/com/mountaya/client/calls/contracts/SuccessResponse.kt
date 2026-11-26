package com.mountaya.client

/**
 * SuccessResponse is one status an operation's success arrives with, and the headers the
 * contract declares that status carries, by name, in the model's order.
 *
 * It decides nothing at request time: an answer's `X-Request-Id`, `Location` and `ETag` are read
 * wherever they are sent. It is printed so the facts gate holds what a success declares to the
 * model, as it holds every other fact.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, prints its own from beside the core, and
 * no consumer reads it (SPEC.md section 7).
 */
@InternalMountayaApi
public class SuccessResponse(
    /** The HTTP status. */
    public val status: Int,
    /** The name of every header the status declares, in the model's order. */
    public val headers: List<String> = emptyList(),
) {
    override fun equals(other: Any?): Boolean =
        other is SuccessResponse && other.status == status && other.headers == headers

    override fun hashCode(): Int = status * 31 + headers.hashCode()

    override fun toString(): String = "SuccessResponse($status, headers=$headers)"
}
