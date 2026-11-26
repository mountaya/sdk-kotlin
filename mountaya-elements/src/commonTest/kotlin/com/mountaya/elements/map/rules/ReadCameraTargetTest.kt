package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadCameraTargetTest {
    @Test
    fun aTargetIsAnsweredAsStatedACentrePastTheAntimeridianAndAPoleIncluded() {
        for (target in listOf(
            CameraTarget(center = listOf(6.86, 45.83), zoom = 12.0),
            CameraTarget(center = listOf(197.5, 0.0), pitch = 0.0),
            CameraTarget(center = listOf(-179.9, -16.5), zoom = 9.5, bearing = -30.0, pitch = 60.0),
            CameraTarget(zoom = 0.0, bearing = 0.0, pitch = 0.0),
            CameraTarget(),
            CameraTarget(center = listOf(0.0, 90.0)),
            CameraTarget(center = listOf(0.0, -90.0)),
        )) {
            assertEquals(target, readCameraTarget(target))
        }
    }

    @Test
    fun aCentreThatNamesNoPlaceIsRefusedAsAPositionNamingTheCentre() {
        for (center in listOf(
            listOf(6.0, 90.0001),
            listOf(6.86, 45.83, 1035.0),
            listOf(Double.NaN, 45.83),
            listOf(6.0),
            emptyList(),
            listOf(Double.POSITIVE_INFINITY, 0.0),
        )) {
            assertEquals(
                MapException(MapException.Kind.PositionRefused, "center"),
                assertFailsWith<MapException> { readCameraTarget(CameraTarget(center = center, zoom = Double.NaN)) },
                "$center",
            )
        }
    }

    @Test
    fun aZoomABearingOrAPitchThatIsNoFiniteNumberIsRefusedNamingIt() {
        assertEquals(
            MapException(MapException.Kind.NumberRefused, "zoom"),
            assertFailsWith<MapException> {
                readCameraTarget(CameraTarget(center = listOf(6.86, 45.83), zoom = Double.NEGATIVE_INFINITY))
            },
        )
        assertEquals(
            MapException(MapException.Kind.NumberRefused, "bearing"),
            assertFailsWith<MapException> {
                readCameraTarget(CameraTarget(bearing = Double.POSITIVE_INFINITY, pitch = Double.NaN))
            },
        )
        assertEquals(
            MapException(MapException.Kind.NumberRefused, "pitch"),
            assertFailsWith<MapException> { readCameraTarget(CameraTarget(pitch = Double.NaN)) },
        )
    }
}
