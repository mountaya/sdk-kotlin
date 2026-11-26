package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CredentialDeclarationTest {
    @Test
    fun aFactLeftOutIsFalseAndAnActingUserLeftOutIsNone() {
        val declaration = CredentialDeclaration()

        assertEquals(CredentialDeclaration.ActingUser.None, declaration.actingUser)
        assertFalse(declaration.acceptsShareToken)
        assertFalse(declaration.requiresSession)
        assertFalse(declaration.requiresSecretKey)
        assertFalse(declaration.takesNoCredential)
        assertFalse(declaration.pendingInvitee)
    }

    @Test
    fun eachFactIsSetOnItsOwn() {
        val declaration =
            CredentialDeclaration(actingUser = CredentialDeclaration.ActingUser.Required, requiresSession = true)

        assertEquals(CredentialDeclaration.ActingUser.Required, declaration.actingUser)
        assertTrue(declaration.requiresSession)
        assertFalse(declaration.acceptsShareToken)
        assertNotEquals(CredentialDeclaration(actingUser = CredentialDeclaration.ActingUser.Required), declaration)
        assertEquals(
            CredentialDeclaration(actingUser = CredentialDeclaration.ActingUser.Required, requiresSession = true),
            declaration,
        )
    }

    @Test
    fun anActingUserIsSpelledAsTheContractSpellsIt() {
        assertEquals(
            listOf("required", "optional", "none"),
            listOf(
                CredentialDeclaration.ActingUser.Required,
                CredentialDeclaration.ActingUser.Optional,
                CredentialDeclaration.ActingUser.None,
            ).map { it.toString() },
        )
    }

    @Test
    fun noTwoSetsOfFactsShareAHash() {
        val hashes =
            listOf(
                CredentialDeclaration(),
                CredentialDeclaration(actingUser = CredentialDeclaration.ActingUser.Required),
                CredentialDeclaration(actingUser = CredentialDeclaration.ActingUser.Optional),
                CredentialDeclaration(acceptsShareToken = true),
                CredentialDeclaration(requiresSession = true),
                CredentialDeclaration(requiresSecretKey = true),
                CredentialDeclaration(takesNoCredential = true),
                CredentialDeclaration(pendingInvitee = true),
            ).map { it.hashCode() }

        assertEquals(hashes.size, hashes.toSet().size)
    }
}
