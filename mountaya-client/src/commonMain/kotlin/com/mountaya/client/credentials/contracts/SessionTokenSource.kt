package com.mountaya.client

import kotlinx.coroutines.flow.StateFlow

/**
 * SessionTokenSource is a source of session tokens that is read by a snapshot and tells its
 * collectors of each change: the shape `SessionTokenEngine` has, and the one a source of the
 * application's own takes.
 *
 * The snapshot is a read that does not suspend, from any thread, since a map's request hook asks
 * for a token on a thread of its own and cannot suspend. A request reads it on every attempt.
 */
public interface SessionTokenSource {
    /**
     * The source's state, one `StateFlow` as an engine's `state` is: its [StateFlow.value] is the
     * snapshot now, the token, its expiry, the user it names, whether a mint is on its way and the
     * last mint's failure, and collecting it is the subscription, told the snapshot at once and then
     * each one that replaces it. A collector that has not read the last change is handed the newest.
     */
    public val snapshot: StateFlow<SessionSnapshot>
}
