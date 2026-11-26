package com.mountaya.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * UrlSerializer reads and writes a [Url] as the text it holds, which is a JSON string.
 *
 * It reads whatever text the wire sent, one with no scheme included, so an answer's web address is
 * kept as written and never fails the answer, and writes that text back as it was read. What a
 * request sends is written by [SentUrlSerializer], which refuses a value with no scheme.
 */
internal object UrlSerializer : KSerializer<Url> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.mountaya.client.Url", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Url,
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Url = Url(decoder.decodeString())
}
