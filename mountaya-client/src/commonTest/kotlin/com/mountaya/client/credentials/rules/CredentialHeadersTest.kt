package com.mountaya.client

import com.mountaya.client.CredentialDeclaration.ActingUser
import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class CredentialHeadersTest {
    private fun session(user: String?): SessionToken =
        SessionToken.Minted(
            RestSchema.Session(
                expires_at = Instant.fromEpochSeconds(1_800_000_000),
                token = "sess_abc",
                user_id = user,
            ),
        )

    private val member = session("7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1")
    private val nobody = session(null)
    private val actsAsUser = CredentialDeclaration(actingUser = ActingUser.Required, requiresSession = true)

    /** The refusal's message, or null where the headers were sent. */
    private fun refusal(
        credential: Credential,
        declaration: CredentialDeclaration = CredentialDeclaration(),
        language: String? = null,
        method: String = "GET",
    ): String? =
        try {
            CredentialHeaders.headers(credential, language, declaration, method)
            null
        } catch (refusal: ConfigurationException) {
            refusal.message.orEmpty()
        }

    @Test
    fun aPublishableKeySendsItsKeyAndItsSessionTokenAndNoHeaderNamesTheUser() {
        val headers = CredentialHeaders.headers(Credential.Publishable("pk_test_abc", member), null, actsAsUser, "GET")

        assertEquals(
            mapOf("X-API-Key" to "pk_test_abc", "X-Session-Token" to "sess_abc", "Accept-Language" to "*"),
            headers,
        )
    }

    @Test
    fun theLanguageRidesEveryRequestAnOperationThatTakesNoCredentialIncluded() {
        val none = CredentialDeclaration(takesNoCredential = true)

        assertEquals(
            mapOf("Accept-Language" to "fr"),
            CredentialHeaders.headers(Credential.Secret("sk_test_abc"), "fr", none, "GET"),
        )
        assertEquals(
            mapOf("Accept-Language" to "*"),
            CredentialHeaders.headers(Credential.Share("share_abc"), null, none, "GET"),
        )
        assertEquals(
            mapOf("X-Share-Token" to "share_abc", "Accept-Language" to "fr-CH, fr;q=0.9"),
            CredentialHeaders.headers(
                Credential.Share("share_abc"),
                "fr-CH, fr;q=0.9",
                CredentialDeclaration(acceptsShareToken = true),
                "GET",
            ),
        )
    }

    /**
     * RFC 9110 reads `*` as no language in particular, which the projection writes itself rather than
     * leaving the platform's loader to write the device's languages.
     */
    @Test
    fun aClientGivenNoLanguageAsksForNoneInParticular() {
        for (declaration in listOf(CredentialDeclaration(), CredentialDeclaration(takesNoCredential = true))) {
            assertEquals(
                "*",
                CredentialHeaders.headers(
                    Credential.Publishable("pk_test_abc"),
                    null,
                    declaration,
                    "GET",
                )["Accept-Language"],
            )
        }
    }

    @Test
    fun aShareTokenThatAsksForCookiesReadsAloneAndIsRefusedOnAWrite() {
        val visitor = Credential.Share("share_abc") { "visitor=7" }

        assertEquals(
            mapOf("Accept-Language" to "*"),
            CredentialHeaders.headers(visitor, null, CredentialDeclaration(), "GET"),
        )
        assertEquals(
            mapOf("Accept-Language" to "*"),
            CredentialHeaders.headers(visitor, null, CredentialDeclaration(), "HEAD"),
        )
        for (write in listOf("POST", "PATCH", "PUT", "DELETE", "TRACE", "GETS")) {
            assertTrue(refusal(visitor, method = write).orEmpty().contains("writes nothing"), write)
        }
    }

    /**
     * A read is a request sent with GET, HEAD or OPTIONS in any letter case, or one of an operation declared
     * a read whatever its method, as every Data API query is over its POST.
     */
    @Test
    fun aShareTokenThatAsksForCookiesReadsWithAnyReadMethodInAnyLetterCaseAndADeclaredReadOverPost() {
        val visitor = Credential.Share("share_abc") { "visitor=7" }

        for (read in listOf("OPTIONS", "get", "Head", "options")) {
            assertEquals(
                mapOf("Accept-Language" to "*"),
                CredentialHeaders.headers(visitor, null, CredentialDeclaration(), read),
                read,
            )
        }
        assertEquals(
            mapOf("Accept-Language" to "*"),
            CredentialHeaders.headers(visitor, null, CredentialDeclaration(read = true), "POST"),
        )
        assertTrue(CredentialHeaders.isRead("pOsT", CredentialDeclaration(read = true)))
        assertTrue(!CredentialHeaders.isRead("POST", CredentialDeclaration()))
        // The Turkish dotless i is no ASCII letter, so `get` spelled with one is no read.
        assertTrue(!CredentialHeaders.isRead("GET".replace('E', '\u0130'), CredentialDeclaration()))
    }

    /**
     * A session the visitor's cookies hold is invisible to the client and plain to the server, so a read under a
     * token that asks for cookies goes out on them even where the operation requires a session token; a write
     * stays refused, and a token that asks for none is refused as before.
     */
    @Test
    fun aReadThatRequiresASessionGoesOutOnTheVisitorsCookiesAndAWriteIsRefused() {
        val visitor = Credential.Share("share_abc") { "visitor=7" }
        val requiresSession = CredentialDeclaration(requiresSession = true)

        assertEquals(mapOf("Accept-Language" to "*"), CredentialHeaders.headers(visitor, null, requiresSession, "GET"))
        assertEquals(
            mapOf("Accept-Language" to "*"),
            CredentialHeaders.headers(
                visitor,
                null,
                CredentialDeclaration(requiresSession = true, read = true),
                "POST",
            ),
        )
        assertTrue(refusal(visitor, requiresSession, method = "POST").orEmpty().contains("requires a session token"))
        assertTrue(
            refusal(Credential.Share("share_abc"), requiresSession).orEmpty().contains("requires a session token"),
        )
    }

    @Test
    fun aLanguageThatIsMissingOrThatNoHeaderCarriesIsRefusedEvenWhereTheOperationTakesNone() {
        val none = CredentialDeclaration(takesNoCredential = true)
        val key = Credential.Publishable("pk_test_abc")

        for (language in listOf("", " ", "f\nr", "frā", "fr\u0085")) {
            assertTrue(refusal(key, none, language).orEmpty().startsWith("The language"), language)
        }
        assertTrue(refusal(key, none, "").orEmpty().contains("empty"))
        assertTrue(refusal(key, none, "f\tr").orEmpty().contains("control character"))
    }

    @Test
    fun aSecretKeyAuthenticatesOnlyTheOperationThatDeclaresIt() {
        val mint = CredentialDeclaration(requiresSecretKey = true)
        val secret = Credential.Secret("sk_test_abc")

        assertEquals(
            mapOf("X-API-Key" to "sk_test_abc", "Accept-Language" to "*"),
            CredentialHeaders.headers(secret, null, mint, "POST"),
        )
        assertTrue(refusal(secret).orEmpty().contains("authenticates only the operation that mints"))
        assertTrue(refusal(Credential.Publishable("pk_test_abc"), mint).orEmpty().contains("a publishable key cannot"))
        assertTrue(refusal(Credential.Share("share_abc"), mint).orEmpty().contains("a share token cannot"))
    }

    @Test
    fun aShareTokenReachesOnlyAnOperationThatDeclaresOneAndNeverASessionOne() {
        val share = Credential.Share("share_abc")

        assertEquals(
            mapOf("X-Share-Token" to "share_abc", "Accept-Language" to "*"),
            CredentialHeaders.headers(share, null, CredentialDeclaration(acceptsShareToken = true), "GET"),
        )
        assertTrue(refusal(share).orEmpty().contains("does not take one"))
        assertTrue(
            refusal(share, CredentialDeclaration(acceptsShareToken = true, requiresSession = true))
                .orEmpty()
                .contains("requires a session token"),
        )
    }

    @Test
    fun anOperationActingAsAUserIsRefusedUnderATokenASourceMintedForNone() {
        val refused = refusal(Credential.Publishable("pk_test_abc", nobody), actsAsUser)

        assertTrue(refused.orEmpty().contains("minted without user_id"))
    }

    @Test
    fun aTokenWhoseUserTheClientIsNotToldIsSentForTheApiToDecide() {
        assertNull(refusal(Credential.Publishable("pk_test_abc", SessionToken.Value("sess_abc")), actsAsUser))
    }

    @Test
    fun anOptionalUserAndNoneAreSentWhoeverTheTokenNames() {
        for (actingUser in listOf(ActingUser.Optional, ActingUser.None)) {
            for (token in listOf(member, nobody)) {
                val declaration = CredentialDeclaration(actingUser = actingUser, requiresSession = true)
                assertNull(refusal(Credential.Publishable("pk_test_abc", token), declaration), "$actingUser $token")
            }
        }
    }

    /**
     * The acting user is what a token names: with no token at all, a required user is refused for
     * the session the operation requires, and nothing is refused on the acting user alone.
     */
    @Test
    fun aRequiredUserWithNoSessionTokenAtAllIsRefusedForTheSessionItRequires() {
        val key = Credential.Publishable("pk_test_abc")

        assertTrue(refusal(key, actsAsUser).orEmpty().contains("requires a session token"))
        assertNull(refusal(key, CredentialDeclaration(actingUser = ActingUser.Required)))
    }

    @Test
    fun aWrongPrefixIsNamedByTheKindItDoesHave() {
        val refused = runCatching { Credential.Share("pk_test_abc") }.exceptionOrNull()?.message.orEmpty()

        assertTrue(refused.contains("which is a publishable key's"))
    }

    @Test
    fun theHeadersAreReachedThroughTheCredentialItself() {
        assertEquals(
            mapOf("X-API-Key" to "pk_test_abc", "Accept-Language" to "de"),
            Credential.Publishable("pk_test_abc").headers(CredentialDeclaration(), language = "de"),
        )
        assertTrue(
            runCatching {
                Credential.Share("share_abc") { "visitor=7" }.headers(CredentialDeclaration(), method = "POST")
            }.exceptionOrNull() is ConfigurationException,
        )
    }

    @Test
    fun aMemberThatIsEmptyOrWhitespaceAloneIsMissingWhicheverMemberItIs() {
        for (blank in listOf("", " ", "   ", "\t")) {
            assertTrue(
                runCatching { Credential.Publishable("pk_test_abc", SessionToken.Value(blank)) }
                    .exceptionOrNull()
                    ?.message
                    .orEmpty()
                    .startsWith("The session token is empty"),
            )
            assertTrue(
                runCatching {
                    Credential.Share(blank)
                }.exceptionOrNull()?.message.orEmpty().startsWith("The share token is empty"),
            )
            assertTrue(
                runCatching {
                    Credential.Secret(blank)
                }.exceptionOrNull()?.message.orEmpty().startsWith("The secret key is empty"),
            )
        }
    }

    /**
     * Latin-1 among them, which no header carries in any SDK and the platform's HTTP stack does not send
     * (`sdk/SPEC.md` sections 1 and 13).
     */
    @Test
    fun aMemberOrALanguageHoldingAControlCharacterOrACharacterTheStackDoesNotSendIsRefused() {
        for (bad in listOf("\r\n", "\u007F", "\uD83C\uDFD4", "\u00e9", "\u00ff")) {
            assertTrue(
                runCatching {
                    Credential.Publishable("pk_test_$bad")
                }.exceptionOrNull()?.message.orEmpty().contains("does not send"),
            )
            assertTrue(
                refusal(
                    Credential.Publishable("pk_test_abc"),
                    language = "fr$bad",
                ).orEmpty().contains("does not send"),
            )
        }
    }

    @Test
    fun printableAsciiTheSpaceIncludedIsCarried() {
        val headers =
            CredentialHeaders.headers(
                Credential.Publishable("pk_test_e ~"),
                "fr-CH, fr;q=0.9",
                CredentialDeclaration(),
                "GET",
            )

        assertEquals(mapOf("X-API-Key" to "pk_test_e ~", "Accept-Language" to "fr-CH, fr;q=0.9"), headers)
    }

    @Test
    fun aSecretKeyIsRefusedWhereTheOperationRequiresASessionAndAMintedTokenIsHeldToItsPrefix() {
        assertTrue(refusal(Credential.Secret("sk_test_abc"), CredentialDeclaration(requiresSession = true)) != null)
        val wrong =
            SessionToken.Minted(
                RestSchema.Session(expires_at = Instant.fromEpochSeconds(0), token = "tok_abc", user_id = null),
            )
        assertTrue(
            runCatching { Credential.Publishable("pk_test_abc", wrong) }.exceptionOrNull() is ConfigurationException,
        )
    }

    /**
     * A visitor's cookie is held to what a header carries where it is read for a map's request, and the refusal
     * never writes the visitor's session out.
     */
    @Test
    fun aVisitorsCookieNoHeaderCarriesIsRefusedItsValueNeverWrittenOut() {
        CredentialHeaders.checkCookie("session=visitor; theme=dark")
        for (cookie in listOf("session=caf\u00e9", "session=a\r\nX-Evil: 1", "session=\u007f")) {
            val refusal = assertFailsWith<ConfigurationException>(cookie) { CredentialHeaders.checkCookie(cookie) }
            assertTrue("session=" !in refusal.message.orEmpty(), refusal.message)
        }
    }
}
