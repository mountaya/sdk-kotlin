package com.mountaya.client

/**
 * ConnectionException is a request that never arrived, so no status answered it: a dropped
 * connection, a body that broke off, or an attempt that spent its time budget.
 *
 * A cancelled call is not one: it throws its `CancellationException`, since a connection failure
 * is retried and a retry after the caller left is a request nobody is waiting for. No request id
 * rides one, since no answer came, and the client never makes one up. [cause] is the platform's
 * own failure, which rides along for diagnostics and is never what a caller matches; a spent
 * budget has none.
 */
public class ConnectionException(
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : MountayaException("the request never arrived", cause, idempotencyKey) {
    override val name: String get() = "ConnectionError"
}
