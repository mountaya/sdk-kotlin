package com.mountaya.build.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmbeddedContractTest {
    @Test
    fun anAbsentCheckoutEmbedsAnEmptyContractThatSaysSo() {
        val source = EmbeddedContract.render(null, emptyMap())

        assertTrue(source.contains("const val PRESENT: Boolean = false"))
        assertTrue(source.contains("const val REGISTRY_VERSION: Int = 0"))
        assertTrue(source.contains("        listOf(\n        )"))
    }

    @Test
    fun theRegistryAndEachFixtureAreEmbedded() {
        val registry = ContractRegistry(6, sampleRegistry.capabilities.take(1))
        val source = EmbeddedContract.render(registry, mapOf("transport/retry-backoff.json" to "{\"cases\":[]}"))

        assertTrue(source.contains("const val PRESENT: Boolean = true"))
        assertTrue(source.contains("const val REGISTRY_VERSION: Int = 6"))
        assertTrue(
            source.contains(
                "RegistryEntry(\"transport.retry.backoff\", \"transport\", false, \"transport/retry-backoff.json\"),",
            ),
        )
        assertTrue(source.contains("\"transport/retry-backoff.json\" -> fixture0()"))
        assertTrue(source.contains("append(\"{\\\"cases\\\":[]}\")"))
    }

    @Test
    fun aLiteralIsPlainAsciiAndEscapesWhatKotlinWouldRead() {
        assertEquals("\"a\\\"b\\\\c\\\$d\\ne\"", kotlinLiteral("a\"b\\c\$d\ne"))
        assertEquals("\"\\u00e9 \\u00b7 \\ud83d\\ude00\"", kotlinLiteral("é · 😀"))
    }

    @Test
    fun aChunkNeverSplitsASurrogatePair() {
        val text = "ab😀cd"
        val pieces = chunks(text, size = 3)

        assertEquals(text, pieces.joinToString(""))
        assertEquals(listOf("ab", "😀c", "d"), pieces)
    }
}
