package com.mountaya.geo

import com.mountaya.client.GeoJsonGeometryCollection
import com.mountaya.client.GeoJsonLineString
import com.mountaya.client.GeoJsonMultiLineString
import com.mountaya.client.GeoJsonMultiPoint
import com.mountaya.client.GeoJsonMultiPolygon
import com.mountaya.client.GeoJsonPoint
import com.mountaya.client.GeoJsonPolygon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BoundsOfTest {
    private fun line(vararg positions: List<Double>) = GeoJsonLineString(positions.toList())

    @Test
    fun theBoxCoversEveryPositionOfEveryGeometry() {
        assertEquals(
            listOf(6.86, 45.8, 6.9, 45.92),
            boundsOf(listOf(line(listOf(6.86, 45.85), listOf(6.9, 45.92), listOf(6.88, 45.8)))),
        )
        assertEquals(
            listOf(5.0, 44.0, 8.0, 47.0),
            boundsOf(
                listOf(
                    GeoJsonPoint(listOf(5.0, 45.0)),
                    GeoJsonMultiLineString(listOf(listOf(listOf(6.0, 44.0)), listOf(listOf(7.0, 46.0)))),
                    GeoJsonPolygon(
                        listOf(listOf(listOf(6.0, 45.0), listOf(8.0, 45.0), listOf(6.0, 47.0), listOf(6.0, 45.0))),
                    ),
                ),
            ),
        )
        assertEquals(
            listOf(1.0, 2.0, 3.0, 4.0),
            boundsOf(listOf(GeoJsonMultiPolygon(listOf(listOf(listOf(listOf(1.0, 2.0), listOf(3.0, 4.0))))))),
        )
    }

    /** A directions segment's collection holds its line and a MultiPoint of its vias, at any depth. */
    @Test
    fun aCollectionOfAnyDepthIsFlattenedFirst() {
        val nested =
            GeoJsonGeometryCollection(
                listOf(
                    line(listOf(6.86, 45.85), listOf(6.9, 45.92)),
                    GeoJsonGeometryCollection(listOf(GeoJsonMultiPoint(listOf(listOf(6.95, 45.7))))),
                ),
            )

        assertEquals(listOf(6.86, 45.7, 6.95, 45.92), boundsOf(listOf(nested)))
    }

    @Test
    fun aSetStraddlingTheAntimeridianIsFramedAcrossIt() {
        assertEquals(
            listOf(179.5, -17.0, 180.5, -16.0),
            boundsOf(listOf(line(listOf(179.5, -16.0), listOf(-179.5, -17.0)))),
        )
        val east =
            GeoJsonPolygon(
                listOf(listOf(listOf(179.0, 1.0), listOf(180.0, 1.0), listOf(180.0, 2.0), listOf(179.0, 1.0))),
            )
        val west =
            GeoJsonPolygon(
                listOf(listOf(listOf(-180.0, 1.0), listOf(-179.0, 1.0), listOf(-180.0, 2.0), listOf(-180.0, 1.0))),
            )
        assertEquals(listOf(179.0, 1.0, 181.0, 2.0), boundsOf(listOf(east, west)))
    }

    /**
     * Positions alone cannot tell such a set from one drawn wider than half the world across the
     * prime meridian, which reads the narrow way too.
     */
    @Test
    fun aSetWiderThanHalfTheWorldReadsTheNarrowWay() {
        assertEquals(listOf(170.0, 0.0, 190.0, 1.0), boundsOf(listOf(line(listOf(-170.0, 0.0), listOf(170.0, 1.0)))))
        assertEquals(listOf(100.0, 0.0, 260.0, 1.0), boundsOf(listOf(line(listOf(-100.0, 0.0), listOf(100.0, 1.0)))))
        // Half a turn or less keeps its longitudes as written.
        assertEquals(listOf(-90.0, 0.0, 90.0, 1.0), boundsOf(listOf(line(listOf(-90.0, 0.0), listOf(90.0, 1.0)))))
    }

    @Test
    fun aSetOnTheAntimeridianKeepsItsWrittenWidth() {
        assertEquals(
            listOf(-180.0, -10.0, 180.0, 10.0),
            boundsOf(listOf(line(listOf(180.0, -10.0), listOf(-180.0, 10.0)))),
        )
        assertEquals(
            listOf(180.0, -10.0, 180.0, 10.0),
            boundsOf(listOf(line(listOf(180.0, -10.0), listOf(180.0, 10.0)))),
        )
    }

    /** A set written past the range keeps it; a turned frame past 180 is brought back by a turn. */
    @Test
    fun aSetWrittenPastTheRangeIsReadAsWritten() {
        assertEquals(listOf(190.0, 0.0, 200.0, 1.0), boundsOf(listOf(line(listOf(190.0, 0.0), listOf(200.0, 1.0)))))
        assertEquals(listOf(-170.0, 0.0, -170.0, 1.0), boundsOf(listOf(line(listOf(190.0, 0.0), listOf(-170.0, 1.0)))))
    }

    @Test
    fun aPositionThatNamesNoPlaceBoundsNothing() {
        assertEquals(
            listOf(6.86, 45.8, 6.88, 45.85),
            boundsOf(
                listOf(line(listOf(6.86, 45.85), listOf(6.9), listOf(Double.NaN, 50.0), listOf(6.88, 45.8, 3000.0))),
            ),
        )
        assertNull(boundsOf(listOf(line(listOf(6.9), listOf(6.0, Double.POSITIVE_INFINITY)))))
        assertNull(boundsOf(emptyList()))
        assertNull(boundsOf(listOf(GeoJsonGeometryCollection(emptyList()))))
    }
}
