package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class CullCrowdedLabelsTest {
    private fun box(
        key: String,
        left: Double,
        right: Double,
    ) = AxisLabelBox(key, left, right)

    @Test
    fun theGutterIsEight() {
        assertEquals(8.0, AXIS_LABEL_GUTTER)
    }

    @Test
    fun everyLabelClearOfItsNeighboursIsDrawnTheFirstAndLastWhateverTheyOverlap() {
        assertEquals(
            emptyList(),
            cullCrowdedLabels(listOf(box("a", 0.0, 60.0), box("b", 200.0, 260.0), box("c", 400.0, 460.0))),
        )
        assertEquals(emptyList(), cullCrowdedLabels(listOf(box("start", 0.0, 60.0), box("finish", 10.0, 70.0))))
        assertEquals(emptyList(), cullCrowdedLabels(listOf(box("only", 0.0, 60.0))))
        assertEquals(emptyList(), cullCrowdedLabels(emptyList()))
    }

    @Test
    fun aLabelMeetingTheLastOrTheOneDrawnBeforeItIsLeftOut() {
        assertEquals(
            listOf("b"),
            cullCrowdedLabels(listOf(box("a", 0.0, 60.0), box("b", 100.0, 160.0), box("end", 150.0, 210.0))),
        )
        assertEquals(
            listOf("b"),
            cullCrowdedLabels(
                listOf(
                    box("a", 0.0, 60.0),
                    box("b", 50.0, 110.0),
                    box("c", 200.0, 260.0),
                    box("end", 400.0, 460.0),
                ),
            ),
        )
        assertEquals(
            listOf("b"),
            cullCrowdedLabels(
                listOf(
                    box("a", 0.0, 60.0),
                    box("b", 50.0, 110.0),
                    box("c", 68.0, 128.0),
                    box("end", 400.0, 460.0),
                ),
            ),
        )
    }

    @Test
    fun aLabelExactlyAGutterAwayIsDrawnAPixelShortOfItIsNot() {
        assertEquals(
            emptyList(),
            cullCrowdedLabels(listOf(box("a", 0.0, 60.0), box("b", 68.0, 128.0), box("end", 136.0, 196.0))),
        )
        assertEquals(
            listOf("b"),
            cullCrowdedLabels(listOf(box("a", 0.0, 60.0), box("b", 67.0, 127.0), box("end", 400.0, 460.0))),
        )
    }

    @Test
    fun bothEndsOfALoopAreDrawnAndTheirDuplicatesLeftOutAKeyStatedOnce() {
        assertEquals(
            listOf("finish#0", "finish#1"),
            cullCrowdedLabels(
                listOf(
                    box("start#0", 0.0, 60.0),
                    box("finish#0", 0.0, 60.0),
                    box("finish#1", 900.0, 960.0),
                    box("start#1", 900.0, 960.0),
                ),
            ),
        )
        assertEquals(
            listOf("b"),
            cullCrowdedLabels(
                listOf(box("a", 0.0, 60.0), box("b", 1.0, 2.0), box("b", 3.0, 4.0), box("z", 9.0, 10.0)),
            ),
        )
    }
}
