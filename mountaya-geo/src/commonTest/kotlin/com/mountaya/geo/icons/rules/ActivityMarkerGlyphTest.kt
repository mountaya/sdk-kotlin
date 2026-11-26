package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ActivityMarkerGlyphTest {
    private fun holding(vararg glyphs: String): GlyphSet =
        assertNotNull(
            GlyphSet.of("held", GlyphSet.Paint.Stroke, markup = glyphs.associateWith { "<path d=\"M0 0h1\"/>" }),
            "the held set is a set",
        )

    @Test
    fun anActivitysSilhouetteIsItsSlugUnderTheNamespaceWhereTheSetHoldsIt() {
        val set = holding("activity-gravel", "activity-hiking-and-trail")

        assertEquals("activity-gravel", activityMarkerGlyph(RestSchema.ActivitySlug("gravel"), set))
        assertEquals(
            "activity-hiking-and-trail",
            activityMarkerGlyph(RestSchema.ActivitySlug.HikingAndTrail, set),
        )
        assertEquals("activity-", ACTIVITY_MARKER_PREFIX)
    }

    @Test
    fun anActivityTheSetHoldsNoSilhouetteForNamesNone() {
        assertNull(activityMarkerGlyph(RestSchema.ActivitySlug.HikingAndTrail, GlyphSet.marker))
        assertNull(activityMarkerGlyph(RestSchema.ActivitySlug("hiking"), holding("hiking")))
    }

    @Test
    fun anEmptySlugNamesNoneNotTheBarePrefix() {
        assertNull(activityMarkerGlyph(RestSchema.ActivitySlug(""), holding("activity-")))
    }
}
