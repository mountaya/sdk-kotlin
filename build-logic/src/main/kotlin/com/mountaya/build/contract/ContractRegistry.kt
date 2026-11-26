package com.mountaya.build.contract

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File

/**
 * Capability is one entry of `sdk/registry.yaml`: a capability every SDK implements, the fixture that proves it, and
 * the names each SDK [published] it under, which only a public entry of the transport, geo and map layers states.
 */
data class Capability(
    /** The capability's id, such as `transport.retry.backoff`. */
    val id: String,
    /** The layer it belongs to: `transport`, `geo`, `map` or `studio`. */
    val layer: String,
    /** Whether a consumer calls it, so its runner reaches it through the public API. */
    val api: Boolean,
    /** The fixture that proves it, below `sdk/fixtures`. */
    val fixture: String,
    /** Each SDK's exact spellings of the declarations a consumer calls for it, or null where it states none. */
    val published: Published? = null,
)

/**
 * Published is an entry's `published`: for each of the four SDKs the contract is written for, its column, the exact
 * spellings of the declarations a consumer calls for the capability, or `scoped` where that SDK does not carry it
 * (`sdk/SPEC.md` section 9).
 */
data class Published(
    /** The TypeScript SDK's. */
    val typescript: PublishedColumn,
    /** The Dart SDK's. */
    val dart: PublishedColumn,
    /** The Swift SDK's. */
    val swift: PublishedColumn,
    /** This SDK's: each name a declaration's path after its package in the committed ABI dumps (SPEC.md section 5). */
    val kotlin: PublishedColumn,
)

/** PublishedColumn is one SDK's column of an entry's `published`. */
sealed interface PublishedColumn {
    /** Names is the capability published under these spellings, in the registry's order. */
    data class Names(
        /** The spellings, in the registry's order. */
        val names: List<String>,
    ) : PublishedColumn

    /** Scoped is the SDK not carrying the capability. */
    data object Scoped : PublishedColumn
}

/**
 * ContractRegistry is `sdk/registry.yaml` of the contract checkout, read at build time.
 *
 * The registry is the port manifest and there is no other: nothing in this repository lists
 * what has been ported, so nothing here can disagree with it.
 *
 * Its [version] is read and carried into the report, and never pinned. It moves with the
 * contract's expected values rather than with the file's format, so a reader that refused a
 * version it did not know would refuse a registry it reads perfectly well (SPEC.md section 9).
 * What is refused is a shape: a top level, an entry or a field this reader was not written for.
 */
