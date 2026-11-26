package com.mountaya.client

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class SessionSnapshotTest {
    private class Failed : Exception("failed")

    @Test
    fun twoSnapshotsAreEqualWhereTheyHoldTheSameTokenAndAgreeOnAMintAndAFailure() {
        val expiry = Instant.fromEpochSeconds(1_800_000_000)

        assertEquals(SessionSnapshot(), SessionSnapshot())
        assertEquals(SessionSnapshot("sess_a", expiry), SessionSnapshot("sess_a", expiry))
        assertNotEquals(SessionSnapshot("sess_a"), SessionSnapshot("sess_b"))
        assertNotEquals(SessionSnapshot(isMinting = true), SessionSnapshot())
        assertNotEquals(SessionSnapshot(error = Failed()), SessionSnapshot())
        assertEquals(SessionSnapshot(error = Failed()), SessionSnapshot(error = Failed()))
    }

    @Test
    fun aSnapshotPrintsItsTokenByItsPrefixAloneAndNothingOfItsUser() {
        val snapshot = SessionSnapshot("sess_secret", Instant.fromEpochSeconds(0), "u1", isMinting = true)

        assertEquals(
            "SessionSnapshot(token=sess_…, expiresAt=1970-01-01T00:00:00.000Z, isMinting=true)",
            snapshot.toString(),
        )
        assertFalse("u1" in snapshot.toString())
    }
}
