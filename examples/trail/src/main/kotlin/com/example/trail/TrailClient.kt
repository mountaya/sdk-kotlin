package com.example.trail

import com.mountaya.client.Credential
import com.mountaya.client.Mountaya
import com.mountaya.client.SessionToken
import com.mountaya.client.SessionTokenEngine
import com.mountaya.client.generated.RestSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/** How long the session URL is given to connect, and then to answer. */
private const val SESSION_TIMEOUT_MILLIS = 10_000

/** trailJson is Trail's JSON, read as the API writes it: a key it does not know is left aside. */
val trailJson: Json = Json { ignoreUnknownKeys = true }

/**
 * trailClient builds the one client Trail reads through, in the device's [language], carrying the publishable key
 * [settings] holds, or null where none is set. Where a session URL is set, it runs over a [SessionTokenEngine] in
 * [scope] whose mint posts to that address and reads the `Session` it answers, so search, which the Data API answers a
 * key only beside a session token, works, while the secret key that mints the token stays on the server.
 */
fun trailClient(
    settings: TrailSettings,
    scope: CoroutineScope,
    language: String,
): Mountaya? {
    // trail: trail.client
    val key = settings.publishableKey ?: return null
    val sessionUrl = settings.sessionUrl
    val credential =
        if (sessionUrl == null) {
            Credential.Publishable(key)
        } else {
            val sessions = SessionTokenEngine(scope, mint = { mintSession(sessionUrl) })
            Credential.Publishable(key, SessionToken.Source(sessions))
        }
    return Mountaya(credential, language = language)
}

/**
 * mintSession posts to [address], the session server's or the app's own backend's, and reads the `Session` it
 * answers, the JSON object `postSession` answers under `data`.
 *
 * @throws IOException where the address cannot be reached or answers anything but a success, which the session engine
 * tries again on its own schedule.
 */
suspend fun mintSession(address: String): RestSchema.Session =
    withContext(Dispatchers.IO) {
        val connection = URI.create(address).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = SESSION_TIMEOUT_MILLIS
            connection.readTimeout = SESSION_TIMEOUT_MILLIS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write("{}".encodeToByteArray()) }
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("The session URL answered $status.")
            val body = connection.inputStream.use { it.readBytes().decodeToString() }
            trailJson.decodeFromString(RestSchema.Session.serializer(), body)
        } finally {
            connection.disconnect()
        }
    }
