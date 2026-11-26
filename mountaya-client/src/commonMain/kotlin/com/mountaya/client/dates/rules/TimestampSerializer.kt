package com.mountaya.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * TimestampSerializer reads a value the contract declares a timestamp through the anchored rule,
 * and writes an instant in UTC, to the millisecond, as [formatTimestamp] spells it.
 *
 * A property declared `format: date-time`, and the Data API's `DateTime` scalar, are read here
 * and nowhere else, so every other string stays a string whatever it looks like. A value that
 * fails the anchor fails the decode: the contract declared a timestamp and something else
 * arrived, and the error class that failure raises waits on the contract.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, names it from beside the core, and no
 * consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public object TimestampSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.mountaya.client.Timestamp", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Instant,
    ) {
        encoder.encodeString(formatTimestamp(value))
    }

    override fun deserialize(decoder: Decoder): Instant {
        val text = decoder.decodeString()
        return parseTimestamp(text)
            ?: throw SerializationException(
                "`$text` is not a timestamp: the contract declares a full ISO 8601 date and time with a zone here.",
            )
    }
}
