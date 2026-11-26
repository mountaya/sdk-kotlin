package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GradeAtTest {
    private val bend = samples(0.0 to 1_000.0, 40.0 to 1_000.0, 240.0 to 1_040.0, 440.0 to 1_000.0)

    @Test
    fun aSteadyClimbReadsItsSlopeADescentReadsNegative() {
        val climb = samples(0.0 to 1_000.0, 200.0 to 1_020.0, 400.0 to 1_040.0, 600.0 to 1_060.0)

        assertEquals(5.710593137499643, assertNotNull(gradeAt(climb, 300.0), "a grade is read at the distance"), 1e-9)
        assertEquals(-11.309932474020215, assertNotNull(gradeAt(bend, 400.0), "a grade is read at the distance"), 1e-9)
    }

    @Test
    fun aWindowStraddlingABendReadsAcrossIt() {
        assertEquals(0.0, gradeAt(bend, 240.0))
    }

    @Test
    fun nearEitherEndTheWindowIsCutToTheCurve() {
        assertEquals(3.8140748342903543, assertNotNull(gradeAt(bend, 10.0), "a grade is read at the distance"), 1e-9)
        assertEquals(-11.30993247402272, assertNotNull(gradeAt(bend, 489.0), "a grade is read at the distance"), 1e-9)
    }

    @Test
    fun aWindowHoldingNoLengthOfTheCurveReadsNone() {
        assertNull(gradeAt(bend, 490.0))
        assertNull(gradeAt(bend, -500.0))
        assertNull(gradeAt(samples(0.0 to 1_000.0), 0.0))
        assertNull(gradeAt(emptyList(), 0.0))
        assertNull(gradeAt(bend, Double.NaN))
    }
}
