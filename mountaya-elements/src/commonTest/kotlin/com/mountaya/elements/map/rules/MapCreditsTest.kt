package com.mountaya.elements

import kotlin.test.Test
import kotlin.test.assertEquals

class MapCreditsTest {
    private val osm =
        "<a href=\"https://www.openstreetmap.org/copyright\" target=\"_blank\">" +
            "&copy; OpenStreetMap contributors</a>"

    @Test
    fun mountayaIsCreditedFirstThenEachDrawnSourcesCreditOnceInOrder() {
        assertEquals(listOf(MOUNTAYA_CREDIT), mapCredits(emptyList()))
        assertEquals(
            listOf(MOUNTAYA_CREDIT, osm),
            mapCredits(
                listOf(
                    CreditedSource(MOUNTAYA_CREDIT, isDrawn = true),
                    CreditedSource(osm, isDrawn = true),
                    CreditedSource(MOUNTAYA_CREDIT, isDrawn = true),
                ),
            ),
        )
        assertEquals(
            listOf(MOUNTAYA_CREDIT, "Imagery &copy; Satellite Provider", osm),
            mapCredits(
                listOf(
                    CreditedSource("Imagery &copy; Satellite Provider", isDrawn = true),
                    CreditedSource(osm, isDrawn = true),
                ),
            ),
        )
    }

    @Test
    fun aBlankCreditAnAbsentOneAndThatOfASourceNoLayerDrawsAreLeftOut() {
        assertEquals(
            listOf(MOUNTAYA_CREDIT),
            mapCredits(
                listOf(
                    CreditedSource(null, isDrawn = true),
                    CreditedSource("", isDrawn = true),
                    CreditedSource("   ", isDrawn = true),
                    CreditedSource("﻿　\t", isDrawn = true),
                    CreditedSource(osm, isDrawn = false),
                ),
            ),
        )
        assertEquals(
            listOf(MOUNTAYA_CREDIT, "\u0085"),
            mapCredits(listOf(CreditedSource("\u0085", isDrawn = true))),
            "NEL is no white space ECMAScript trims",
        )
    }

    @Test
    fun twoSpellingsOfOneCreditAreTwoCreditsComparedAsWritten() {
        val quoted = "<a href='https://mountaya.com/' target='_blank'>&copy; Mountaya</a>"

        assertEquals(listOf(MOUNTAYA_CREDIT, quoted), mapCredits(listOf(CreditedSource(quoted, isDrawn = true))))
        assertEquals(
            3,
            mapCredits(listOf(CreditedSource("café", isDrawn = true), CreditedSource("café", isDrawn = true))).size,
        )
        assertEquals("<a href=\"https://mountaya.com/\" target=\"_blank\">&copy; Mountaya</a>", MOUNTAYA_CREDIT)
    }
}
