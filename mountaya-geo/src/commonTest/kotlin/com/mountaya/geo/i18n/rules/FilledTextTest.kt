package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FilledTextTest {
    @Test
    fun aTemplateTakesTheValueHandedToItInPlaceOfItsPlaceholder() {
        assertEquals("15 min stop", filledText("{elapsed} stop", "15 min"))
        assertEquals("15 min d’arrêt", filledText("{elapsed} d’arrêt", "15 min"))
        assertEquals("Cut-off 21:30", filledText("Cut-off {moment}", "21:30"))
        assertEquals("Open 8:00 to 18:00", filledText("Open {hours}", "8:00 to 18:00"))
    }

    @Test
    fun theValueIsWrittenExactlyAsHandedNeverReadAsAPattern() {
        assertEquals("Ouvert $& 8:00", filledText("Ouvert {hours}", "$& 8:00"))
        assertEquals("""\1 $1 {a}""", filledText("{a}", """\1 $1 {a}"""))
        assertEquals("", filledText("{a}", ""))
    }

    @Test
    fun onlyTheFirstPlaceholderFillsAndATemplateHoldingNoneIsWrittenAsItIs() {
        assertEquals("x and {b}", filledText("{a} and {b}", "x"))
        assertEquals("{x}", filledText("{{a}}", "x"))
        assertEquals("x", filledText("{a1}", "x"))
        assertEquals("no placeholder", filledText("no placeholder", "x"))
        assertEquals("{}", filledText("{}", "x"))
        assertEquals("{1a} {a b} {a-b} {é}", filledText("{1a} {a b} {a-b} {é}", "x"))
        assertEquals("{elapsed", filledText("{elapsed", "x"))
        assertEquals("", filledText("", "x"))
    }

    @Test
    fun aPlaceholderIsReadCodePointByCodePointItsBraceStandingWhereItsCodePointDoes() {
        // The opening brace and the combining acute accent are one character, and two code points.
        assertEquals("e{́a}", filledText("e{́a}", "x"))
        assertEquals("́x", filledText("́{a}", "x"))
        assertEquals("{c}", firstPlaceholder("a {b} {c}", "c")?.let { "a {b} {c}".substring(it) })
        assertNull(firstPlaceholder("a {b}", "B"))
    }

    @Test
    fun aTemplateHandedSeveralValuesTakesEachByItsNameWhereverTheWordsPutIt() {
        val values = mapOf("min" to "2°C", "max" to "5°C")

        assertEquals("Between 2°C and 5°C", filledText("Between {min} and {max}", values))
        assertEquals("Up to 5°C, from 2°C", filledText("Up to {max}, from {min}", values))
        assertEquals("{min} to {min}", filledText("{min} to {min}", emptyMap()))
        assertEquals("2°C and {upper}", filledText("{min} and {upper}", values))
    }

    @Test
    fun aValueHandedByNameIsWrittenAsHandedAndFillsNoPlaceholderItHolds() {
        assertEquals("a {max} b", filledText("a {min} b", mapOf("min" to "{max}", "max" to "x")))
        assertEquals("\$&", filledText("{min}", mapOf("min" to "\$&")))
    }
}
