package com.mountaya.geo

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelLanguageTest {
    @Test
    fun aTagIsReadByItsPrimarySubtagWhateverItsCase() {
        assertEquals(RestSchema.Language.Fr, labelLanguage("fr-CA"))
        assertEquals(RestSchema.Language.Fr, labelLanguage("fr_CA"))
        assertEquals(RestSchema.Language.Ja, labelLanguage("JA-JP"))
        assertEquals(RestSchema.Language.Zh, labelLanguage("zh-Hant-TW"))
        assertEquals(RestSchema.Language.De, labelLanguage("De"))
        // A subtag ended by its hyphen is still the subtag.
        assertEquals(RestSchema.Language.Fr, labelLanguage("fr-"))
    }

    @Test
    fun aTagNamingNoLabelLanguageAndNoTagLabelInEnglish() {
        for (tag in listOf("frr", "pt-BR", "und-FR", "*", "", " fr", "fr CA", null)) {
            assertEquals(RestSchema.Language.En, labelLanguage(tag), "$tag")
        }
    }

    /** Only the ASCII letters are folded: a dotted capital I folds to no ASCII `i`. */
    @Test
    fun aLetterOutsideAsciiFoldsToNoLanguage() {
        assertEquals(RestSchema.Language.En, labelLanguage("İt"))
        assertEquals(RestSchema.Language.En, labelLanguage("ＦＲ"))
    }

    @Test
    fun theLanguagesAreTheSevenLocalesIsKeyedBy() {
        assertEquals(listOf("en", "fr", "es", "it", "de", "zh", "ja"), labelLanguages.map { it.value })
    }
}
