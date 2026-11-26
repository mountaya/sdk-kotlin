package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MapPaintTest {
    private val standard = MapPaint.standard

    @Test
    fun theStandardPaintIsThePaintTheContractShips() {
        val itinerary = standard.itinerary
        assertEquals("#ffffff", itinerary.casingColor)
        assertEquals(0.9, itinerary.casingOpacity)
        assertEquals(listOf(7.0, 4.0, 10.0, 4.0, 13.0, 5.0, 16.0, 7.0), itinerary.casingWidthStops)
        assertEquals(listOf(2.0, 2.0), itinerary.dashArray)
        assertEquals(0.6, itinerary.dashedOpacity)
        assertEquals(listOf(7.0, 2.0, 10.0, 2.0, 13.0, 3.0, 16.0, 5.0), itinerary.lineWidthStops)
    }

    /**
     * The ranks and the segments' colours are the values the rank and colour rules read, never a second
     * copy of them.
     */
    @Test
    fun theStandardRanksAndSegmentColoursAreTheOnesTheRulesPublish() {
        assertEquals(tileSourceLayerRanks, standard.layerRanks)
        assertEquals(11, standard.layerRanks.size)
        assertEquals(DEFAULT_SEGMENT_COLOR, standard.segments.defaultColor)
        assertEquals(livelyPalette, standard.segments.palette)
    }

    @Test
    fun theStandardCasingIsWiderThanTheLineAtEveryStopAndStopsAtTheSameZooms() {
        val line = standard.itinerary.lineWidthStops
        val casing = standard.itinerary.casingWidthStops
        assertEquals(line.size, casing.size)
        for (index in line.indices step 2) {
            assertEquals(line[index], casing[index])
            assertTrue(casing[index + 1] > line[index + 1])
        }
    }

    @Test
    fun aMemberSetOnACopyChangesThatMemberAloneAndTheStandardPaintStands() {
        val paint = standard.copy { itinerary.casingColor = "#1b1b1b" }
        assertEquals("#1b1b1b", paint.itinerary.casingColor)
        assertEquals(0.9, paint.itinerary.casingOpacity)
        assertEquals(standard.itinerary.casingWidthStops, paint.itinerary.casingWidthStops)
        assertEquals(standard.itinerary.dashArray, paint.itinerary.dashArray)
        assertEquals(standard.itinerary.dashedOpacity, paint.itinerary.dashedOpacity)
        assertEquals(standard.itinerary.lineWidthStops, paint.itinerary.lineWidthStops)
        assertEquals(standard.layerRanks, paint.layerRanks)
        assertEquals(standard.segments, paint.segments)
        assertEquals("#ffffff", MapPaint.standard.itinerary.casingColor)
        assertNotEquals(standard, paint)
    }

    /**
     * A table merged position by position would keep the shipped tail behind a shorter one: `[7, 3]`
     * would draw 3 pixels at zoom 7 and the shipped widths above it.
     */
    @Test
    fun aListSetReplacesTheShippedOneWhole() {
        val paint =
            standard.copy {
                itinerary.lineWidthStops = listOf(7.0, 3.0)
                segments.palette = listOf("#0b7285", "#e8590c")
            }
        assertEquals(listOf(7.0, 3.0), paint.itinerary.lineWidthStops)
        assertEquals(listOf("#0b7285", "#e8590c"), paint.segments.palette)
        assertEquals(DEFAULT_SEGMENT_COLOR, paint.segments.defaultColor)
    }

    /**
     * A band named after a member every JavaScript object has is left out, as every merge of a patch leaves those
     * names out in every SDK, and a band beside it is added all the same.
     */
    @Test
    fun aBandNamedAfterAPrototypeMemberIsLeftOut() {
        val paint =
            standard.copy {
                layerRanks += "__proto__" to 10
                layerRanks += "acme_avalanche" to 45
                layerRanks += "constructor" to 20
                layerRanks += "prototype" to 30
            }

        assertEquals(standard.layerRanks + ("acme_avalanche" to 45), paint.layerRanks)
        assertEquals(standard, standard.copy { layerRanks = layerRanks + ("constructor" to 1) })
    }

    @Test
    fun aBandAddedKeepsEveryShippedBandAndABandMovedKeepsTheOthers() {
        val added = standard.copy { layerRanks += "acme_avalanche" to LayerRank.Zone.rank }
        assertEquals(standard.layerRanks.size + 1, added.layerRanks.size)
        assertEquals(40, added.layerRanks["acme_avalanche"])
        assertEquals(50, added.layerRanks["itinerary_lines"])

        val moved = standard.copy { layerRanks += "poi_food" to 85 }
        assertEquals(85, moved.layerRanks["poi_food"])
        assertEquals(80, moved.layerRanks["poi_tourism"])
        assertEquals(standard.layerRanks.size, moved.layerRanks.size)
    }

    @Test
    fun aPatchAtTwoDepthsSetsEachMemberItNames() {
        val paint =
            standard.copy {
                itinerary.casingOpacity = 1.0
                itinerary.dashArray = listOf(1.0, 3.0)
                segments.defaultColor = "#495057"
            }
        assertEquals(1.0, paint.itinerary.casingOpacity)
        assertEquals(listOf(1.0, 3.0), paint.itinerary.dashArray)
        assertEquals("#ffffff", paint.itinerary.casingColor)
        assertEquals("#495057", paint.segments.defaultColor)
        assertEquals(livelyPalette, paint.segments.palette)
    }

    @Test
    fun twoPaintsAreEqualWhereEveryMemberHoldsTheSameValues() {
        assertEquals(standard, standard.copy {})
        assertEquals(standard.hashCode(), standard.copy {}.hashCode())
        assertNotEquals(standard, standard.copy { itinerary.dashedOpacity = 0.5 })
        assertNotEquals(standard, standard.copy { segments.palette = segments.palette.dropLast(1) })
        assertNotEquals(standard, standard.copy { layerRanks += "fills" to 41 })
        assertNotEquals(standard, standard.copy { layerRanks -= "fills" })
    }

    @Test
    fun twoPaintsCompareTheirNumbersAsTheSameNumbers() {
        val unknown = standard.copy { itinerary.casingOpacity = Double.NaN }
        val also = standard.copy { itinerary.casingOpacity = Double.NaN }
        assertEquals(also, unknown)
        assertEquals(also.hashCode(), unknown.hashCode())
        val zero = standard.copy { itinerary.lineWidthStops = listOf(0.0, 2.0) }
        val negativeZero = standard.copy { itinerary.lineWidthStops = listOf(-0.0, 2.0) }
        assertNotEquals(negativeZero, zero)
        assertNotEquals(negativeZero.itinerary, zero.itinerary)
    }

    /** A Kotlin string compares by its code units: `é` and `e` with a combining acute are two texts. */
    @Test
    fun twoPaintsCompareTheirTextsAndTheirSourceLayersByCodePoint() {
        val composed = standard.copy { itinerary.casingColor = "café" }
        val decomposed = standard.copy { itinerary.casingColor = "café" }
        assertNotEquals(decomposed, composed)

        val palette = standard.copy { segments.palette = listOf("café") }
        val otherPalette = standard.copy { segments.palette = listOf("café") }
        assertNotEquals(otherPalette.segments, palette.segments)

        val band = standard.copy { layerRanks += "café" to 52 }
        assertNotEquals(standard.copy { layerRanks += "café" to 52 }, band)
        assertEquals(standard.copy { layerRanks += "café" to 52 }, band)
    }
}
