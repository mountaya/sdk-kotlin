package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class OperationDeclarationTest {
    @Test
    fun aFactLeftOutIsTheOneNoOperationStatesByDefault() {
        val declaration = OperationDeclaration("readThing", "GET")

        assertEquals(Surface.Rest, declaration.surface)
        assertFalse(declaration.idempotent)
        assertNull(declaration.timeout)
        assertFalse(declaration.declaresIdempotencyKey)
        assertFalse(declaration.isConditional)
        assertEquals(CredentialDeclaration(), declaration.credentials)
    }

    @Test
    fun twoDeclarationsAreEqualWhenEveryFactIs() {
        assertEquals(OperationDeclaration("readThing", "GET"), OperationDeclaration("readThing", "GET"))
        assertNotEquals(
            OperationDeclaration("readThing", "GET"),
            OperationDeclaration("readThing", "GET", idempotent = true),
        )
        assertEquals("OperationDeclaration(rest readThing: GET)", OperationDeclaration("readThing", "GET").toString())
    }
}
