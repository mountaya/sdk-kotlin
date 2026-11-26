package com.mountaya.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable

/**
 * nullableSerializer answers [serializer] reading and writing `null` as well, for a printed call whose
 * payload may be null. It is a function so printed code can name it in full, with no import, which an
 * extension property cannot be.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, calls it from beside the core, and no
 * consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public fun <T : Any> nullableSerializer(serializer: KSerializer<T>): KSerializer<T?> = serializer.nullable
