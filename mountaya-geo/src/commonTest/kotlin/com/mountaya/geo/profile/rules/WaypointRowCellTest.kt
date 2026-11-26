package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Instant

class WaypointRowCellTest {
    private val english = UnitPreferences("en")
    private val water = RestSchema.WaypointAmenity("water")
    private val hotMeal = RestSchema.WaypointAmenity("hot-meal")
    private val refuge =
        WaypointDetails(
            amenities = listOf(water, hotMeal),
            timing = WaypointDetails.Timing(cutoffAt = Instant.fromEpochSeconds(1_788_000_000)),
            properties =
                listOf(
                    WaypointDetails.Property(
                        "next-water",
                        "Next water",
                        "3600",
                        RestSchema.WaypointPropertyFormat("distance"),
                    ),
                    WaypointDetails.Property(
                        "page",
                        "Page",
                        " https://refuge.example ",
                        RestSchema.WaypointPropertyFormat("url"),
                    ),
                ),
        )

    private fun row(
        name: String? = "Refuge Elisabetta",
        glyph: RestSchema.WaypointIcon? = RestSchema.WaypointIcon("hut"),
        details: WaypointDetails? = refuge,
    ): ItineraryWaypointRow =
        ItineraryWaypointRow(
            key = "refuge#0",
            place =
                ProfilePlace(
                    ProfileWaypoint.AtDistance("refuge", 14_600.0),
                    name = name,
                    glyph = glyph,
                    details = details,
                ),
            index = 3,
            from = 9_400.0,
            to = 14_600.0,
            metrics =
                ItineraryWaypointRowMetrics(
                    distance = 14_600.0,
                    sectionDistance = 5_200.0,
                    ascent = 1_250.0,
                    descent = 310.0,
                    sectionAscent = 640.0,
                    sectionDescent = 25.0,
                    altitude = 2_102.0,
                ),
        )

    /** A stroked set drawing the glyphs named, each a one-unit line. */
    private fun glyphSet(vararg glyphs: String): GlyphSet =
        assertNotNull(
            GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = glyphs.associateWith { "<path d=\"M0 0h1\"/>" }),
            "the set is built",
        )

    @Test
    fun theDefaultColumnsAndTheFiguresOfAPlace() {
        assertEquals(
            listOf(
                WaypointTableColumn.Waypoint,
                WaypointTableColumn.Profile,
                WaypointTableColumn.Distance,
                WaypointTableColumn.Elevation,
                WaypointTableColumn.Amenities,
            ),
            defaultWaypointTableColumns,
        )
        val held = glyphSet("hut")
        val cell = { column: WaypointTableColumn -> waypointRowCell(row(), column, held, english) }

        assertEquals(WaypointRowCell.Waypoint("hut", "Refuge Elisabetta"), cell(WaypointTableColumn.Waypoint))
        assertEquals(WaypointRowCell.Profile(ProfileRange(9_400.0, 14_600.0)), cell(WaypointTableColumn.Profile))
        assertEquals(WaypointRowCell.Distance(14_600.0), cell(WaypointTableColumn.Distance))
        assertEquals(WaypointRowCell.Elevation(640.0, 25.0), cell(WaypointTableColumn.Elevation))
        assertEquals(WaypointRowCell.Altitude(2_102.0), cell(WaypointTableColumn.Altitude))
        assertEquals(WaypointRowCell.Amenities(listOf(water, hotMeal)), cell(WaypointTableColumn.Amenities))
    }

    @Test
    fun aGlyphTheSetCannotDrawOrNoneIsThePinWhetherTheSetDrawsItOrNot() {
        assertEquals(
            WaypointRowCell.Waypoint("map-pin", null),
            waypointRowCell(row(name = null), WaypointTableColumn.Waypoint, glyphSet(), english),
        )
        assertEquals(
            WaypointRowCell.Waypoint("map-pin", "Refuge Elisabetta"),
            waypointRowCell(row(glyph = null), WaypointTableColumn.Waypoint, glyphSet("hut"), english),
        )
    }

    @Test
    fun aCutoffOnlyThroughTheCallersFormatterFieldsByTheirFormatWithTheirLinks() {
        val held = glyphSet("hut")

        assertEquals(WaypointRowCell.Cutoff(null), waypointRowCell(row(), WaypointTableColumn.Cutoff, held, english))
        assertEquals(
            WaypointRowCell.Cutoff("12:30"),
            waypointRowCell(row(), WaypointTableColumn.Cutoff, held, english) { "12:30" },
        )
        assertEquals(
            WaypointRowCell.Properties(
                listOf(
                    WaypointField("next-water", "Next water", "3.6 km"),
                    WaypointField("page", "Page", " https://refuge.example ", "https://refuge.example"),
                ),
            ),
            waypointRowCell(row(), WaypointTableColumn.Properties, held, english),
        )
    }

    @Test
    fun aPlaceThatStatesNothingStatesNoServiceCutoffOrField() {
        val bare = row(details = null)
        val held = glyphSet("hut")

        assertEquals(
            WaypointRowCell.Amenities(emptyList()),
            waypointRowCell(bare, WaypointTableColumn.Amenities, held, english),
        )
        assertEquals(
            WaypointRowCell.Properties(emptyList()),
            waypointRowCell(bare, WaypointTableColumn.Properties, held, english),
        )
        assertEquals(
            WaypointRowCell.Cutoff(null),
            waypointRowCell(bare, WaypointTableColumn.Cutoff, held, english) { "12:30" },
        )
    }
}
