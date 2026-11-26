package com.mountaya.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTokenSourceTest {
    @Test
    fun aSourceOfTheApplicationsOwnIsReadByItsSnapshotNow() {
        val state = MutableStateFlow(SessionSnapshot(token = "sess_a"))
        val source =
            object : SessionTokenSource {
                override val snapshot: StateFlow<SessionSnapshot> = state
            }

        assertEquals("sess_a", source.snapshot.value.token)
        state.value = SessionSnapshot(token = "sess_b")
        assertEquals("sess_b", source.snapshot.value.token)
    }
}
