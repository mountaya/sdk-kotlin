package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometry
import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon
import com.mountaya.client.generated.RestSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RoadAccessFeaturesTest {
    private val positions = listOf(listOf(6.0, 45.0), listOf(6.01, 45.0), listOf(6.02, 45.0), listOf(6.03, 45.0))

    private val line: GeoJsonGeometry = GeoJsonLineString(positions)

    private fun stretch(
        from: Int,
        to: Int,
        vararg values: String,
    ) = RestSchema.RoadAccessInfo(from, to, values.map(RestSchema::RoadAccessRestrictionType))

    private fun piece(
        coordinates: List<List<Double>>,
        kind: RoadAccessKind,
    ) = GeoJsonFeature(GeoJsonLineString(coordinates), JsonObject(mapOf("kind" to JsonPrimitive(kind.id))))

    private fun at(
        longitude: Double,
        latitude: Double,
    ) = listOf(longitude, latitude)

    @Test
    fun eachStretchIsCutBetweenItsTwoIndexesBothIncludedWithItsKind() {
        assertEquals(
            listOf(
                piece(listOf(at(6.0, 45.0), at(6.01, 45.0)), RoadAccessKind.Private),
                piece(listOf(at(6.02, 45.0), at(6.03, 45.0)), RoadAccessKind.LocalAccess),
            ),
            roadAccessFeatures(line, listOf(stretch(0, 1, "private"), stretch(2, 3, "destination", "permissive"))),
        )
        assertEquals(
            listOf(piece(positions, RoadAccessKind.Forbidden)),
            roadAccessFeatures(line, listOf(stretch(0, 3, "no"))),
        )
    }

    @Test
    fun aStretchAcrossTwoLinesIsCutWhereOneEndsNeverAcrossTheGap() {
        val lines =
            GeoJsonMultiLineString(
                listOf(
                    listOf(at(6.0, 45.0), at(6.01, 45.0), at(6.02, 45.0)),
                    listOf(at(7.0, 46.0), at(7.01, 46.0), at(7.02, 46.0)),
                ),
            )
        assertEquals(
            listOf(
                piece(listOf(at(6.01, 45.0), at(6.02, 45.0)), RoadAccessKind.Forbidden),
                piece(listOf(at(7.0, 46.0), at(7.01, 46.0)), RoadAccessKind.Forbidden),
            ),
            roadAccessFeatures(lines, listOf(stretch(1, 4, "no"))),
        )
    }

    @Test
    fun anIndexBeforeTheFirstPositionOrPastTheLastIsClampedToTheGeometry() {
        assertEquals(
            listOf(piece(listOf(at(6.02, 45.0), at(6.03, 45.0)), RoadAccessKind.Permit)),
            roadAccessFeatures(line, listOf(stretch(2, 9, "permit"))),
        )
        assertEquals(
            listOf(piece(positions, RoadAccessKind.Forbidden)),
            roadAccessFeatures(line, listOf(stretch(-4, 99, "no"))),
        )
        // Indexes at the ends of an `Int`'s range are clamped as any other, with no overflow.
        assertEquals(
            listOf(piece(positions, RoadAccessKind.Forbidden)),
            roadAccessFeatures(line, listOf(stretch(Int.MIN_VALUE, Int.MAX_VALUE, "no"))),
        )
    }

    @Test
    fun aPieceOfFewerThanTwoPositionsIsLeftOut() {
        assertTrue(
            roadAccessFeatures(
                line,
                listOf(
                    stretch(2, 2, "private"),
                    stretch(3, 9, "private"),
                    stretch(7, 9, "private"),
                    stretch(3, 1, "private"),
                    stretch(Int.MAX_VALUE, Int.MIN_VALUE, "private"),
                ),
            ).isEmpty(),
        )
        // Positions 0 to 2 are the first line, the vias of the MultiPoint count for nothing, 3 and 4 are
        // the second line and 5 to 7 the third: the piece on the third keeps one position.
        val collection =
            GeoJsonGeometryCollection(
                listOf(
                    GeoJsonLineString(listOf(at(6.0, 45.0), at(6.01, 45.0), at(6.02, 45.0))),
                    GeoJsonMultiPoint(listOf(at(6.5, 45.5))),
                    GeoJsonMultiLineString(
                        listOf(
                            listOf(at(7.0, 46.0), at(7.01, 46.0)),
                            listOf(at(8.0, 47.0), at(8.01, 47.0), at(8.02, 47.0)),
                        ),
                    ),
                ),
            )
        assertEquals(
            listOf(
                piece(listOf(at(6.01, 45.0), at(6.02, 45.0)), RoadAccessKind.Service),
                piece(listOf(at(7.0, 46.0), at(7.01, 46.0)), RoadAccessKind.Service),
            ),
            roadAccessFeatures(collection, listOf(stretch(1, 5, "service"))),
        )
    }

    @Test
    fun aStretchOfNoKindIsLeftOutAndAValueTheTableDoesNotNameReadsAsRestricted() {
        assertEquals(
            listOf(piece(listOf(at(6.0, 45.0), at(6.01, 45.0)), RoadAccessKind.LocalAccess)),
            roadAccessFeatures(line, listOf(stretch(0, 2, "permissive", "agricultural"), stretch(0, 1, "customers"))),
        )
        assertTrue(roadAccessFeatures(line, listOf(stretch(0, 3, "permissive"))).isEmpty())
        assertTrue(roadAccessFeatures(line, listOf(stretch(0, 3))).isEmpty())
        assertEquals(
            listOf(piece(listOf(at(6.0, 45.0), at(6.01, 45.0), at(6.02, 45.0)), RoadAccessKind.Restricted)),
            roadAccessFeatures(line, listOf(stretch(0, 2, "tolled", "forestry"))),
        )
        // A value in the Data API's spelling reads as REST's.
        assertEquals(
            listOf(piece(listOf(at(6.0, 45.0), at(6.01, 45.0)), RoadAccessKind.Private)),
            roadAccessFeatures(line, listOf(stretch(0, 1, "PRIVATE"))),
        )
    }

    @Test
    fun noStretchAndAGeometryHoldingNoLineCutNothing() {
        assertTrue(roadAccessFeatures(line, null).isEmpty())
        assertTrue(roadAccessFeatures(line, emptyList()).isEmpty())
        assertTrue(roadAccessFeatures(null, listOf(stretch(0, 3, "no"))).isEmpty())
        assertTrue(roadAccessFeatures(GeoJsonPoint(at(6.0, 45.0)), listOf(stretch(0, 3, "no"))).isEmpty())
        val polygon = GeoJsonPolygon(listOf(listOf(at(6.0, 45.0), at(6.01, 45.0), at(6.01, 45.01), at(6.0, 45.0))))
        assertTrue(roadAccessFeatures(polygon, listOf(stretch(0, 3, "no"))).isEmpty())
    }

    @Test
    fun aCollectionNestedInAnotherAndALineOfNoPositionAreLaidEndToEnd() {
        val nested =
            GeoJsonGeometryCollection(
                listOf(
                    GeoJsonMultiLineString(listOf(emptyList(), listOf(at(6.0, 45.0), at(6.01, 45.0)))),
                    GeoJsonGeometryCollection(listOf(GeoJsonLineString(listOf(at(7.0, 46.0), at(7.01, 46.0))))),
                ),
            )
        assertEquals(
            listOf(
                piece(listOf(at(6.0, 45.0), at(6.01, 45.0)), RoadAccessKind.Forbidden),
                piece(listOf(at(7.0, 46.0), at(7.01, 46.0)), RoadAccessKind.Forbidden),
            ),
            roadAccessFeatures(nested, listOf(stretch(0, 3, "no"))),
        )
        assertTrue(roadAccessFeatures(nested, listOf(stretch(1, 2, "no"))).isEmpty())
    }

    @Test
    fun eachPositionIsKeptAsTheGeometryWritesItAnAltitudeOrANumberOfNone() {
        val written =
            GeoJsonLineString(listOf(listOf(6.0, 45.0, 1200.0), listOf(Double.NaN, 45.0), listOf(6.02, 45.0, 1250.0)))
        assertEquals(
            listOf(piece(written.coordinates, RoadAccessKind.Forbidden)),
            roadAccessFeatures(written, listOf(stretch(0, 2, "no"))),
        )
    }
}
