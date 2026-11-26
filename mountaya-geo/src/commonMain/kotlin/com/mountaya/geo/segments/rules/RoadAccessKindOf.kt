package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlin.jvm.JvmName

/**
 * roadAccessValueKinds is the kind each access value the contract names is shown as, keyed by the
 * value as REST spells it. A value of no kind maps to null: `permissive`, `agricultural` and
 * `forestry` grant passage or are ordinary terrain on foot, which is information and never a hazard.
 * A value it does not name is shown as [RoadAccessKind.Restricted] by [roadAccessKindOf].
 */
public val roadAccessValueKinds: Map<RestSchema.RoadAccessRestrictionType, RoadAccessKind?> =
    mapOf(
        RestSchema.RoadAccessRestrictionType.No to RoadAccessKind.Forbidden,
        RestSchema.RoadAccessRestrictionType.Private to RoadAccessKind.Private,
        RestSchema.RoadAccessRestrictionType.Permit to RoadAccessKind.Permit,
        RestSchema.RoadAccessRestrictionType.Service to RoadAccessKind.Service,
        RestSchema.RoadAccessRestrictionType.Destination to RoadAccessKind.LocalAccess,
        RestSchema.RoadAccessRestrictionType.Customers to RoadAccessKind.LocalAccess,
        RestSchema.RoadAccessRestrictionType.Delivery to RoadAccessKind.LocalAccess,
        RestSchema.RoadAccessRestrictionType.Restricted to RoadAccessKind.Restricted,
        RestSchema.RoadAccessRestrictionType.Unknown to RoadAccessKind.Restricted,
        RestSchema.RoadAccessRestrictionType.Permissive to null,
        RestSchema.RoadAccessRestrictionType.Agricultural to null,
        RestSchema.RoadAccessRestrictionType.Forestry to null,
    )

/** The kind of a value [roadAccessValueKinds] does not name, so a restriction added later still shows. */
private val unnamedValueKind = RoadAccessKind.Restricted

/**
 * roadAccessKindOf answers the kind a stretch is shown as from the access [values] REST answers for
 * it: the most severe kind among them, whatever their order, or null where no value names one, an
 * empty list included.
 *
 * Each value reads through [roadAccessValueKinds], a value it does not name as
 * [RoadAccessKind.Restricted]. A value is read in either surface's spelling, its ASCII capitals folded
 * and nothing else, never by a locale's case rules: `PRIVATE` is `private`.
 */
public fun roadAccessKindOf(values: List<RestSchema.RoadAccessRestrictionType>): RoadAccessKind? =
    mostSevereKindOf(values) { it.value }

/**
 * roadAccessKindOf answers the kind a stretch is shown as from the access [values] the Data API
 * answers for it, by the rule REST's values are read by: the Data API's `PRIVATE` is REST's `private`.
 */
@JvmName("roadAccessKindOfDataApi")
public fun roadAccessKindOf(values: List<DataSchema.RoadAccessRestrictionType>): RoadAccessKind? =
    mostSevereKindOf(values) { it.value }

/** The most severe kind among [values], each spelled by [spelling], or null where none names one. */
private inline fun <T> mostSevereKindOf(
    values: List<T>,
    spelling: (T) -> String,
): RoadAccessKind? {
    var mostSevere: RoadAccessKind? = null
    for (value in values) {
        val kind = kindOf(spelling(value)) ?: continue
        if (mostSevere == null || kind < mostSevere) mostSevere = kind
    }
    return mostSevere
}

/** The kind [value] names in either surface's spelling, or null where it names none. */
private fun kindOf(value: String): RoadAccessKind? {
    val key = RestSchema.RoadAccessRestrictionType(asciiLowercase(value))
    return if (key in roadAccessValueKinds) roadAccessValueKinds[key] else unnamedValueKind
}
