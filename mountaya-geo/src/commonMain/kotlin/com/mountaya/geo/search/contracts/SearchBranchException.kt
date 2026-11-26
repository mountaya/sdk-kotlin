package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/**
 * SearchBranchException is one branch of a search round the Data API answered as failed: the answer arrived, and
 * its `errors` name the branch (`geo.search.live-round`). The API answers the branches that worked beside it, so
 * only that branch's section reads the failure.
 *
 * [code] is why the branch failed, the code querying that target alone would have raised, and [message] the API's
 * sentence for it, in the language the request asked for, each null where the answer states none. Two are equal
 * where they name the same branch and say the same.
 */
public class SearchBranchException(
    /** The branch that failed. */
    public val target: DataSchema.SearchTarget,
    /** Why the branch failed, or null where the answer states no code. */
    public val code: DataSchema.ErrorCode? = null,
    message: String? = null,
) : Exception(message) {
    override fun equals(other: Any?): Boolean =
        other is SearchBranchException && other.target == target && other.code == code && other.message == message

    override fun hashCode(): Int = (target.hashCode() * 31 + code.hashCode()) * 31 + message.hashCode()

    override fun toString(): String = "SearchBranchException(target=$target, code=$code, message=$message)"
}
