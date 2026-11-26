package com.mountaya.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LocationExceptionTest {
    private val kinds =
        listOf(
            LocationException.Kind.NoProvider,
            LocationException.Kind.PermissionRevoked,
            LocationException.Kind.ProviderDisabled,
            LocationException.Kind.PlatformFailed,
            LocationException.Kind.BackgroundUndeclared,
            LocationException.Kind.AudioUndeclared,
        )

    @Test
    fun aFailureCarriesItsKindAndWhatThePlatformThrew() {
        val revoked = SecurityException("revoked")
        val failure = LocationException(LocationException.Kind.PermissionRevoked, revoked)

        assertEquals(LocationException.Kind.PermissionRevoked, failure.kind)
        assertSame(revoked, failure.cause)
        assertNull(LocationException(LocationException.Kind.NoProvider).cause)
        assertEquals("providerDisabled", LocationException.Kind.ProviderDisabled.toString())
    }

    /** The kinds are one set under one spelling in every SDK, as a log names each. */
    @Test
    fun eachKindIsNamedAsTheOtherSdksNameIt() {
        assertEquals(
            listOf(
                "noProvider",
                "permissionRevoked",
                "providerDisabled",
                "platformFailed",
                "backgroundUndeclared",
                "audioUndeclared",
            ),
            kinds.map { it.value },
        )
    }

    /** A message ends on what to do, for a developer reading a log. */
    @Test
    fun eachMessageSaysWhatToDo() {
        val messages = kinds.map { LocationException(it).message.orEmpty() }

        assertTrue(messages[0].contains("LocationSource"))
        assertTrue(messages[1].contains("recheckPermission()"))
        assertTrue(messages[2].contains("settings"))
        assertTrue(messages[3].contains("recheckPermission()"))
        assertTrue(messages[4].contains("ACCESS_BACKGROUND_LOCATION"))
        assertTrue(messages[5].contains("UIBackgroundModes"))
        assertEquals(kinds.size, messages.toSet().size)
    }
}
