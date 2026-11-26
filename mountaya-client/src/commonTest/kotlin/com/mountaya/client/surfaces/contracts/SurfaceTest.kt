package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals

class SurfaceTest {
    @Test
    fun theContractsThreePublicSurfaces() {
        assertEquals(listOf("Rest", "Tiles", "Data"), Surface.entries.map { it.name })
    }

    @Test
    fun eachSurfaceIsNamedAndPrintedAsTheContractNamesIt() {
        assertEquals(listOf("rest", "tiles", "data"), Surface.entries.map { it.value })
        assertEquals("rest", Surface.Rest.toString())
    }
}
