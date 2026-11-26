package com.mountaya.build.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KotlinTextTest {
    @Test
    fun aNameIsWrittenAsTheContractWritesIt() {
        assertEquals("itinerary_id", KotlinText.identifier("itinerary_id"))
        assertEquals("POIsInput", KotlinText.identifier("POIsInput"))
        assertEquals("data", KotlinText.identifier("data"))
        assertEquals("value", KotlinText.identifier("value"))
    }

    @Test
    fun aKeywordOrAHyphenIsWrittenBetweenBackticks() {
        assertEquals("`in`", KotlinText.identifier("in"))
        assertEquals("`object`", KotlinText.identifier("object"))
        assertEquals("`Last-Event-ID`", KotlinText.identifier("Last-Event-ID"))
        assertEquals("`-updated_at`", KotlinText.identifier("-updated_at"))
    }

    @Test
    fun aNameNoTargetCanDeclareIsRefusedRatherThanRenamed() {
        for (name in listOf("a.b", "a/b", "a:b", "", "a<b>")) {
            assertFailsWith<ModelException>(name) { KotlinText.identifier(name) }
        }
    }

    @Test
    fun aDeprecationCarriesTheDescriptionsFirstSentence() {
        assertEquals("Use beds.", KotlinText.firstSentence("Use beds. It goes\nin the next major."))
        assertEquals("Version 1.2 of it, kept.", KotlinText.firstSentence("Version 1.2 of it, kept. Then more."))
        assertEquals("No full stop", KotlinText.firstSentence("No full stop"))
        assertEquals("The contract deprecates this, and a later major removes it.", KotlinText.firstSentence(null))
    }

    @Test
    fun documentationCannotCloseOrOpenAComment() {
        val text = StringBuilder().apply { with(KotlinText) { kdoc("", listOf("a */ b /* c")) } }.toString()
        assertEquals("/** a * / b / * c */\n", text)
    }
}
