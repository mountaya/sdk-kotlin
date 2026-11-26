package com.mountaya.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class ResolvedSessionTest {
    private class MintFailed : Exception("the mint failed")

    private class Moving(
        initial: SessionSnapshot,
    ) : SessionTokenSource {
        val state = MutableStateFlow(initial)
        override val snapshot: StateFlow<SessionSnapshot> = state
    }

    @Test
    fun aFunctionIsReadForTheRequestAndAWrongPrefixItAnswersIsRefusedAtTheRead() {
        assertEquals("sess_read", ResolvedSession.reading(SessionToken.Function { "sess_read" }, null).token)
        assertNull(ResolvedSession.reading(SessionToken.Function { null }, null).token)
        assertFailsWith<ConfigurationException> { ResolvedSession.reading(SessionToken.Function { "tok_read" }, null) }
    }

    @Test
    fun aSourceNamesItsUserAndASourceWithNoneCarriesTheFailureOfItsMint() {
        val read = ResolvedSession.reading(SessionToken.Source(Moving(SessionSnapshot(token = "sess_a"))), null)
        val none = ResolvedSession.reading(SessionToken.Source(Moving(SessionSnapshot(error = MintFailed()))), null)

        assertEquals("sess_a", read.token)
        assertNull(assertIs<ResolvedSession.User.Known>(read.user).id)
        assertNull(none.token)
        assertIs<MintFailed>(none.mintError)
        assertIs<ResolvedSession.User.Unknown>(ResolvedSession.reading(SessionToken.Value("sess_v"), null).user)
    }

    @Test
    fun aRequestWaitsWhileATokenIsOnItsWayAndNotOnceNoneIs() =
        runTest {
            val arriving = Moving(SessionSnapshot(isMinting = true))
            val failing = Moving(SessionSnapshot(isMinting = true))
            val idle = Moving(SessionSnapshot())
            launch {
                yield()
                arriving.state.value = SessionSnapshot(token = "sess_minted")
                failing.state.value = SessionSnapshot(error = MintFailed())
                idle.state.value = SessionSnapshot(token = "sess_later")
            }

            assertNull(ResolvedSession.awaitingFirstToken(idle).token)
            assertEquals("sess_minted", ResolvedSession.awaitingFirstToken(arriving).token)
            assertIs<MintFailed>(ResolvedSession.awaitingFirstToken(failing).error)
        }
}
