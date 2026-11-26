package com.mountaya.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * SettableSerializer writes a [Settable] as the value it holds, `null` included, and reads any
 * value that arrived, `null` included, as one that was set.
 *
 * [Settable.Absent] is never written: it is the property's default, which the client's `Json`
 * leaves out of a request. Asked to write one anyway, it refuses rather than send a `null` that
 * would clear what the caller never touched.
 *
 * It is `InternalMountayaApi`, as [Settable] is: the serialization plugin names it in the printed admin unit's
 * serializers, beside the core, and no consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public class SettableSerializer<T>(
    private val value: KSerializer<T>,
) : KSerializer<Settable<T>> {
    override val descriptor: SerialDescriptor = value.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Settable<T>,
    ) {
        when (value) {
            is Settable.Present -> encoder.encodeSerializableValue(this.value, value.value)

            Settable.Absent -> throw SerializationException(
                "A property nobody set is left out of a request, and this Json writes defaults: " +
                    "encode a printed type with the client's own Json.",
            )
        }
    }

    override fun deserialize(decoder: Decoder): Settable<T> = Settable.Present(decoder.decodeSerializableValue(value))
}
