package com.mountaya.client

import kotlinx.serialization.json.Json

/**
 * wireJson is the `Json` every request is written with and every response read with.
 *
 * It ignores a key it does not know, so a property the contract adds decodes in a build made
 * before it. It leaves a default out, which is what makes a printed type's optional property
 * absent from the request until it is set, and writes `null` where a property holds one, which is
 * how a required nullable property and a cleared merge property reach the server.
 *
 * It is `InternalMountayaApi`: the printed admin unit, `mountaya-admin`, reads it from beside the core, and no
 * consumer does (SPEC.md section 7).
 */
@InternalMountayaApi
public val wireJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
