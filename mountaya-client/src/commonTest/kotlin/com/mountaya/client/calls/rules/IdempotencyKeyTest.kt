package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.uuid.Uuid

class IdempotencyKeyTest {
    private val uuid = Uuid.parse("6F1C2A9E-3B4D-4E5F-8A6B-7C8D9E0F1A2B")

    @Test
    fun aDeclaredKeyIsTheCallersAsGivenElseOneDrawnInLowercase() {
        assertEquals("order-7f3a:1", IdempotencyKey.key(true, "order-7f3a:1") { null })
        assertEquals("6f1c2a9e-3b4d-4e5f-8a6b-7c8d9e0f1a2b", IdempotencyKey.key(true, null) { uuid })
    }

    @Test
    fun noKeyIsSentWhereNoneIsDeclaredNorDrawnWhereNoCryptographicSourceIs() {
        assertNull(IdempotencyKey.key(false, "order\n7f3a") { uuid })
        assertNull(IdempotencyKey.key(true, null) { null })
    }

    @Test
    fun aKeyNoHeaderCanCarryIsRefusedTheEmptyOneIncluded() {
        for (key in listOf("", "order\n7f3a", "ordre-№7")) {
            assertFailsWith<ConfigurationException> { IdempotencyKey.key(true, key) { uuid } }
        }
    }
}
