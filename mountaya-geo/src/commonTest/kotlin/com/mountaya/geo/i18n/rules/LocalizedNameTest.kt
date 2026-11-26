package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import com.mountaya.client.generated.TileSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedNameTest {
    private fun rest(
        en: String? = null,
        fr: String? = null,
        de: String? = null,
    ) = RestSchema.Locales(
        de = de?.let { mapOf("name" to it) },
        en = en?.let { mapOf("name" to it) },
        es = null,
        fr = fr?.let { mapOf("name" to it) },
        it = null,
        ja = null,
        zh = null,
    )

    @Test
    fun anEntryIsNamedInItsLanguageThenInEnglishThenBySlug() {
        assertEquals(
            "Hauteur de neige",
            localizedName(rest(en = "Snow depth", fr = "Hauteur de neige"), "snow-depth", "fr"),
        )
        assertEquals("Snow depth", localizedName(rest(en = "Snow depth"), "snow-depth", "fr"))
        assertEquals("snow-depth", localizedName(rest(), "snow-depth", "fr"))
    }

    /** The language is read by its primary subtag, as a label language is. */
    @Test
    fun theLanguageIsReadByItsPrimarySubtag() {
        val locales = rest(en = "Snow depth", fr = "Hauteur de neige", de = "Schneehöhe")
        assertEquals("Hauteur de neige", localizedName(locales, "snow-depth", "fr-CA"))
        assertEquals("Hauteur de neige", localizedName(locales, "snow-depth", "FR"))
        assertEquals("Schneehöhe", localizedName(locales, "snow-depth", "de_CH"))
        assertEquals("Snow depth", localizedName(locales, "snow-depth", "frr"))
        assertEquals("Snow depth", localizedName(locales, "snow-depth", "pt"))
        assertEquals("Snow depth", localizedName(locales, "snow-depth", null))
    }

    /** An empty name falls through as a missing one does; one of white space is a name. */
    @Test
    fun anEmptyNameFallsThroughAndOneOfWhiteSpaceIsAName() {
        assertEquals("Snow depth", localizedName(rest(en = "Snow depth", fr = ""), "snow-depth", "fr"))
        assertEquals("snow-depth", localizedName(rest(en = ""), "snow-depth", "de"))
        assertEquals(" ", localizedName(rest(en = "Snow depth", fr = " "), "snow-depth", "fr"))
    }

    @Test
    fun theTileApisEntriesAreNamedTheSameWay() {
        val locales =
            TileSchema.Locales(
                de = null,
                en = mapOf("name" to "Snow depth"),
                es = null,
                fr = mapOf("name" to null),
                it = null,
                ja = null,
                zh = null,
            )
        assertEquals("Snow depth", localizedName(locales, "snow-depth", "fr"))
        assertEquals("Snow depth", localizedName(locales, "snow-depth", "ja"))
    }
}
