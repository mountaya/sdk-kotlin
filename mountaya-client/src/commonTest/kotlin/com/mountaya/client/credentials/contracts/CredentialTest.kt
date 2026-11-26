package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class CredentialTest {
    private val session = SessionToken.Value("sess_abc")

    private val minted =
        SessionToken.Minted(
            RestSchema.Session(
                expires_at = Instant.fromEpochSeconds(1_800_000_000),
                token = "sess_abc",
                user_id = "7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1",
            ),
        )

    @Test
    fun aShareTokenReplacesAKeyBesideItAndTheKeysCompanionGoesWithIt() {
        val credential = Credential(publishableKey = "pk_test_abc", sessionToken = session, shareToken = "share_abc")

        assertEquals(Credential.Share("share_abc"), credential)
    }

    /**
     * A share token wins over every other part, an empty one included: it is refused as a missing
     * share token, never read as a fall back to the key beside it.
     */
    @Test
    fun anEmptyShareTokenBesideAKeyIsAMissingShareToken() {
        val refusal =
            assertFailsWith<ConfigurationException> { Credential(publishableKey = "pk_test_abc", shareToken = "") }

        assertTrue(refusal.message.orEmpty().startsWith("The share token is empty"))
    }

    @Test
    fun aPublishableKeyCarriesItsSessionToken() {
        assertEquals(
            Credential.Publishable("pk_test_abc", session),
            Credential(publishableKey = "pk_test_abc", sessionToken = session),
        )
        assertEquals(
            Credential.Publishable("pk_test_abc", minted),
            Credential(publishableKey = "pk_test_abc", sessionToken = minted),
        )
    }

    @Test
    fun aPositionalKeyIsThePublishableKey() {
        assertEquals(Credential.Publishable("pk_test_abc"), Credential("pk_test_abc"))
    }

    @Test
    fun partsThatMakeNoCredentialAreRefusedEachSayingWhatIsMissing() {
        assertFailsWith<ConfigurationException> { Credential() }
        val sessionAlone = assertFailsWith<ConfigurationException> { Credential(sessionToken = session) }
        assertTrue(sessionAlone.message.orEmpty().contains("accompanies a publishable key"))
    }

    @Test
    fun aPartWithTheWrongPrefixIsRefusedAtConstructionWithoutQuotingIt() {
        val refusal = assertFailsWith<ConfigurationException> { Credential(publishableKey = "sk_live_0123456789") }

        assertTrue(refusal.message.orEmpty().contains("`sk_`"))
        assertFalse(refusal.message.orEmpty().contains("0123456789"))
    }

    @Test
    fun aPartThatIsMissingOrThatNoHeaderCarriesIsRefusedAtConstruction() {
        for (key in listOf("", "   ", "pk_test\nabc", "pk_test_ābc", "pk_test\u0000")) {
            assertFailsWith<ConfigurationException>(key) { Credential.Publishable(key) }
        }
        assertFailsWith<ConfigurationException> { Credential.Publishable("pk_test_abc", SessionToken.Value("tok_abc")) }
        assertFailsWith<ConfigurationException> { Credential.Publishable("pk_test_abc", SessionToken.Value(" ")) }
        assertFailsWith<ConfigurationException> { Credential.Share("tok_abc") }
        assertFailsWith<ConfigurationException> { Credential.Share("share_a\tb") }
        assertFailsWith<ConfigurationException> { Credential.Secret("pk_test_abc") }
    }

    /**
     * The key is checked before the session token beside it, and each member is missing before it
     * is uncarried, and uncarried before its prefix is read.
     */
    @Test
    fun theKeyIsCheckedBeforeItsSessionTokenAndAMissingMemberBeforeAWrongOne() {
        val both = assertFailsWith<ConfigurationException> { Credential.Publishable("", SessionToken.Value("tok")) }
        val empty = assertFailsWith<ConfigurationException> { Credential.Publishable("") }
        val uncarried = assertFailsWith<ConfigurationException> { Credential.Publishable("zz\n") }

        assertTrue(both.message.orEmpty().startsWith("The publishable key is empty"))
        assertTrue(empty.message.orEmpty().contains("missing"))
        assertTrue(uncarried.message.orEmpty().contains("control character"))
    }

    @Test
    fun twoCredentialsAreEqualWhenTheirShapeAndPartsAre() {
        assertEquals(Credential.Publishable("pk_test_abc", session), Credential.Publishable("pk_test_abc", session))
        assertEquals(
            Credential.Publishable("pk_test_abc", session).hashCode(),
            Credential.Publishable("pk_test_abc", session).hashCode(),
        )
        assertNotEquals<Credential>(
            Credential.Publishable("pk_test_abc"),
            Credential.Publishable("pk_test_abc", session),
        )
        assertNotEquals<Credential>(
            Credential.Publishable("pk_test_abc", minted),
            Credential.Publishable("pk_test_abc", session),
        )
        assertEquals(Credential.Secret("sk_test_abc"), Credential.Secret("sk_test_abc"))
        assertNotEquals<Credential>(Credential.Share("share_abc"), Credential.Publishable("pk_test_abc"))
    }

    @Test
    fun aCredentialPrintsEachKeyAndTokenByItsPrefixAloneHoweverTheTokenIsHeld() {
        assertEquals(
            "Credential.Publishable(key=pk_…, sessionToken=sess_…)",
            Credential.Publishable("pk_test_abc", session).toString(),
        )
        assertEquals(
            "Credential.Publishable(key=pk_…, sessionToken=sess_…)",
            Credential.Publishable("pk_test_abc", minted).toString(),
        )
        assertEquals("Credential.Publishable(key=pk_…)", Credential.Publishable("pk_test_abc").toString())
        assertEquals("Credential.Secret(key=sk_…)", Credential.Secret("sk_test_abc").toString())
        assertEquals("Credential.Share(token=share_…)", Credential.Share("share_abc").toString())
    }

    @Test
    fun aShareTokenThatAsksForCookiesSaysSoAndATokenReadPerRequestIsCheckedAtItsRead() {
        val cookies: CookieSource = { "session=visitor" }

        assertEquals("Credential.Share(token=share_…, cookies)", Credential.Share("share_abc", cookies).toString())
        assertEquals(Credential.Share("share_abc", cookies), Credential.Share("share_abc", cookies))
        assertEquals(
            "Credential.Publishable(key=pk_…, sessionToken=function)",
            Credential.Publishable("pk_test_abc", SessionToken.Function { "tok_wrong" }).toString(),
        )
    }
}
