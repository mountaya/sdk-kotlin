package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class ReadoutRowsTest {
    private val position = listOf(6.8652, 45.9237)
    private val moment = Instant.fromEpochMilliseconds(1_773_480_360_000)

    private fun kinds(rows: List<ReadoutRow>): List<String?> = rows.map { it::class.simpleName }

    @Test
    fun everythingAtTheDetailZoomStandsInOrder() {
        val rows =
            readoutRows(
                ReadoutPoint(
                    position,
                    14.0,
                    terrain = ReadoutPoint.Terrain(1834.6, 32.4, 135.2),
                    poi = ReadoutPoint.Poi("Refuge du Goûter", "alpine_hut"),
                    zone =
                        ReadoutPoint.Zone(
                            "Couloir",
                            "Rockfall.",
                            RestSchema.ZoneLevel.Warning,
                            Instant.fromEpochSeconds(1),
                            Instant.fromEpochSeconds(2),
                        ),
                    overlay = ReadoutPoint.Overlay("temperature-actual", "Temperature", isVector = true),
                    band = ReadoutPoint.Band(2.0, 5.0),
                    instant = moment,
                ),
            )

        assertEquals(listOf("Poi", "Band", "Terrain", "Coordinates", "Zone"), kinds(rows))
        val poi = assertIs<ReadoutRow.Poi>(rows[0])
        assertEquals(DataSchema.POICategory.Hut, poi.category)
        assertEquals("Hut", poi.label)
        val band = assertIs<ReadoutRow.Band>(rows[1])
        assertEquals(listOf(2.0, 5.0), band.values)
        assertEquals("readout.overlayRange", band.word)
        val terrain = assertIs<ReadoutRow.Terrain>(rows[2])
        assertEquals("#b71c1c", terrain.readings[1].color)
        assertEquals(AspectCardinal.SouthEast, terrain.readings[2].cardinal)
        assertEquals("readout.aspects.SE", terrain.readings[2].cardinalWord)
    }

    @Test
    fun belowTheDetailZoomTheBandAndTheCoordinatesStayAndANoteAsksToZoomIn() {
        val rows =
            readoutRows(
                ReadoutPoint(
                    position,
                    12.99,
                    terrain = ReadoutPoint.Terrain(1834.6, 32.4, 135.2),
                    poi = ReadoutPoint.Poi("Refuge", "alpine_hut"),
                    overlay = ReadoutPoint.Overlay("wind", "Wind", isVector = true),
                    band = ReadoutPoint.Band(25.0, 40.0),
                ),
            )

        assertEquals(listOf("Band", "Coordinates", "ZoomIn"), kinds(rows))
    }

    @Test
    fun aVectorOverlayWithNoBandStatesItsMomentAloneAndARasterOneNone() {
        val vector = ReadoutPoint.Overlay("precipitation", "Precipitation", isVector = true)
        val raster = ReadoutPoint.Overlay("steepness", "Slope", isVector = false)

        assertEquals(
            listOf("Moment", "Coordinates"),
            kinds(readoutRows(ReadoutPoint(position, 14.0, overlay = vector, instant = moment))),
        )
        assertEquals(
            listOf("Coordinates"),
            kinds(readoutRows(ReadoutPoint(position, 14.0, overlay = raster, instant = moment))),
        )
    }

    @Test
    fun aSnowDepthIsScaledToCentimetersAndOneBoundStandsAlone() {
        val depth = ReadoutPoint.Overlay("snowdepth", "Snow depth", isVector = true)
        val band =
            assertIs<ReadoutRow.Band>(
                readoutRows(ReadoutPoint(position, 14.0, overlay = depth, band = ReadoutPoint.Band(0.2, 0.5)))[0],
            )
        val one =
            assertIs<ReadoutRow.Band>(
                readoutRows(ReadoutPoint(position, 14.0, overlay = depth, band = ReadoutPoint.Band(null, 0.3)))[0],
            )

        assertEquals(listOf(20.0, 50.0), band.values)
        assertEquals(listOf(30.0), one.values)
        assertEquals(null, one.word)
    }

    @Test
    fun theCopiedPairIsTheLatitudeThenTheLongitudeToFivePlacesAndZeroHasNoSign() {
        val copied = assertIs<ReadoutRow.Coordinates>(readoutRows(ReadoutPoint(listOf(-0.1276, 51.5072), 14.0))[0])
        val zero = assertIs<ReadoutRow.Coordinates>(readoutRows(ReadoutPoint(listOf(-1e-6, -4e-6), 3.0))[0])

        assertEquals("51.50720, -0.12760", copied.copy)
        assertEquals("0.00000, 0.00000", zero.copy)
        assertEquals("0.01563", copiedCoordinate(0.015625))
    }

    @Test
    fun aZoneOfALevelTheTableDoesNotNameIsDrawnInInfosColourUnderNoWord() {
        val zone =
            readoutRows(
                ReadoutPoint(
                    position,
                    14.0,
                    zone =
                        ReadoutPoint.Zone(
                            "Closed",
                            "",
                            RestSchema.ZoneLevel("critical"),
                            Instant.fromEpochSeconds(10),
                            null,
                        ),
                ),
            ).last()

        val row = assertIs<ReadoutRow.Zone>(zone)
        assertEquals("#1c7ed6", row.color)
        assertEquals(null, row.levelWord)
        assertEquals(null, row.description)
        assertEquals("readout.zoneWindowFrom", row.window?.word)
        assertTrue(row.window?.end == null)
    }
}
