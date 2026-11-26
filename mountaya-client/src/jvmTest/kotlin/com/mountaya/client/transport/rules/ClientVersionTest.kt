package com.mountaya.client

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The version the identification header names is the version catalog's `mountaya`, the one every
 * module is published at, read here from the catalog itself as the release script reads it.
 */
class ClientVersionTest {
    @Test
    fun theVersionEveryRequestNamesIsTheOneTheCatalogPublishesEveryModuleAt() {
        val catalog = System.getProperty("mountaya.catalog")?.let(::File)
        assertNotNull(catalog, "The build hands the test the version catalog as `mountaya.catalog`.")
        val published =
            catalog
                .readLines()
                .firstNotNullOfOrNull { Regex("""^mountaya = "(.*)"$""").matchEntire(it)?.groupValues?.get(1) }

        assertEquals(published, CLIENT_VERSION)
        assertEquals("mountaya-kotlin/$published", ClientIdentity.value)
    }
}
