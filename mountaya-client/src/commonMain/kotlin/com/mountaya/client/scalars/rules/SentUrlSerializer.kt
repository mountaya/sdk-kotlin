package com.mountaya.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * SentUrlSerializer writes a [Url] a request sends, which the printed types a request carries name for
 * each web address they hold, and reads one as [UrlSerializer] does.
 *
 * A web address a caller builds is held to a scheme, a colon and at least one more character, none
 * a line break, as the contract asks of a `format: uri` value a request sends: one with no scheme, a
 * path or a host alone, is refused with a [ConfigurationException] as the request's body is written,
 * which is before any request leaves. An answer's is read as [UrlSerializer] reads it, whatever it
 * holds.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, names it from beside the core, and no
 * consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public object SentUrlSerializer : KSerializer<Url> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.mountaya.client.SentUrl", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Url,
    ) {
        encoder.encodeString(sentText(value))
    }

    override fun deserialize(decoder: Decoder): Url = Url(decoder.decodeString())

    /**
     * Returns the text [value] is sent as, in a body, a path, a query or a form, or refuses it with a
     * [ConfigurationException] where it has no scheme. The refusal quotes the value, which is a web
     * address and never a credential.
     */
    public fun sentText(value: Url): String {
        if (value.hasScheme) return value.toString()
        throw ConfigurationException(
            "`$value` is not a URL: a URL starts with a scheme, such as `https:`, and a web address with none is " +
                "not sent.",
        )
    }
}
