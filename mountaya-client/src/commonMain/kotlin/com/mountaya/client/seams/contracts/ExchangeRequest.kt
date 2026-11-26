package com.mountaya.client

/**
 * ExchangeRequest is one attempt as it leaves the client: every header it carries is already
 * written, the credential's among them.
 *
 * It prints its method and its address alone, since its headers carry the credential.
 */
public class ExchangeRequest(
    /** The HTTP method, uppercase. */
    public val method: String,
    /** The whole address, the query included. */
    public val url: Url,
    /** Every header the attempt carries, each name in the spelling the client writes. */
    public val headers: Map<String, String> = emptyMap(),
    /** What the attempt carries, or null where it carries nothing. */
    public val body: ByteArray? = null,
) {
    /** Returns the value of the header named [name], read in any letter case, or null where the attempt carries none. */
    public fun header(name: String): String? = HeaderLookup.value(name, headers)

    override fun equals(other: Any?): Boolean =
        other is ExchangeRequest && other.method == method && other.url == url && other.headers == headers &&
            (other.body?.contentEquals(body) ?: (body == null))

    override fun hashCode(): Int =
        ((method.hashCode() * 31 + url.hashCode()) * 31 + headers.hashCode()) * 31 + (body?.contentHashCode() ?: 0)

    override fun toString(): String = "ExchangeRequest($method $url)"
}
