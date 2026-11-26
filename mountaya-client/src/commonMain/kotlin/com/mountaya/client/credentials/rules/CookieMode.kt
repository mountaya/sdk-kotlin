package com.mountaya.client

/** CookieMode is whether a request asks for the visitor's cookies. */
internal enum class CookieMode(
    /** The contract's word for it. */
    val word: String,
) {
    /** Include sends the cookies the credential's source answers. */
    Include("include"),

    /** Omit sends none. */
    Omit("omit"),
    ;

    companion object {
        /**
         * Returns whether a request under [credential] to an operation declaring [declaration] asks
         * for cookies: only under a share token that asks for them, and never to an operation that
         * takes no credential.
         *
         * A key never asks for cookies, publishable or secret, with a session token or without: a
         * cookie's broader session would outrank it.
         */
        fun mode(
            credential: Credential,
            declaration: CredentialDeclaration,
        ): CookieMode =
            if (!declaration.takesNoCredential && credential is Credential.Share && credential.cookies != null) {
                Include
            } else {
                Omit
            }
    }
}
