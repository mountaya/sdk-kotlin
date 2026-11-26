package com.mountaya.client

/**
 * ExchangeResponse is the answer to one attempt: its status and headers as soon as they have
 * arrived, and its body as it is read.
 */
public class ExchangeResponse(
    /** The HTTP status. */
    public val status: Int,
    /**
     * Every header of the answer. A header that arrived more than once holds its values joined by
     * a comma and a space, as HTTP reads it.
     */
    public val headers: Map<String, String> = emptyMap(),
    /** The answer's body, read as it arrives. */
    public val body: ExchangeBody = ExchangeBody(),
) {
    /** Returns the value of the header named [name], read in any letter case, or null where the answer sent none. */
    public fun header(name: String): String? = HeaderLookup.value(name, headers)

    override fun toString(): String = "ExchangeResponse($status)"
}
