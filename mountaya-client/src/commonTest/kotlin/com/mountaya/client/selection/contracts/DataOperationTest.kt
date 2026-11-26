package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class DataOperationTest {
    private val directions =
        DataOperation(
            name = "directions",
            inputType = "DirectionsInput!",
            selection =
                FieldSelection {
                    field("routes") {
                        field("distance")
                        field("duration")
                        field("analysis") { field("elevations") }
                    }
                },
        )

    @Test
    fun twoAreEqualWhereTheyStateTheSameTheirBudgetIncluded() {
        val timed = DataOperation(directions.name, directions.inputType, directions.selection, 30.seconds)

        assertNull(directions.timeout)
        assertEquals(30.seconds, timed.timeout)
        assertNotEquals(directions, timed)
        assertEquals(timed, DataOperation("directions", "DirectionsInput!", directions.selection, 30.seconds))
        assertEquals(
            timed.hashCode(),
            DataOperation("directions", "DirectionsInput!", directions.selection, 30.seconds).hashCode(),
        )
        assertEquals(directions.document, timed.document)
    }

    @Test
    fun theFullDocumentIsPrintedFromTheFieldTree() {
        assertEquals(
            """
            query Directions(${'$'}input: DirectionsInput!) {
              directions(input: ${'$'}input) {
                routes {
                  distance
                  duration
                  analysis {
                    elevations
                  }
                }
              }
            }

            """.trimIndent(),
            directions.document,
        )
    }

    @Test
    fun noSelectionSendsTheFullDocumentUnchanged() {
        assertEquals(directions.document, directions.document(selecting = null))
    }

    @Test
    fun aSelectionIsPrintedInTheTreesOrderHoweverItWasWritten() {
        val document =
            directions.document(
                selecting =
                    FieldSelection {
                        field("routes") {
                            field("analysis") { field("elevations") }
                            field("distance")
                        }
                    },
            )

        assertEquals(
            """
            query Directions(${'$'}input: DirectionsInput!) {
              directions(input: ${'$'}input) {
                routes {
                  distance
                  analysis {
                    elevations
                  }
                }
              }
            }

            """.trimIndent(),
            document,
        )
    }

    @Test
    fun aFieldOutsideTheTreeIsRefusedAtTheCallNamingIt() {
        val refusal =
            assertFailsWith<SelectionException> {
                directions.document(selecting = FieldSelection { field("routes") { field("nope") } })
            }

        assertEquals(listOf("routes", "nope"), refusal.path)
    }

    @Test
    fun theWholeTreeSelectedSendsTheFullDocument() {
        assertEquals(directions.document, directions.document(selecting = directions.selection))
    }
}
