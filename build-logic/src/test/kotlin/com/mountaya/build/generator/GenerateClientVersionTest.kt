package com.mountaya.build.generator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GenerateClientVersionTest {
    @Test
    fun theVersionIsPrintedAsAnInternalConstantOfThePackageItIsGiven() {
        assertEquals(
            """
            // GENERATED FILE. DO NOT EDIT.
            //
            // Printed by `generateClientVersion` from `mountaya` in gradle/libs.versions.toml,
            // which every build prints again.

            package com.mountaya.client

            /** The version every module of this SDK is published at, the catalog's `mountaya`. */
            internal const val CLIENT_VERSION: String = "0.1.0"

            """.trimIndent(),
            GenerateClientVersion.source("com.mountaya.client", "0.1.0"),
        )
    }

    @Test
    fun aPrereleaseIsPrintedAsTheCatalogWritesIt() {
        val printed = GenerateClientVersion.source("com.example", "1.2.0-rc.1+build.7")

        assertTrue(printed.endsWith("internal const val CLIENT_VERSION: String = \"1.2.0-rc.1+build.7\"\n"), printed)
    }

    /** The version rides every request in a header, so one no header carries is refused at the build. */
    @Test
    fun aVersionNoHeaderCarriesIsRefused() {
        for (version in listOf("", " 0.1.0", "0.1.0\n", "0.1 0", "\"0.1.0\"", "0.1.0/2")) {
            val error =
                assertFailsWith<IllegalArgumentException> { GenerateClientVersion.source("com.example", version) }

            assertTrue(error.message.orEmpty().contains("no header carries"), version)
        }
    }
}
