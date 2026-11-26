package com.mountaya.client

/**
 * RateLimitState is the limits an answer says it was counted against, as its `RateLimit-Policy`
 * and `RateLimit` fields describe them.
 *
 * It is reported, never counted against: the client delays no request on what it says, since an
 * organization's figures are its agreement's and the API is what holds them.
 */
public class RateLimitState(
    /** One policy per String member of `RateLimit-Policy`, in the order the field lists them. */
    public val policies: List<RateLimitPolicy>,
) {
    override fun equals(other: Any?): Boolean = other is RateLimitState && other.policies == policies

    override fun hashCode(): Int = policies.hashCode()

    override fun toString(): String = "RateLimitState($policies)"
}
