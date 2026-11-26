package com.example.consumer

import com.mountaya.admin.admin
import com.mountaya.admin.generated.AdminSchema
import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.generated.RestSchema
import com.mountaya.client.invoke

/** Server builds a client as a server does, under a secret key, which no app platform can name. */
object Server {
    /** Returns a client under a secret key, through the factory that takes one, declared on the jvm alone. */
    fun client(): Mountaya {
        val credential = Credential(secretKey = "sk_live_a")
        seen(credential, (credential as Credential.Secret).key)
        return Mountaya(credential)
    }

    /** Returns a secret key built as its own class, `Credential.Secret(key)`, which the jvm alone declares. */
    fun secret(): Credential.Secret {
        val secret = Credential.Secret("sk_live_a")
        seen(secret, secret.key)
        return secret
    }

    /**
     * Mints a session token for a user with the secret key, through the admin unit, which is what an app's own
     * backend answers its session engine with: the core's `Session`, a token a credential takes as it is.
     */
    suspend fun mint(
        server: Mountaya,
        userId: String,
    ): SessionToken {
        val session: RestSchema.Session =
            server.admin.sessions
                .postSession(
                    AdminSchema.SessionCreateParams(publishable_key = "pk_live_0a1b") {
                        user_id = userId
                        surface = RestSchema.SessionSurface.StudioEmbedded
                    },
                ).data
        return SessionToken.Minted(session)
    }
}
