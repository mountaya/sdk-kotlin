package com.mountaya.build.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The words an enum member is printed under, read off the name the model gives it (SPEC.md section 3). */
class EnumMembersTest {
    @Test
    fun aRestOrTileMemberIsItsNameLessItsTypesInUpperCamelCase() {
        assertEquals("Line", EnumMembers.openApi("TileLayerType", "TileLayerTypeLine"))
        assertEquals("RasterDem", EnumMembers.openApi("TileSourceType", "TileSourceTypeRasterDem"))
        assertEquals("Fr", EnumMembers.openApi("Language", "LanguageFr"))
        assertEquals(
            "OrganizationMemberUpdated",
            EnumMembers.openApi("WebhookEventType", "WebhookEventTypeOrganizationMemberUpdated"),
        )
    }

    @Test
    fun aFirstWordThatStartsWithADigitKeepsTheTypesLastWordInFront() {
        assertEquals("Size512", EnumMembers.openApi("ElevationTileSize", "ElevationTileSize512"))
        assertEquals("Class2xx", EnumMembers.openApi("UsageStatusClass", "UsageStatusClass2xx"))
        assertEquals("Window24h", EnumMembers.openApi("WebhookHealthWindow", "WebhookHealthWindow24h"))
        assertEquals("Mtb0", EnumMembers.openApi("TrailDifficultyType", "TrailDifficultyTypeMtb0"))
        assertEquals("Class2xx", EnumMembers.data("UsageStatusClass", "2XX"))
    }

    @Test
    fun aDataApiMemberIsItsNameSplitAtEachUnderscoreInUpperCamelCase() {
        assertEquals("Places", EnumMembers.data("SearchTarget", "PLACES"))
        assertEquals("HikingAndTrail", EnumMembers.data("ActivitySlug", "HIKING_AND_TRAIL"))
        assertEquals("Mtb0", EnumMembers.data("TrailDifficultyType", "MTB_0"))
        assertEquals("From25To30", EnumMembers.data("SteepnessBand", "FROM_25_TO_30"))
        assertEquals("Osm", EnumMembers.data("OSMType", "OSM"))
    }

    @Test
    fun aMemberAndItsTwinOnTheOtherSurfaceArePrintedAlike() {
        assertEquals(
            EnumMembers.openApi("ActivitySlug", "ActivitySlugHikingAndTrail"),
            EnumMembers.data("ActivitySlug", "HIKING_AND_TRAIL"),
        )
        assertEquals(
            EnumMembers.openApi("TrailDifficultyType", "TrailDifficultyTypeMtb6"),
            EnumMembers.data("TrailDifficultyType", "MTB_6"),
        )
    }

    @Test
    fun aNameTheRuleCannotReadIsRefusedRatherThanGuessedAt() {
        for ((type, name) in listOf(
            "Season" to "Summer",
            "Season" to "Season",
            "Season" to "Seasonsummer",
            "Season" to "Season_Summer",
        )) {
            assertFailsWith<ModelException>("$type.$name") { EnumMembers.openApi(type, name) }
        }
        for (name in listOf("", "_HIKING", "HIKING_", "HIKING__TRAIL", "HIKING-TRAIL")) {
            assertFailsWith<ModelException>(name) { EnumMembers.data("Activity", name) }
        }
    }

    @Test
    fun twoMembersPrintedAsOneNameOnceCaseIsSetAsideAreRefused() {
        val members = listOf(constant("Summer", "SeasonSummer"), constant("SUMMER", "SeasonSUMMER"))
        val refused = assertFailsWith<ModelException> { EnumMembers.check("RestSchema.Season", members) }
        assertEquals(
            "RestSchema.Season: the members SeasonSummer and SeasonSUMMER are printed as Summer and SUMMER, one name " +
                "once case is set aside, and the printer renames nothing",
            refused.message,
        )
    }

    @Test
    fun aMemberPrintedAsANameTheValueClassDeclaresOfItsOwnIsRefused() {
        for (name in listOf("Value", "Companion", "Serializer", "ToString", "Equals", "HashCode")) {
            val refused =
                assertFailsWith<ModelException>(name) {
                    EnumMembers.check("DataSchema.Activity", listOf(constant(name, name.uppercase())))
                }
            assertTrue("which reads as the `" in refused.message.orEmpty(), name)
        }
        EnumMembers.check("DataSchema.Activity", listOf(constant("Values", "VALUES"), constant("Hiking", "HIKING")))
    }

    private fun constant(
        name: String,
        source: String,
    ) = SchemaPrinter.EnumConstant(name, source, "\"$source\"", null, false)
}
