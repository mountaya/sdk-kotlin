package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SuccessResponseTest {
    @Test
    fun aSuccessIsItsStatusAndTheHeadersItDeclaresInTheirOrder() {
        val created = SuccessResponse(201, listOf("Location", "X-Request-Id"))

        assertEquals(SuccessResponse(201, listOf("Location", "X-Request-Id")), created)
        assertEquals(SuccessResponse(201, listOf("Location", "X-Request-Id")).hashCode(), created.hashCode())
        assertNotEquals(SuccessResponse(201, listOf("X-Request-Id", "Location")), created)
        assertNotEquals(SuccessResponse(200, listOf("Location", "X-Request-Id")), created)
        assertEquals(emptyList(), SuccessResponse(204).headers)
        assertEquals("SuccessResponse(201, headers=[Location, X-Request-Id])", created.toString())
    }
}