data class ContractRegistry(
    /** The registry's version, carried into the report. */
    val version: Int,
    /** Every capability, in the registry's order. */
    val capabilities: List<Capability>,
) {
    /** The reading of the registry. */
    companion object {
        /** Reads [file], failing on a shape this reader was not written for. */
        fun read(file: File): ContractRegistry {
            val root = Load(LoadSettings.builder().build()).loadFromString(file.readText())
            require(root is Map<*, *>) { "${file.name}: expected a mapping at the top level." }
            val version = root["version"]
            require(version is Int && version > 0) {
                "${file.name}: `version` must be a positive integer, and it is ${version ?: "absent"}."
            }
            val entries = root["capabilities"]
            require(
                entries is List<*> && entries.isNotEmpty(),
            ) { "${file.name}: `capabilities` must be a non-empty list." }

            val capabilities =
                entries.mapIndexed { index, entry ->
                    require(entry is Map<*, *>) { "${file.name}: capability #${index + 1} is not a mapping." }
                    val id = entry["id"]
                    val layer = entry["layer"]
                    val api = entry["api"]
                    val fixture = entry["fixture"]
                    require(id is String && layer is String && api is Boolean && fixture is String) {
                        "${file.name}: capability #${index + 1} needs a string `id`, `layer` and `fixture`, and a boolean `api`."
                    }
                    val where = "${file.name}: `$id`"
                    when (val published = entry["published"]) {
                        null -> Capability(id, layer, api, fixture)

                        is Map<*, *> -> Capability(id, layer, api, fixture, columns(published, where))

                        else -> throw IllegalArgumentException(
                            "$where: `published` must map `typescript`, `dart`, `swift` and `kotlin` to their names.",
                        )
                    }
                }
            val repeated = capabilities.groupBy { it.id }.filterValues { it.size > 1 }.keys
            require(repeated.isEmpty()) { "${file.name}: declared twice: ${repeated.joinToString(", ")}." }
            return ContractRegistry(version, capabilities)
        }

        /** The four SDKs a `published` mapping holds a column for, in the registry's order. */
        private val SDKS = listOf("typescript", "dart", "swift", "kotlin")

        /** What a column holds in place of names where its SDK does not carry the capability. */
        private const val SCOPED = "scoped"

        /**
         * A Kotlin spelling: a declaration's path after its package, each part an identifier, so a
         * type, a top-level function or property, or a member of a type however deeply it is nested.
         */
        private val KOTLIN_NAME = Regex("""^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z_][A-Za-z0-9_]*)*$""")

        /**
         * Reads an entry's `published` mapping: exactly the four SDKs, each a non-empty list of distinct
         * names or `scoped`. A Kotlin name is held to the shape a path in an ABI dump has; another SDK's
         * names are that SDK's to hold to its own shape, and are read as text.
         */
        private fun columns(
            value: Map<*, *>,
            where: String,
        ): Published {
            val keys = value.keys.map { it.toString() }
            require(keys.sorted() == SDKS.sorted()) {
                "$where: `published` maps exactly `typescript`, `dart`, `swift` and `kotlin`, and it maps " +
                    "${keys.joinToString(", ") { "`$it`" }}."
            }
            val read = SDKS.associateWith { sdk -> column(value[sdk], sdk, where) }
            return Published(
                read.getValue("typescript"),
                read.getValue("dart"),
                read.getValue("swift"),
                read.getValue("kotlin"),
            )
        }

        /** Reads [sdk]'s column of an entry's `published`. */
        private fun column(
            value: Any?,
            sdk: String,
            where: String,
        ): PublishedColumn {
            if (value == SCOPED) return PublishedColumn.Scoped
            require(value is List<*> && value.isNotEmpty()) {
                "$where: `published.$sdk` must be `$SCOPED` or a non-empty list of names."
            }
            val names =
                value.map { name ->
                    require(name is String && name.isNotBlank() && name.trim() == name) {
                        "$where: `published.$sdk` holds ${name ?: "nothing"}, which is no name."
                    }
                    require(sdk != "kotlin" || KOTLIN_NAME.matches(name)) {
                        "$where: `published.kotlin` holds `$name`, which is no path after a package, " +
                            "`Name`, `name` or `Type.member`."
                    }
                    name
                }
            val twice = names.groupBy { it }.filterValues { it.size > 1 }.keys
            require(twice.isEmpty()) { "$where: `published.$sdk` lists ${twice.joinToString(", ") { "`$it`" }} twice." }
            return PublishedColumn.Names(names)
        }
    }
}

/**
 * Scopes holds the capabilities this SDK deliberately does not carry.
 *
 * A scope is a named absence, not a skip: each one is held to the sentence of `SPEC.md`
 * section 11 that says so, and reported as `scoped`. Anything else with no runner fails.
 */
data class Scopes(
    /** The layers scoped whole, each valued by the title of the bullet that scopes it. */
    val layers: Map<String, String>,
    /** The capabilities scoped one by one, each valued by the title of the bullet that scopes it. */
    val ids: Map<String, String>,
) {
    /** Returns the title of the section 11 bullet that scopes [capability], or null when nothing does. */
    fun sentenceFor(capability: Capability): String? = ids[capability.id] ?: layers[capability.layer]

    /** Every bullet title this table relies on. */
    val sentences: Set<String> = (layers.values + ids.values).toSet()
}

/** ContractLayout says where the contract checkout's files are, relative to its root. */
object ContractLayout {
    /** The registry. */
    const val REGISTRY = "sdk/registry.yaml"

    /** The directory of the fixtures. */
    const val FIXTURES = "sdk/fixtures"

    /** This SDK's conformance report. */
    const val REPORT = "sdk/reports/kotlin.json"

    /** The hot paths every SDK budgets. */
    const val HOT_PATHS = "sdk/hot-paths.yaml"

    /** Trail's scenario. */
    const val TRAIL = "sdk/trail.yaml"

    /** The demo itinerary every Trail ships a copy of. */
    const val TRAIL_ITINERARY = "sdk/trail/itinerary.json"
}
