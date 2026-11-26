package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BaseUrlsTest {
    @Test
    fun eachSurfaceDefaultsToMountayasOwnOrigin() {
        val urls = BaseUrls()

        assertEquals("https://data.mountaya.com", urls.data.toString())
        assertEquals("https://api.mountaya.com", urls.rest.toString())
        assertEquals("https://tiles.mountaya.com", urls.tiles.toString())
        assertEquals(BaseUrls.standard, urls)
    }

    @Test
    fun overridingOneSurfaceLeavesTheOthersWhereTheyWere() {
        val proxy = Url("https://proxy.example.com/mountaya")

        val data = BaseUrls(data = proxy)
        val rest = BaseUrls(rest = proxy)
        val tiles = BaseUrls(tiles = proxy)

        assertEquals(proxy, data.data)
        assertEquals(BaseUrls.standard.rest, data.rest)
        assertEquals(BaseUrls.standard.tiles, data.tiles)
        assertEquals(proxy, rest.rest)
        assertEquals(BaseUrls.standard.data, rest.data)
        assertEquals(proxy, tiles.tiles)
        assertEquals(BaseUrls.standard.data, tiles.data)
    }

    @Test
    fun twoSetsOfOriginsAreEqualWhenEachOriginIs() {
        val proxy = Url("https://proxy.example.com")

        assertEquals(BaseUrls(data = proxy), BaseUrls(data = Url("https://proxy.example.com")))
        assertEquals(BaseUrls(data = proxy).hashCode(), BaseUrls(data = Url("https://proxy.example.com")).hashCode())
    }

    @Test
    fun anOriginWithNoSchemeIsRefusedWhereItIsGiven() {
        for (origin in listOf("api.example.com", "/v1", "")) {
            assertFailsWith<ConfigurationException>(origin) { BaseUrls(rest = Url(origin)) }
            assertFailsWith<ConfigurationException>(origin) { BaseUrls(data = Url(origin)) }
            assertFailsWith<ConfigurationException>(origin) { BaseUrls(tiles = Url(origin)) }
        }
    }
}
