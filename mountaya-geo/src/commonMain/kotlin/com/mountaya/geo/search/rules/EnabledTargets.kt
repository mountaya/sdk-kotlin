package com.mountaya.geo

import com.mountaya.client.generated.DataSchema

/** Every branch this build can enable, one flag of the Data API's search targets each. */
private val enabledBranches: List<DataSchema.SearchTarget> =
    listOf(
        DataSchema.SearchTarget.Places,
        DataSchema.SearchTarget.Pois,
        DataSchema.SearchTarget.Ways,
        DataSchema.SearchTarget.Itineraries,
        DataSchema.SearchTarget.Collections,
        DataSchema.SearchTarget.Zones,
    )

/**
 * enabledTargets answers the targets of a search request that asks the branch [branch] alone
 * (`geo.search.section-order`): its flag true and every other flag false, each stated, so the request says what it
 * asks whatever the API's defaults.
 *
 * Null for a branch this build cannot enable, one the contract added after it, which no request of it can ask.
 * Every branch is named, so a target the contract adds is one more case here rather than a section that renders and
 * is always empty.
 */
internal fun enabledTargets(branch: DataSchema.SearchTarget): DataSchema.SearchTargetsInput? {
    if (branch !in enabledBranches) return null
    return DataSchema.SearchTargetsInput {
        places = branch == DataSchema.SearchTarget.Places
        pois = branch == DataSchema.SearchTarget.Pois
        ways = branch == DataSchema.SearchTarget.Ways
        itineraries = branch == DataSchema.SearchTarget.Itineraries
        collections = branch == DataSchema.SearchTarget.Collections
        zones = branch == DataSchema.SearchTarget.Zones
    }
}
