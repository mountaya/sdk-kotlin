@file:JvmName("SecretKeyCredential")

package com.mountaya.client

/**
 * invoke, called as `Credential.Secret(key)`, makes the secret key [key] a credential, which mints session tokens
 * and belongs on a server.
 *
 * It is declared on the jvm alone, as the factory below is, so an Android or iOS build has nothing to call, and it
 * reads as the class's constructor, which is the module's own. It throws [ConfigurationException] where [key] is
 * missing, holds a character no header carries, or has the wrong prefix.
 */
public operator fun Credential.Secret.Companion.invoke(key: String): Credential.Secret = secretCredential(key)

/**
 * Credential builds the credential separate parts make, where one of them may be a secret key, which
 * mints session tokens and belongs on a server.
 *
 * It is declared on the jvm alone, the one target servers, command-line tools and desktop processes run on,
 * none of which `sdk/SPEC.md` counts an app build, so an Android or iOS build has nothing to call: the barrier
 * is the compiler's, where a check at run time would ship the key and hope.
 *
 * A secret key is sent alone, so it is refused beside any other part. Without one, this is the
 * factory that takes no secret key. [secretKey] comes last and has no default, so a call that does
 * not name it is never this one.
 */
public fun Credential(
    publishableKey: String? = null,
    sessionToken: SessionToken? = null,
    shareToken: String? = null,
    secretKey: String?,
): Credential {
    if (secretKey == null) {
        return Credential(
            publishableKey = publishableKey,
            sessionToken = sessionToken,
            shareToken = shareToken,
        )
    }
    if (publishableKey != null || sessionToken != null || shareToken != null) {
        throw ConfigurationException("A secret key is sent alone: pass no other part beside it.")
    }
    return secretCredential(secretKey)
}
