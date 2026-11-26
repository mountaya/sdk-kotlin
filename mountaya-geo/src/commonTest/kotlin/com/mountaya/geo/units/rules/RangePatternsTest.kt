package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class RangePatternsTest {
    /** A language's two patterns, the range then the approximately, since a row compares by what it holds. */
    private fun patternsOf(language: String): List<String> =
        rangePatterns(language).let { listOf(it.range, it.approximately) }

    @Test
    fun theSevenLanguagesHoldCldrsRangeAndApproximatelyPatternsAnyOtherEnglishs() {
        assertEquals(unitPatternLanguages, rangePatternLanguages)
        assertEquals(listOf("{0}\u2013{1}", "~{0}"), patternsOf("en"))
        assertEquals("\u2243{0}", rangePatterns("fr-CA").approximately)
        assertEquals("\u2248{0}", rangePatterns("de").approximately)
        assertEquals("{0}-{1}", rangePatterns("es").range)
        assertEquals("{0}-{1}", rangePatterns("it").range)
        assertEquals(listOf("{0}\uFF5E{1}", "\u7D04{0}"), patternsOf("ja"))
        assertEquals("{0}-{1}", rangePatterns("zh").range)
        assertEquals(patternsOf("en"), patternsOf("pt"))
    }
}
