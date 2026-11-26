package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.generated.ElementLayout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WatermarkOfTest {
    @Test
    fun noMarkIsDrawnWhileTheReadWaitsOrOnceItAnswersWhiteLabel() {
        assertNull(watermarkOf(OrganizationRead.Waiting, RestSchema.Language.En))
        assertNull(watermarkOf(OrganizationRead.Answered(whiteLabel = true), RestSchema.Language.Fr))
    }

    @Test
    fun aMarkIsDrawnOnceItAnswersWithoutWhiteLabelWhereItFailsOrTimesOutAndWhereNoKeyReads() {
        val reads =
            listOf(
                OrganizationRead.Answered(false),
                OrganizationRead.Failed,
                OrganizationRead.TimedOut,
                OrganizationRead.NoKey,
            )
        for (read in reads) assertNotNull(watermarkOf(read, RestSchema.Language.En), "$read draws the mark")
    }

    @Test
    fun theFramesExtentIsPublishedForAHostToKeepClear() {
        val mark =
            assertNotNull(
                watermarkOf(OrganizationRead.NoKey, RestSchema.Language.En),
                "a map with no key draws the mark",
            )

        assertEquals(180.0, watermarkWidth)
        assertEquals(mark.width, watermarkWidth)
        assertEquals(-4.0, watermarkInset)
        assertEquals(mark.x, watermarkInset)
        assertEquals(mark.y, watermarkInset)
        assertEquals(180.0 * 58.0 / 138.0, watermarkHeight, 1e-9)
        assertEquals(mark.height, watermarkHeight)
        assertEquals(mark.y + mark.height, watermarkClearance)
        assertEquals(-4.0 + 180.0 * 58.0 / 138.0, watermarkClearance, 1e-9)
    }

    @Test
    fun noChangeToThePublishedArtworkReachesTheMark() {
        val artwork: Any = GlyphDrawing.artwork

        assertFalse(artwork is MutableMap<*, *>, "the published table is no mutable map")
        assertFailsWith<ClassCastException>("a cast to a mutable map fails") { artwork as MutableMap<*, *> }
        assertNotNull(GlyphDrawing.artwork["watermark"], "the table holds the watermark")
        assertNotNull(watermarkOf(OrganizationRead.Failed, RestSchema.Language.En), "the mark is still drawn")
    }

    @Test
    fun theMarkIsTheWatermarkDrawingInItsFrameLabelledByItsWord() {
        val mark =
            assertNotNull(watermarkOf(OrganizationRead.Failed, RestSchema.Language.En), "a failed read draws the mark")
        val box = mark.drawing.viewBox

        assertEquals(GlyphDrawing.artwork["watermark"], mark.drawing)
        assertEquals(ElementLayout.watermarkInset, mark.x)
        assertEquals(ElementLayout.watermarkInset, mark.y)
        assertEquals(ElementLayout.watermarkWidth, mark.width)
        assertEquals(ElementLayout.watermarkWidth * box[3] / box[2], mark.height)
        assertEquals("watermark.label", mark.label)
    }

    @Test
    fun theLinkIsMountayasSiteInTheWordsLanguageOrItsRoot() {
        fun link(language: RestSchema.Language) = watermarkOf(OrganizationRead.NoKey, language)?.link

        assertEquals("https://www.mountaya.com/en/", link(RestSchema.Language.En))
        assertEquals("https://www.mountaya.com/fr/", link(RestSchema.Language.Fr))
        assertEquals("https://www.mountaya.com/", link(RestSchema.Language.It))
        assertEquals("https://www.mountaya.com/", link(RestSchema.Language.Ja))
    }

    @Test
    fun aTopLevelPageOnMountayasOwnOriginOmitsTheMarkAndEveryOtherPageKeepsIt() {
        val answered = OrganizationRead.Answered(whiteLabel = false)

        for (origin in watermarkFirstPartyOrigins) {
            assertNull(watermarkOf(answered, RestSchema.Language.En, WatermarkPage(origin, isTopLevel = true)), origin)
            assertNotNull(
                watermarkOf(answered, RestSchema.Language.En, WatermarkPage(origin, isTopLevel = false)),
                "a framed page on $origin keeps the mark",
            )
        }
        for (origin in listOf("https://studio.mountaya.com", "http://www.mountaya.com", "https://guides.example.com")) {
            assertNotNull(
                watermarkOf(answered, RestSchema.Language.En, WatermarkPage(origin, isTopLevel = true)),
                "a top-level page on $origin keeps the mark",
            )
        }
    }

    @Test
    fun thePublishedRuleIsTheRuleOfNoDocument() {
        for (read in listOf(OrganizationRead.Answered(false), OrganizationRead.Waiting, OrganizationRead.Failed)) {
            assertEquals(
                watermarkOf(read, RestSchema.Language.Fr, page = null),
                watermarkOf(read, RestSchema.Language.Fr),
            )
        }
    }
}
