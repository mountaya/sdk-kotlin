package com.mountaya.client

import kotlinx.serialization.SerializationException

/** What kotlinx.serialization writes before the copy of its input it appends to a decoding failure's message. */
private const val INPUT_COPY = "JSON input:"

/**
 * decoderFailure rebuilds the decoder's [failure] as a failure that carries none of the input it read: its message
 * up to the copy of the input kotlinx.serialization appends to it, which keeps where and why the input did not
 * decode, and no failure beneath it.
 *
 * kotlinx.serialization ends a JSON decoding failure's message with `JSON input:` and the input itself, the whole of
 * it where it is under 200 characters and the 60 around the failure otherwise. A session's answer is that small, and
 * an answer or a webhook delivery may hold a setup intent's `client_secret`, a share link's token or a webhook
 * endpoint's signing secret, so the failure as thrown would carry a credential into every log that prints an error's
 * cause. The failure it wrapped, where it wrapped one, goes too, since its message is the decoder's as well.
 *
 * It is `InternalMountayaApi`: the admin unit, `mountaya-admin`, reads a verified webhook delivery through it,
 * and no consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public fun decoderFailure(failure: Exception): SerializationException =
    SerializationException(failure.message?.substringBefore(INPUT_COPY)?.trimEnd())
