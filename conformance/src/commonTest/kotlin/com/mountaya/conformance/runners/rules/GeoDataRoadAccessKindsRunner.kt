package com.mountaya.conformance

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.RoadAccessKind
import com.mountaya.geo.roadAccessKindOf
import com.mountaya.geo.roadAccessValueKinds
import kotlinx.coroutines.test.TestScope

/**
 * `geo.data.road-access-kinds`: the kind a stretch of the case's `values` is shown as, or null.
 *
 * The kinds, most severe first, and the kind each value names are held to the ones Geo publishes,
 * and the kind of a value the table does not name, which Geo keeps to its rule, to what the rule
 * answers for one. A case's values are asked through both surfaces' types, REST's and the Data API's,
 * since a value is read in either spelling whichever type it arrives in, and the two must agree.
 */
object GeoDataRoadAccessKindsRunner : ConformanceRunner {
    /** A value no access tag spells, which the table must not name. */
    private const val UNNAMED_VALUE = "not-an-access-value"

    override suspend fun run(
        scope: TestScope,
        case: ConformanceCase,
    ): ConformanceOutcome {
        val unnamed = RestSchema.RoadAccessRestrictionType(UNNAMED_VALUE)
        if (unnamed in roadAccessValueKinds) throw AssertionError("the table names `$UNNAMED_VALUE`")
        holdDeclaredConstants(
            case,
            rules = emptyMap(),
            values =
                mapOf(
                    "kinds" to textArray(RoadAccessKind.entries.map { it.id }),
                    "valueKinds" to
                        FixtureValue.Object(
                            roadAccessValueKinds.map { (value, kind) -> value.value to textOrNull(kind?.id) },
                        ),
                    "unknownValueKind" to textOrNull(roadAccessKindOf(listOf(unnamed))?.id),
                ),
        )
        val values = fixtureTexts(case.fields.items("values"), "`values`")
        val rest = roadAccessKindOf(values.map(RestSchema::RoadAccessRestrictionType))
        val data = roadAccessKindOf(values.map(DataSchema::RoadAccessRestrictionType))
        if (rest != data) {
            throw AssertionError("REST's values read as ${rest?.id}, and the Data API's as ${data?.id}")
        }
        return ConformanceOutcome.Value(textOrNull(rest?.id))
    }
}
