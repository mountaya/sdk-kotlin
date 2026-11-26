package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SelectionNarrowingTest {
    private val tree =
        FieldSelection {
            field("routes") {
                field("analysis") {
                    field("elevations")
                    field("advisories") { field("tollway") }
                }
                field("distance")
                field("duration")
            }
            field("summary")
        }

    private fun refusal(selection: FieldSelection): SelectionException =
        assertFailsWith<SelectionException> { SelectionNarrowing.narrow(tree, selection) }

    @Test
    fun theNarrowedSelectionFollowsTheTreesOrder() {
        val narrowed =
            SelectionNarrowing.narrow(
                tree,
                FieldSelection {
                    field("summary")
                    field("routes") {
                        field("duration")
                        field("analysis") { field("elevations") }
                    }
                },
            )

        val expected =
            FieldSelection {
                field("routes") {
                    field("analysis") { field("elevations") }
                    field("duration")
                }
                field("summary")
            }
        assertEquals(expected, narrowed)
    }

    @Test
    fun theWholeTreeNarrowsToItself() {
        assertEquals(tree, SelectionNarrowing.narrow(tree, tree))
    }

    @Test
    fun aFieldOutsideTheTreeIsNamedByItsPathWithTheFieldsThatAreThere() {
        val refusal = refusal(FieldSelection { field("routes") { field("analysis") { field("nope") } } })

        assertEquals(listOf("routes", "analysis", "nope"), refusal.path)
        assertTrue(refusal.message.orEmpty().contains("`elevations`, `advisories`"), refusal.message)
    }

    /** `true` on an object selects its whole subtree, at the top and inside a narrowing alike. */
    @Test
    fun anObjectSelectedWholeIsSelectedWithItsWholeSubtree() {
        val wholeRoutes =
            FieldSelection {
                field("routes") {
                    field("analysis") {
                        field("elevations")
                        field("advisories") { field("tollway") }
                    }
                    field("distance")
                    field("duration")
                }
            }
        val wholeAnalysis =
            FieldSelection {
                field("routes") {
                    field("analysis") {
                        field("elevations")
                        field("advisories") { field("tollway") }
                    }
                    field("duration")
                }
            }

        assertEquals(wholeRoutes, SelectionNarrowing.narrow(tree, FieldSelection { field("routes") }))
        assertEquals(
            wholeAnalysis,
            SelectionNarrowing.narrow(
                tree,
                FieldSelection {
                    field("routes") {
                        field("duration")
                        field("analysis")
                    }
                },
            ),
        )
    }

    @Test
    fun aValueGivenFieldsAndAnEmptyBlockAreEachRefused() {
        assertEquals(listOf("summary"), refusal(FieldSelection { field("summary") { field("text") } }).path)
        assertEquals(listOf("summary"), refusal(FieldSelection { field("summary") {} }).path)
        assertEquals(listOf("routes"), refusal(FieldSelection { field("routes") {} }).path)
        assertEquals(
            listOf("routes", "analysis", "advisories"),
            refusal(FieldSelection { field("routes") { field("analysis") { field("advisories") {} } } }).path,
        )
    }

    @Test
    fun aSelectionThatNamesNoFieldIsRefusedAsAWhole() {
        assertEquals(emptyList(), refusal(FieldSelection {}).path)
    }
}
