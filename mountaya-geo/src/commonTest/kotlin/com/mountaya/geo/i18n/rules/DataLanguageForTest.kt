package com.mountaya.geo

import com.mountaya.client.generated.DataSchema
import kotlin.test.Test
import kotlin.test.assertEquals

class DataLanguageForTest {
    @Test
    fun aTagAsksTheDataApiInTheLanguageTheMapLabelsInForItInCapitals() {
        assertEquals(DataSchema.Language.Fr, dataLanguageFor("fr-CH"))
        assertEquals(DataSchema.Language.De, dataLanguageFor("de_AT"))
        assertEquals(DataSchema.Language.Zh, dataLanguageFor("zh-Hant-TW"))
        assertEquals(DataSchema.Language.Ja, dataLanguageFor("JA"))
        assertEquals(DataSchema.Language.Es, dataLanguageFor("es"))
        assertEquals(DataSchema.Language.It, dataLanguageFor("it"))
    }

    @Test
    fun aTagNamingNoneOfTheMapsLanguagesOrNoTagAsksInEnglish() {
        assertEquals(DataSchema.Language.En, dataLanguageFor("pt-BR"))
        assertEquals(DataSchema.Language.En, dataLanguageFor("frr"))
        assertEquals(DataSchema.Language.En, dataLanguageFor(""))
        assertEquals(DataSchema.Language.En, dataLanguageFor(null))
        assertEquals(DataSchema.Language.En, dataLanguageFor(" fr"))
    }
}
