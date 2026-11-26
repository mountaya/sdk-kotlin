package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NamedExceptionTest {
    /** A failure a class of another module's rebuilt, declaring the name it was raised under. */
    private class Rebuilt(
        override val name: String,
    ) : Exception(name),
        NamedException

    @Test
    fun everyFailureTheClientReportsDeclaresItsName() {
        val reported: NamedException = ConnectionException()

        assertEquals("ConnectionError", reported.name)
    }

    @Test
    fun aFailureOfAnotherClassThatDeclaresAFamilyNameIsRecognisedByIt() {
        assertTrue(isApiException(Rebuilt("ConflictError")))
        assertTrue(isMountayaException(Rebuilt("ConfigurationError")))
    }
}
