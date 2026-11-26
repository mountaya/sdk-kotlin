package com.mountaya.build.contract

import java.io.File
import java.nio.file.Files

/** A column of [names], in their order. */
internal fun names(vararg names: String): PublishedColumn = PublishedColumn.Names(names.toList())

/** An entry's `published` whose `kotlin` column is [kotlin], every other SDK's [others]. */
internal fun columns(
    kotlin: PublishedColumn,
    others: PublishedColumn = kotlin,
): Published = Published(typescript = others, dart = others, swift = others, kotlin = kotlin)

/**
 * A registry small enough to read at a glance, shaped like the contract's: every layer, both flags,
 * and the names its public entries are published under in each SDK's column, the studio's included,
 * which this SDK's column scopes as its scope table does.
 */
internal val sampleRegistry =
    ContractRegistry(
        version = 6,
        capabilities =
            listOf(
                Capability("transport.retry.backoff", "transport", false, "transport/retry-backoff.json"),
                Capability(
                    "transport.credentials",
                    "transport",
                    true,
                    "transport/credentials.json",
                    columns(
                        kotlin = names("Credential", "Credential.headers", "ConfigurationException"),
                        others = names("Credential", "Credential.headers", "ConfigurationError"),
                    ),
                ),
                Capability("transport.browser.secret-key", "transport", false, "transport/browser-secret-key.json"),
                Capability(
                    "geo.layers.order",
                    "geo",
                    true,
                    "geo/layers-order.json",
                    columns(names("tileLayerRank", "tileSourceLayerRanks")),
                ),
                Capability("geo.camera.ground-meters", "geo", false, "geo/route-ground-meters.json"),
                Capability("map.edit.snap", "map", false, "map/edit-snap.json"),
                Capability(
                    "studio.share.links",
                    "studio",
                    true,
                    "studio/share-links.json",
                    columns(kotlin = PublishedColumn.Scoped, others = names("ShareLink")),
                ),
            ),
    )

/**
 * A klib dump in the shape the Kotlin plugin writes, declaring every name [sampleRegistry]
 * publishes but the studio's: a type, a member, an exception, a function and a property.
 */
internal val sampleKlibDump =
    """
    // Klib ABI Dump
    // Targets: [iosArm64, iosSimulatorArm64]
    // Library unique name: <com.mountaya:mountaya-client>
    abstract interface com.mountaya.client/Credential { // com.mountaya.client/Credential|null[0]
        open fun headers(kotlin/String?): kotlin.collections/Map<kotlin/String, kotlin/String> // com.mountaya.client/Credential.headers|headers(kotlin.String?){}[0]
    }

    final class com.mountaya.client/ConfigurationException : com.mountaya.client/MountayaException { // com.mountaya.client/ConfigurationException|null[0]
        constructor <init>(kotlin/String) // com.mountaya.client/ConfigurationException.<init>|<init>(kotlin.String){}[0]
    }

    final val com.mountaya.geo/tileSourceLayerRanks // com.mountaya.geo/tileSourceLayerRanks|{}tileSourceLayerRanks[0]
        final fun <get-tileSourceLayerRanks>(): kotlin.collections/Map<kotlin/String, kotlin/Int> // com.mountaya.geo/tileSourceLayerRanks.<get-tileSourceLayerRanks>|<get-tileSourceLayerRanks>(){}[0]

    final fun com.mountaya.geo/tileLayerRank(kotlin/String): kotlin/Int // com.mountaya.geo/tileLayerRank|tileLayerRank(kotlin.String){}[0]
    """.trimIndent()

/** What [sampleKlibDump] declares. */
internal val sampleApi = ApiDeclarations.read(listOf(sampleKlibDump))

/** The ownership table this repository's root build states, `mountaya-geo` and `mountaya-elements` included. */
internal val sampleOwnership =
    Ownership(
        apiModules =
            mapOf(
                "transport" to "conformance",
                "geo" to "conformance",
                "map" to "mountaya-elements",
                "studio" to "conformance",
            ),
        internalModules =
            mapOf(
                "transport" to "mountaya-client",
                "geo" to "mountaya-geo",
                "map" to "mountaya-elements",
            ),
    )

/** The scope table this repository states, applied to [sampleRegistry]. */
internal val sampleScopes =
    Scopes(
        layers = mapOf("studio" to "No studio layer."),
        ids = mapOf("transport.browser.secret-key" to "No browser refusal for the secret key."),
    )

/** A `SPEC.md` whose section 11 carries both sentences [sampleScopes] relies on. */
internal val sampleSpec =
    """
    # SPEC

    ## 10. Release

    Nothing here.

    ## 11. Deliberate divergences

    - **No studio layer.** The studio is TypeScript-only.
    - **No browser refusal for the secret key.** There is no browser target.

    ## 12. After
    """.trimIndent()

/** Writes [text] to a throwaway file and returns it. */
internal fun temporaryFile(
    name: String,
    text: String,
): File {
    val directory = Files.createTempDirectory("contract").toFile()
    return File(directory, name).apply { writeText(text) }
}
