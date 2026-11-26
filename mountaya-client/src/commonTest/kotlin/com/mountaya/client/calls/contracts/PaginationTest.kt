package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PaginationTest {
    @Test
    fun aPaginationIsItsSortsAndItsDefault() {
        val paged = Pagination(listOf("name", "-name"), "name")

        assertEquals(Pagination(listOf("name", "-name"), "name"), paged)
        assertEquals(Pagination(listOf("name", "-name"), "name").hashCode(), paged.hashCode())
        assertNotEquals(Pagination(listOf("-name", "name"), "name"), paged)
        assertNotEquals(Pagination(emptyList(), null), paged)
        assertEquals("Pagination(sort=[name, -name], defaultSort=name)", paged.toString())
    }
}
