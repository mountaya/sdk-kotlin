package com.mountaya.conformance

import com.mountaya.conformance.contract.generated.EmbeddedContract
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RegistryEntryTest {
    @Test
    fun everyEmbeddedEntryCarriesItsFixture() {
        if (!EmbeddedContract.PRESENT) return

        assertTrue(EmbeddedContract.entries.isNotEmpty())
        for (entry in EmbeddedContract.entries) {
            assertNotNull(
                EmbeddedContract.fixture(entry.fixture),
                "${entry.id} names ${entry.fixture}, which was not embedded",
            )
        }
    }

    @Test
    fun anEntryKeepsWhatTheRegistrySays() {
        val entry = RegistryEntry("transport.base-urls", "transport", true, "transport/base-urls.json")

        assertEquals(
            listOf("transport.base-urls", "transport", "true", "transport/base-urls.json"),
            listOf(entry.id, entry.layer, entry.api.toString(), entry.fixture),
        )
    }
}
