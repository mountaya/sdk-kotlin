package com.mountaya.geo

import kotlin.test.Test
import kotlin.test.assertEquals

class ClimbCategoryTest {
    @Test
    fun theCategoriesRunInAscendingSeverityEachFromItsScore() {
        assertEquals(listOf("4", "3", "2", "1", "hc"), ClimbCategory.entries.map { it.id })
        assertEquals(
            listOf(8_000.0, 16_000.0, 32_000.0, 64_000.0, 80_000.0),
            ClimbCategory.entries.map { it.minimumScore },
        )
    }
}
