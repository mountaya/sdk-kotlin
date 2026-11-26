package com.example.consumer

import com.mountaya.admin.WebhookSignatureException
import com.mountaya.admin.constructWebhookEvent
import com.mountaya.admin.generated.WebhookEvent
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Webhooks receives a webhook delivery as a server does, through `constructWebhookEvent`, which no app
 * platform can name: the README's snippet, compiled where it runs.
 */
object Webhooks {
    /** Stands for the endpoint's signing secret, which the README's snippet names without declaring. */
    private val webhookSecret: String
        get() = "whsk_8f3c2a71b845d0e4c7a6b3f180d92e5a17c4e9b0d36f2a58e1c7b4093d6a2f15"

    /**
     * Receives a delivery on a server: the body exactly as it arrived, and its `Mountaya-Signature` header,
     * in; the status to answer the delivery with, out.
     */
    fun receive(
        body: ByteArray,
        signature: String?,
    ): Int {
        val event =
            try {
                constructWebhookEvent(body, signature, webhookSecret)
            } catch (refusal: WebhookSignatureException) {
                println(refusal.reason)
                return 400
            }

        // Each event arrives at least once: act on its `id` once.
        println("${event.id} ${event.type} ${event.created_at}")
        when (event) {
            is WebhookEvent.OrganizationMemberUpdated -> {
                // The membership as it is now, and the role it held before the change.
                println("${event.data.`object`.role} ${event.data.previous_attributes?.role}")
            }

            is WebhookEvent.CollectionDeleted -> {
                println("collection ${event.data.`object`.id} deleted")
            }

            else -> {}
        }
        return 204
    }

    /** Verifies a body given as text, its tolerance and its clock stated. */
    fun stated(
        body: String,
        signature: String?,
    ): WebhookEvent =
        constructWebhookEvent(
            body,
            signature,
            webhookSecret,
            tolerance = 10.minutes,
            now = Instant.fromEpochSeconds(0),
        )
}
