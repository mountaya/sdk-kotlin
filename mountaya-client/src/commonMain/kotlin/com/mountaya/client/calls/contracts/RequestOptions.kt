package com.mountaya.client

import kotlin.time.Duration

/**
 * RequestOptions is what one call overrides of its client's configuration.
 *
 * Every operation takes one as `options`, so an override the client gains later is one more
 * property here and changes no call site.
 */
public class RequestOptions(
    /** The retry policy for this call, its wait budget among it, or null where the client's own applies. */
    public val retry: RetryPolicy? = null,
    /**
     * The call's own budget for each of its attempts, or null where the call takes the larger of
     * its client's and the operation's own plus a margin. A budget the call names is the
     * attempt's, shorter or longer than the API's, and one of zero or less is refused with
     * `ConfigurationException` before any request.
     */
    public val timeout: Duration? = null,
) {
    override fun equals(other: Any?): Boolean =
        other is RequestOptions && other.retry == retry && other.timeout == timeout

    override fun hashCode(): Int = (retry?.hashCode() ?: 0) * 31 + (timeout?.hashCode() ?: 0)

    override fun toString(): String = "RequestOptions(retry=$retry, timeout=$timeout)"
}
