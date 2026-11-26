package com.mountaya.client

/** The header a request names the SDK that made it in. */
internal const val CLIENT_HEADER = "X-Mountaya-Client"

/**
 * ClientIdentity is what every request names the SDK that made it by (`sdk/SPEC.md` section 1):
 * [CLIENT_HEADER] carrying `mountaya-kotlin/<version>`, the version every module is published at,
 * which the build prints from the version catalog, and never the contract tag the client speaks.
 *
 * It is the client's, as its language is: it rides every request the transport makes, an operation
 * that takes no credential and every event stream's open included, and every Tile API request a map
 * makes for itself, beside the credential. No method takes it, and a call by declaration that names it
 * is refused. It is no part of a credential's projection, so `Credential.headers` answers without it.
 */
internal object ClientIdentity {
    /** The language the header names, Mountaya's word for this SDK. */
    private const val LANGUAGE = "kotlin"

    /** What [CLIENT_HEADER] carries. */
    val value: String = "mountaya-$LANGUAGE/$CLIENT_VERSION"
}
