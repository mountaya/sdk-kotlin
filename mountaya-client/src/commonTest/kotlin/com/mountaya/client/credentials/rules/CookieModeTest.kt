package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CookieModeTest {
    private val cookies: (Url) -> String? = { "session=visitor" }

    @Test
    fun onlyAShareTokenThatAsksForCookiesAsksForThemAndNeverWhereNothingIsTaken() {
        val asking = Credential.Share("share_abc", cookies)

        assertEquals(CookieMode.Include, CookieMode.mode(asking, CredentialDeclaration()))
        assertEquals(CookieMode.Include, CookieMode.mode(asking, CredentialDeclaration(acceptsShareToken = true)))
        assertEquals(CookieMode.Omit, CookieMode.mode(asking, CredentialDeclaration(takesNoCredential = true)))
        assertEquals(
            CookieMode.Omit,
            CookieMode.mode(Credential.Share("share_abc"), CredentialDeclaration(acceptsShareToken = true)),
        )
        assertEquals(
            CookieMode.Omit,
            CookieMode.mode(
                Credential.Publishable("pk_test_abc", SessionToken.Value("sess_abc")),
                CredentialDeclaration(),
            ),
        )
        assertEquals(listOf("include", "omit"), CookieMode.entries.map { it.word })
    }

    @Test
    fun aReadThatTakesNoShareTokenRidesTheVisitorsSessionAloneAndAWriteIsRefused() {
        val asking = Credential.Share("share_abc", cookies)
        val read = CredentialHeaders.project(asking, null, null, CredentialDeclaration(), isRead = true)

        assertEquals(mapOf("Accept-Language" to "*"), read.headers)
        assertNotNull(read.cookies, "the read rides its cookies")
        assertFailsWith<ConfigurationException> {
            CredentialHeaders.project(asking, null, null, CredentialDeclaration(), isRead = false)
        }
    }

    @Test
    fun aReadThatRequiresASessionRidesTheVisitorsSessionAloneAndAWriteOfOneIsRefused() {
        val asking = Credential.Share("share_abc", cookies)
        val requiresSession = CredentialDeclaration(requiresSession = true)
        val read = CredentialHeaders.project(asking, null, null, requiresSession, isRead = true)

        assertEquals(CookieMode.Include, CookieMode.mode(asking, requiresSession))
        assertEquals(mapOf("Accept-Language" to "*"), read.headers)
        assertNotNull(read.cookies, "the read rides its cookies")
        assertFailsWith<ConfigurationException> {
            CredentialHeaders.project(asking, null, null, requiresSession, isRead = false)
        }
    }
}
