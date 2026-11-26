package com.mountaya.client

/**
 * ConfigurationException is a client or a call set up wrong, refused before any request left the
 * process: a credential with the wrong prefix, or one the operation does not take.
 *
 * The server would answer most of these with a 401 or a 400 that names no rule. The local
 * refusal names it, and its message says what is wrong and what would be right, for the
 * developer who set it up. [cause] is what made the refusal where something beneath it failed,
 * such as the failed mint of a session token an operation requires.
 */
public class ConfigurationException(
    message: String,
    idempotencyKey: String? = null,
    cause: Throwable? = null,
) : MountayaException(message, cause, idempotencyKey) {
    override val name: String get() = "ConfigurationError"
}
