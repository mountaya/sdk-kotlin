package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class FieldSelectionTest {
    @Test
    fun aSelectionKeepsItsFieldsInTheOrderWrittenWithAFieldWithoutABlockSelectedWhole() {
        val selection =
            FieldSelection {
                field("routes") {
                    field("distance")
                    field("analysis") { field("elevations") }
                }
            }

        val elevations = FieldSelection(listOf(FieldSelection.Field("elevations", null)))
        val routes =
            FieldSelection(
                listOf(FieldSelection.Field("distance", null), FieldSelection.Field("analysis", elevations)),
            )
        assertEquals(FieldSelection(listOf(FieldSelection.Field("routes", routes))), selection)
    }

    @Test
    fun theOrderWrittenIsPartOfTheSelection() {
        val distanceFirst =
            FieldSelection {
                field("distance")
                field("duration")
            }
        val durationFirst =
            FieldSelection {
                field("duration")
                field("distance")
            }

        assertNotEquals(distanceFirst, durationFirst)
    }

    @Test
    fun aFieldNamedTwiceTakesTheSelectionGivenLast() {
        val selection =
            FieldSelection {
                field("routes") { field("distance") }
                field("duration")
                field("routes")
            }

        val expected =
            FieldSelection {
                field("duration")
                field("routes")
            }
        assertEquals(expected, selection)
    }

    @Test
    fun anEmptyBlockIsAnEmptySelectionAndNotAWholeOne() {
        assertNotEquals(FieldSelection { field("routes") }, FieldSelection { field("routes") {} })
    }
}
