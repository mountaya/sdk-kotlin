package com.mountaya.client

import kotlin.time.Duration.Companion.seconds

/**
 * RateLimitHeaders reads the limits an answer was counted against from its `RateLimit-Policy`
 * and `RateLimit` fields, as the IETF's RateLimit header fields for HTTP state them.
 */
internal object RateLimitHeaders {
    /**
     * Returns one policy per String member of [policy], in its order, each with the remaining
     * figure and reset of the first member of [remaining] of the same name, or null where no
     * policy is left.
     *
     * A member that is not a String, or whose `q` is not a whole number of zero or more, is left
     * out, and so is a member of [remaining] naming no policy, since its quota is unknown. A policy
     * named twice is kept twice. A parameter this reader does not know is ignored, and a figure
     * that is not a whole number of zero or more is one not stated. A field that does not parse is
     * ignored whole: no policy where it is [policy], and no figure where it is [remaining].
     */
    fun state(
        policy: String?,
        remaining: String?,
    ): RateLimitState? {
        val members = policy?.let { StructuredFieldList.parse(it) } ?: return null
        val figures = remaining?.let { StructuredFieldList.parse(it) }.orEmpty()
        val policies =
            members.mapNotNull { member ->
                val single = member as? StructuredFieldList.Member.Single ?: return@mapNotNull null
                val name = (single.item as? StructuredFieldList.Item.Text)?.value ?: return@mapNotNull null
                val quota = wholeNumber(single.parameters, "q") ?: return@mapNotNull null
                val state =
                    figures.firstNotNullOfOrNull { figure ->
                        (figure as? StructuredFieldList.Member.Single)
                            ?.takeIf { (it.item as? StructuredFieldList.Item.Text)?.value == name }
                            ?.parameters
                    }
                RateLimitPolicy(
                    name = name,
                    quota = quota,
                    window = wholeNumber(single.parameters, "w")?.seconds,
                    remaining = state?.let { wholeNumber(it, "r") },
                    reset = state?.let { wholeNumber(it, "t") }?.seconds,
                )
            }
        return if (policies.isEmpty()) null else RateLimitState(policies)
    }

    /** The parameter named [key] where it is an integer of zero or more. */
    private fun wholeNumber(
        parameters: List<StructuredFieldList.Parameter>,
        key: String,
    ): Long? {
        val value = (parameters.firstOrNull { it.key == key }?.value as? StructuredFieldList.Item.Integer)?.value
        return value?.takeIf { it >= 0 }
    }
}
