package com.mountaya.client

import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SessionTokenTest {
    private fun minted(user: String?) =
        SessionToken.Minted(
            RestSchema.Session(
                expires_at = Instant.fromEpochSeconds(1_800_000_000),
                token = "sess_abc",
                user_id = user,
            ),
        )

    @Test
    fun aTokenIsWhatItSendsWhicheverWayItIsHeld() {
        assertEquals("sess_abc", SessionToken.Value("sess_abc").token)
        assertEquals("sess_abc", minted(null).token)
    }

    @Test
    fun aMintedTokenNamesTheUserItWasMintedForAndAValueNamesNone() {
        assertEquals("7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1", minted("7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1").userId)
        assertNull(minted(null).userId)
        assertEquals(minted(null), minted(null))
        assertNotEquals(minted(null), minted("7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1"))
        assertNotEquals<SessionToken>(SessionToken.Value("sess_abc"), minted(null))
    }

    @Test
    fun aTokenPrintsByItsPrefixAloneHoweverItIsHeld() {
        assertEquals("SessionToken.Value(sess_…)", SessionToken.Value("sess_abc").toString())
        assertEquals("SessionToken.Minted(sess_…)", minted("7a4e1c98-3b52-4f60-9d81-c05e2f7a83b1").toString())
    }

    @Test
    fun aTokenReadPerRequestIsReadAfreshAndPrintsByWhereItIsReadFrom() {
        var answer: String? = "sess_one"
        val function = SessionToken.Function { answer }
        val source =
            object : SessionTokenSource {
                val state = MutableStateFlow(SessionSnapshot(token = "sess_held"))
                override val snapshot: StateFlow<SessionSnapshot> = state
            }

        assertEquals("sess_one", function.token)
        answer = null
        assertNull(function.token)
        assertEquals("sess_held", SessionToken.Source(source).token)
        source.state.value = SessionSnapshot()
        assertNull(SessionToken.Source(source).token)
        assertEquals("SessionToken.Function", function.toString())
        assertEquals("SessionToken.Source", SessionToken.Source(source).toString())
    }
}
